package com.choqnet.budget.app;

import com.choqnet.budget.entity.*;
import com.choqnet.budget.entity.datalists.Operation;
import com.choqnet.budget.entity.datalists.Source;
import com.choqnet.budget.entity.datalists.TypeMsg;
import com.google.gson.Gson;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.security.Authenticated;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.email.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MainProcessBean {
    private static final Logger log = LoggerFactory.getLogger(MainProcessBean.class);
    private String jiraToken;
    private final Gson gson = new Gson();
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Emailer emailer;
    @Autowired
    private SystemAuthenticator systemAuthenticator;

    final int NB_ITEM_PER_PAGE = 5000;
    List<String> quarter1 = Arrays.asList("fin202201", "fin202202", "fin202203");
    List<String> quarter2 = Arrays.asList("fin202204", "fin202205", "fin202206");
    List<String> quarter3 = Arrays.asList("fin202207", "fin202208", "fin202209");
    List<String> quarter4 = Arrays.asList("fin202210", "fin202211", "fin202212");
    List<String> months = new ArrayList<>();
    List<String> quarters;

    List<Command> tempoCmds;
    String month;
    String dumboToken;
    HttpURLConnection conn = null;

    @ManagedOperation
    public void mainProcess() {
        systemAuthenticator.withUser("admin", () -> {
            saveLogApp("Scheduler", TypeMsg.INFO, "Starting", "");
            log.info("Main process starting....");
            getJiraTeams();
            log.info("Jira Teams OK");
            getDumboData();
            saveLogApp("Scheduler", TypeMsg.INFO, "Finishing", "");
            fireLogs();
            return null;
        });
    }

    @ManagedOperation
    public void getDumboData() {
        systemAuthenticator.withUser("admin", () -> {
            int processedItems;
            int page;
            loadMonth();
            getDumboToken();
            for (Command cmd : tempoCmds) {
                month = cmd.getValue();
                // Get the worklogs : finWL
                log.info("Get worklog for " + month);
                deleteMonthWorkloads(month);
                page = 0;
                do {
                    processedItems = getWorklogs(page, month);
                    page += NB_ITEM_PER_PAGE;
                } while (processedItems != 0);
                connectWorklogs(month);

                // Get the actualMonths
                log.info("Get actuals for " + month);
                deleteActualMonths(month);
                page = 0;
                do {
                    processedItems = getActuals(page, month);
                    page += NB_ITEM_PER_PAGE;
                } while (processedItems != 0);
                connectActuals(month);

                //setActualProgress();
                log.info("Month " + month + " processed");
            }
            // connect newly importer Actuals to the Progresses
            setActualProgress(months);
            // delete the commands just run
            dataManager.remove(tempoCmds);
            saveLogApp("MainProcess", TypeMsg.INFO, "Done with import of Dumbo data", null);
            return null;
        });
    }

    private List<String> giveQuarterMonths(String quarter) {
        String year = quarter.substring(0,4);
        List<String> qMonths = new ArrayList<>();
        List<String> tmp = new ArrayList<>();
        switch (quarter.substring(4)) {
            case "Q1":
                tmp = Arrays.asList("01", "02", "03");
                break;
            case "Q2":
                tmp = Arrays.asList("04", "05", "06");
                break;
            case "Q3":
                tmp = Arrays.asList("07", "08", "09");
                break;
            case "Q4":
                tmp = Arrays.asList("10", "11", "12");
                break;
        }
        for (String mm: tmp) {
            qMonths.add("fin" + year + mm);
        }
        return qMonths;
    }

    private Progress setQxValue(Progress target, double value, String quarter) {
        String shortQuarter = quarter.substring(quarter.length() - 2);
        switch(shortQuarter) {
            case "Q1":
                target.setActualQ1(value);
                break;
            case "Q2":
                target.setActualQ2(value);
                break;
            case "Q3":
                target.setActualQ3(value);
                break;
            case "Q4":
                target.setActualQ4(value);
                break;
            default:
                break;
        }
        return target;
    }

    private List<String> convertMonthsToQuarters(List<String> months) {
        List<String> output = new ArrayList<>();
        for (String month: months) {
            String year = month.substring(3,7);
            String mm = month.substring(7);
            switch (mm) {
                case "01":
                case "02":
                case "03":
                    output.add(year+"Q1");
                    break;
                case "04":
                case "05":
                case "06":
                    output.add(year+"Q2");
                    break;
                case "07":
                case "08":
                case "09":
                    output.add(year+"Q3");
                    break;
                case "10":
                case "11":
                case "12":
                    output.add(year+"Q4");
                    break;
            }
        }
        return output.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Update all Progress records with the actual efforts just imported
     * @param months
     */
    private void setActualProgress(List<String> months) {
        systemAuthenticator.withUser("admin", () -> {
            saveLogApp("MainProcess", TypeMsg.INFO, "Connecting Actuals to Progresses", null);
            String currentYear;
            // 1. convert the list of months into a list of quarters
            quarters = convertMonthsToQuarters(months);
            // 2. fetch the quarters
            for (String quarter: quarters) {
                // a. quarter -> year
                currentYear = quarter.substring(0,4);
                // b. year -> list of budgets
                List<Budget> budgets = dataManager.load(Budget.class).query("select e from Budget e where e.year = :year").parameter("year", currentYear).list();
                // c. build the list of month of the quarter
                List<String> qMonths = giveQuarterMonths(quarter);
                // d. build the list of actuals belonging to these months
                List<Actual> actuals = dataManager.load(Actual.class).query("select e from Actual e where e.finMonth IN :months").parameter("months", qMonths).list();
                // e. get the list of IPRB out of them
                List<IPRB> iprbs = actuals.stream().map(e -> e.getIprb()).distinct().collect(Collectors.toList());
                // f. fetch the IPRBs

                for (IPRB iprb: iprbs) {
                    // fetch the budgets
                    for (Budget budget: budgets) {
                        SaveContext sc = new SaveContext();
                        // get the list of progresses to update
                        List<Progress> progresses = dataManager.load(Progress.class)
                                .query("select e from Progress e where e.budget = :budget and e.iprb = :iprb")
                                .parameter("budget", budget)
                                .parameter("iprb",iprb)
                                .list();
                        for (Progress progress: progresses) {
                            double value = sumQ(iprb, actuals);
                            progress = setQxValue(progress, value, quarter);
                            sc.saving(progress);
                        }
                        dataManager.save(sc);
                    }
                }

                saveLogApp("MainProcess", TypeMsg.INFO, "Quarter completed", quarter);
            }
                    return null;
                });
            /*
            // 1. get a list of unique involved quarters
            //quarters = quarters.stream().distinct().collect(Collectors.toList());

            List<Actual> actuals;
            List<IPRB> iprbs = new ArrayList<>();
            List<IPRB> defIprbs;
            // gets the default budget
            Budget budget = dataManager.load(Budget.class).query("select e from Budget e where e.preferred = true").one();
            // get the monthly actuals
            actuals = dataManager.load(Actual.class).query("select e from Actual e where e.finMonth = :finMonth").parameter("finMonth", month).list();
            // get the list of IPRB from this list
            iprbs = actuals.stream().map(Actual::getIprb).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            // get the list of existing Progress, for the default budget
            List<Progress> defaultProgresses = dataManager.load(Progress.class).query("select e from Progress e where e.budget = :budget").parameter("budget", budget).list();
            // get the list of IPRB from this default list
            defIprbs = defaultProgresses.stream().map(e -> e.getIprb()).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            // build the list of missing Progresses
            List<IPRB> missing = new ArrayList<>();
            for (IPRB iprb : iprbs) {
                if (!defIprbs.contains(iprb)) {
                    missing.add(iprb);
                }
            }
            // create the missing progresses
            for (IPRB iprb : missing) {
                Progress progress = dataManager.create(Progress.class);
                progress.setBudget(budget);
                progress.setIprb(iprb);
                log.warn("Progress created for " + iprb.getReference());
                dataManager.save(progress);
            }
            // get the full list of progress
            List<Progress> progresses = dataManager.load(Progress.class).all().list();
            SaveContext sc = new SaveContext();
            for (Progress progress : progresses) {
                progress.setActualQ1(actuals.stream().filter(e -> progress.getIprb().equals(e.getIprb()) && quarter1.contains(e.getFinMonth())).map(Actual::getEffort).reduce(0.0, Double::sum));
                progress.setActualQ2(actuals.stream().filter(e -> progress.getIprb().equals(e.getIprb()) && quarter2.contains(e.getFinMonth())).map(Actual::getEffort).reduce(0.0, Double::sum));
                progress.setActualQ3(actuals.stream().filter(e -> progress.getIprb().equals(e.getIprb()) && quarter3.contains(e.getFinMonth())).map(Actual::getEffort).reduce(0.0, Double::sum));
                progress.setActualQ4(actuals.stream().filter(e -> progress.getIprb().equals(e.getIprb()) && quarter4.contains(e.getFinMonth())).map(Actual::getEffort).reduce(0.0, Double::sum));
                sc.saving(progress);
            }
            dataManager.save(sc);
            log.info("Actual Progress updated for " + month);
            return null;

             */
    }

    private double sumQ(IPRB iprb, List<Actual> actuals) {
        if (iprb==null) {
            return actuals.stream().filter(e -> e.getIprb()==null).map(e -> e.getEffort()).reduce(0.0, Double::sum);
        } else {
            return actuals.stream().filter(e -> iprb.equals(e.getIprb())).map(e -> e.getEffort()).reduce(0.0, Double::sum);
        }
    }

    @ManagedOperation
    private void deleteActualMonths(String finMonth) {
        systemAuthenticator.withUser("admin", () -> {
            List<Actual> acts = dataManager.load(Actual.class).query("select e from Actual e where e.finMonth = :finMonth").parameter("finMonth", finMonth).list();
            dataManager.remove(acts);
            return null;
        });
    }

    @ManagedOperation
    private void deleteMonthWorkloads(String finMonth) {
        systemAuthenticator.withUser("admin", () -> {
            List<Worklog> wls = dataManager.load(Worklog.class).query("select e from Worklog e where e.finMonth = :finMonth").parameter("finMonth", finMonth).list();
            dataManager.remove(wls);
            return null;
        });
    }

    @ManagedOperation
    private void loadMonth() {
        systemAuthenticator.withUser("admin", () -> {
            // add getTempo command for month M and M-1
            tempoCmds = (dataManager.load(Command.class).query("select e from Command e where e.operation = :op")).parameter("op", Operation.TEMPO).list();
            for (int i = 0; i < 2; i++) {
                //String cMonth = "fin" + LocalDateTime.now().minusMonths(i).format(DateTimeFormatter.ofPattern("yyyyMM"));
                String cMonth = getFinancialMonth(addTime(new Date(),-i,"MONTH"));
                months.add(cMonth);
                if (tempoCmds.stream().noneMatch(e -> cMonth.equals(e.getValue()))) {
                    Command record = dataManager.create(Command.class);
                    record.setOperation(Operation.TEMPO);
                    record.setValue(cMonth);
                    record = dataManager.save(record);
                    tempoCmds.add(record);
                }
            }
            return null;
        });
    }

    @ManagedOperation
    private void getDumboToken() {
        systemAuthenticator.withUser("admin", () -> {
            try {

                String auth = "dumbo-0LuuFNPm:ce340f61da907c20ab1f0b66437bccddc863ff6d3db13311d1356fd2d7059a27";
                byte[] encodeAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
                conn = (HttpURLConnection) (new URL("http://cosnprpsn21:6100/dumbo/rest/v2/oauth/token")).openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Basic " + new String(encodeAuth));
                //conn.setRequestProperty("Authorization", "Basic Y2xpZW50OnNlY3JldA==");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                String data = "grant_type=password&username=restReader&password=c";
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(data);
                out.close();
                conn.disconnect();
                saveLogApp("MainProcess::getDumboToken", TypeMsg.INFO, "Responde code", String.valueOf(conn.getResponseCode()));
                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String reply = br.readLine();
                    Connect connect = gson.fromJson(reply, Connect.class);
                    dumboToken = connect.getAccess_token();
                    saveLogApp("MainProcess::getDumboToken", TypeMsg.INFO, "Token", dumboToken);
                }
            } catch (IOException e) {
                saveLogApp("MainProcess::getDumboToken", TypeMsg.ERROR, "Error", e.toString());
            }
            return null;
        });

    }

    private int getWorklogs(int offset, String finMonth) {
        int result = systemAuthenticator.withUser("admin", () -> {
            SaveContext sc;
            // gets the worklogs for the current month
            try {
                //URL dumboURL = new URL("http://cosnprpsn21:6100/dumbo/rest/v2/entities/dumbo_FinWL?view=finWLs&limit=50&offset=100000&sort=key");
                URL dumboURL = new URL("http://cosnprpsn21:6100/dumbo/rest/v2/queries/dumbo_FinWL/wl4boox?finMonth=" + finMonth + "&limit=" + NB_ITEM_PER_PAGE + "&offset=" + offset);
                HttpURLConnection connectionIssue = (HttpURLConnection) dumboURL.openConnection();
                connectionIssue.setRequestMethod("GET");
                connectionIssue.setRequestProperty("Accept", "*/*");
                connectionIssue.setRequestProperty("Content-Type", "application/json");
                connectionIssue.setDoOutput(false);
                connectionIssue.setDoInput(true);
                // connection with token
                // connectionIssue.setRequestProperty("Authorization", "Bearer 5UfLo2VoqYhLCaVUSPcxfBZSj6I");
                connectionIssue.setRequestProperty("Authorization", "Bearer " + dumboToken);
                connectionIssue.connect();
                BufferedReader in = new BufferedReader(new InputStreamReader(connectionIssue.getInputStream(), StandardCharsets.UTF_8));
                String answer = in.readLine();
                in.close();
                connectionIssue.disconnect();
                DumboWorklog[] dwls = gson.fromJson(answer, DumboWorklog[].class);
                sc = new SaveContext();
                for (DumboWorklog dwl : dwls) {
                    Worklog wl = dataManager.create(Worklog.class);
                    wl.setFinMonth(finMonth);
                    wl.setEffort(dwl.getEffort());
                    wl.setInitiative(dwl.getInit() == null ? "" : dwl.getInit().getKey());
                    wl.setCategory(dwl.getInit() == null ? "Non-Project" : codifCategory(dwl.getInit().getCategory()));
                    wl.setIprbRef(dwl.getIprb() == null ? "" : dwl.getIprb().getKey());
                    wl.setTeamRef(dwl.getTeamID());
                    wl.setUserRef(dwl.getUserRef());
                    wl.setDate(dwl.getDate());
                    wl.setKey(dwl.getKey());
                    wl.setUserName(dwl.getUserName());
                    wl.setResType(dwl.getResType());
                    wl.setInitRef(dwl.getInitRef());
                    wl.setFinanceType(dwl.getFinanceType());
                    wl.setSapID(dwl.getSapID());
                    wl.setAccount(dwl.getAccount());
                    wl.setInitStatus(dwl.getInit()==null ? "" : dwl.getInit().getStatus());
                    sc.saving(wl);
                }
                dataManager.save(sc);
                saveLogApp("MainProcess::getWorklogs", TypeMsg.INFO, "Success", "finMonth:" + finMonth + ", offset: " + offset + ", read: " + dwls.length);
               return dwls.length;
            } catch (Exception e) {
                saveLogApp("MainProcess::getWorklogs", TypeMsg.ERROR, "Error", e.toString());
                return 0;
            }
        });
        return result;
    }

    @ManagedOperation
    private int getActuals(int offset, String finMonth) {
        int result = systemAuthenticator.withUser("admin", () -> {
            SaveContext sc;
            // gets the actuals for all commands
            try {
                URL dumboURL = new URL("http://cosnprpsn21:6100/dumbo/rest/v2/queries/dumbo_ActualMonth/am4boox?finMonth=" + finMonth + "&limit=" + NB_ITEM_PER_PAGE + "&offset=" + offset);
                HttpURLConnection connectionIssue = (HttpURLConnection) dumboURL.openConnection();
                connectionIssue.setRequestMethod("GET");
                connectionIssue.setRequestProperty("Accept", "*/*");
                connectionIssue.setRequestProperty("Content-Type", "application/json");
                connectionIssue.setDoOutput(false);
                connectionIssue.setDoInput(true);
                // connection with token
                // connectionIssue.setRequestProperty("Authorization", "Bearer 5UfLo2VoqYhLCaVUSPcxfBZSj6I");
                connectionIssue.setRequestProperty("Authorization", "Bearer " + dumboToken);
                connectionIssue.connect();
                BufferedReader in = new BufferedReader(new InputStreamReader(connectionIssue.getInputStream(), StandardCharsets.UTF_8));
                String answer = in.readLine();
                in.close();
                connectionIssue.disconnect();
                DumboActualMonth[] dams = gson.fromJson(answer, DumboActualMonth[].class);
                sc = new SaveContext();
                for (DumboActualMonth dam : dams) {
                    Actual am = dataManager.create(Actual.class);
                    am.setCostCenter(dam.getCostCenterRef());
                    am.setEffort(dam.getEffort());
                    am.setInitRef(dam.getInitiative() == null ? "" : dam.getInitiative().getKey());
                    am.setInitName(dam.getInitiative() == null ? "" : dam.getInitiative().getName());
                    am.setInitCategory(dam.getInitiative() == null ? "Non-Project" : codifCategory(dam.getInitiative().getCategory()));
                    am.setFinMonth(finMonth);
                    am.setIprbRef(dam.getIprb() == null ? "" : dam.getIprb().getKey());
                    am.setJiraTeamRef(dam.getSquad() == null ? 0 : dam.getSquad().getSqID());
                    am.setJiraProject(dam.getJiraProject() == null ? "" : dam.getJiraProject().getName());
                    am.setJiraProjectDomain(dam.getJiraProject() == null ? "" : dam.getJiraProject().getDomain());
                    am.setJiraProjectPlatform(dam.getJiraProject() == null ? "" : dam.getJiraProject().getPlatform());
                    sc.saving(am);
                }
                dataManager.save(sc);
                return dams.length;
            } catch (Exception e) {
                saveLogApp("MainProcess::getActuals", TypeMsg.ERROR, "Error", e.toString());
                return 0;
            }
        });
        return result;
    }

    @ManagedOperation
    private void connectWorklogs(String finMonth) {
        systemAuthenticator.withUser("admin", () -> {
            SaveContext sc;
            // connect the new worklogs to objects (IPRB and Teams)
            // 1. get the list of worklogs to process
            List<Worklog> worklogs = dataManager.load(Worklog.class).query("select e from Worklog e where e.finMonth = :finMonth").parameter("finMonth", finMonth).list();
            // 2. proceed with the connection to teams
            List<Team> teams = dataManager.load(Team.class).query("select e from Team e").list();
            List<String> ids = worklogs.stream().map(e -> e.getTeamRef().toString()).distinct().collect(Collectors.toList());
            for (String id : ids) {
                List<Worklog> pack = worklogs.stream().filter(e -> e.getTeamRef().toString().equals(id)).collect(Collectors.toList());
                Optional<Team> target = teams.stream().filter(e -> id.equals(e.getSourceID())).findFirst();

                if (target.isPresent()) {
                    sc = new SaveContext();
                    for (Worklog worklog : pack) {
                        worklog.setTeam(target.get());
                        sc.saving(worklog);
                    }
                    dataManager.save(sc);
                } else {
                    saveLogApp("MainProcess", TypeMsg.WARNING, "Missing Team while connecting Worklogs", id);
                }
            }
            // 3. proceed with the connection to IPRB
            List<IPRB> iprbs = dataManager.load(IPRB.class).query("select e from IPRB e").list();
            ids = worklogs.stream().map(Worklog::getIprbRef).filter(iprbRef -> !"".equals(iprbRef)).distinct().collect(Collectors.toList());
            for (String id : ids) {
                List<Worklog> pack = worklogs.stream().filter(e -> id.equals(e.getIprbRef())).collect(Collectors.toList());
                Optional<IPRB> target = iprbs.stream().filter(e -> id.replace(" ", "").equals(e.getReference().replace(" ", ""))).findFirst();
                if (target.isPresent()) {
                    sc = new SaveContext();
                    for (Worklog worklog : pack) {
                        worklog.setIprb(target.get());
                        sc.saving(worklog);
                    }
                    dataManager.save(sc);
                } else {
                    saveLogApp("MainProcess", TypeMsg.WARNING, "Missing IPRB while connecting Worklogs", id);
                }
            }
            saveLogApp("MainProcess", TypeMsg.INFO, "End of connection of worklogs", null);
            return null;
        });
    }

    @ManagedOperation
    private void connectActuals(String finMonth) {
        systemAuthenticator.withUser("admin", () -> {
            SaveContext sc;
            // connect the new actuals to objects: IPRB and Team
            // 1. get the list of actuals to process
            List<Actual> actuals = dataManager.load(Actual.class).query("select e from Actual e where e.finMonth = :finMonth").parameter("finMonth", finMonth).list();
            // 2. process with the connection to teams
            List<Team> teams = dataManager.load(Team.class).query("select e from Team e").list();
            List<Integer> ids = actuals.stream().map(Actual::getJiraTeamRef).collect(Collectors.toList());
            for (Integer id : ids) {
                List<Actual> pack = actuals.stream().filter(e -> e.getJiraTeamRef().equals(id)).collect(Collectors.toList());
                Optional<Team> target = teams.stream().filter(e -> id.toString().equals(e.getSourceID())).findFirst();
                if (target.isPresent()) {
                    sc = new SaveContext();
                    for (Actual actual : pack) {
                        actual.setTeam(target.get());
                        sc.saving(actual);
                    }
                    dataManager.save(sc);
                } else {
                    saveLogApp("MainProcess", TypeMsg.WARNING, "Missing Team while connecting Actual", id.toString());
                }
            }
            // 3. proceed with the connection to IPRB
            List<IPRB> iprbs = dataManager.load(IPRB.class).query("select e from IPRB e").list();
            List<String> idss = actuals.stream().map(Actual::getIprbRef).filter(iprbRef -> !"".equals(iprbRef)).distinct().collect(Collectors.toList());
            for (String id : idss) {
                List<Actual> pack = actuals.stream().filter(e -> id.equals(e.getIprbRef())).collect(Collectors.toList());
                Optional<IPRB> target = iprbs.stream().filter(e -> id.replace(" ", "").equals(e.getReference().replace(" ", ""))).findFirst();
                if (target.isPresent()) {
                    sc = new SaveContext();
                    for (Actual actual : pack) {
                        actual.setIprb(target.get());
                        sc.saving(actual);
                    }
                    dataManager.save(sc);
                } else {
                    saveLogApp("MainProcess", TypeMsg.WARNING, "Missing IPRB while connecting Actual", id);
                }
            }
            saveLogApp("MainProcess", TypeMsg.INFO, "End of connection of Actuals", null);
            return null;
        });
    }


    // *** logs management
    @ManagedOperation
    private void saveLogApp(String context, TypeMsg typeMessage, String message, String values) {
        systemAuthenticator.withUser("admin", () -> {
            LogApp logApp = dataManager.create(LogApp.class);
            logApp.setContext(context);
            logApp.setTypeMsg(typeMessage);
            logApp.setMessage(message);
            logApp.setValues(values);
            logApp.setTimeStamp(LocalDateTime.now());
            dataManager.save(logApp);
            return null;
        });
    }

    @ManagedOperation
    public void fireLogs() {
        systemAuthenticator.withUser("admin", () -> {
            // send a mail with the content of LogApp
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm");
            // Excel header
            String[] columns = {"Context", "Type", "Message", "Values", "TimeStamp"};
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Boox Feedback Summary");
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            Row Title = sheet.createRow(0);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
            Cell celltitle = Title.createCell(0);
            Font titlefont = workbook.createFont();
            titlefont.setBold(true);
            titlefont.setFontHeightInPoints((short) 14);
            titlefont.setColor(IndexedColors.LIGHT_BLUE.getIndex());
            CellStyle titleCellStyle = workbook.createCellStyle();
            titleCellStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCellStyle.setFont(titlefont);
            celltitle.setCellValue("List of Error / Warning messages");
            celltitle.setCellStyle(titleCellStyle);
            // Excel first row
            Row headerRow = sheet.createRow(1);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }
            // create the other lines
            int rowNum = 2;
            List<LogApp> logApps = dataManager.load(LogApp.class)
                    .query("select e from LogApp e")
                    .list();
            for (LogApp logApp : logApps) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(logApp.getContext());
                row.createCell(1).setCellValue(logApp.getTypeMsg().getId());
                row.createCell(2).setCellValue(logApp.getMessage());
                row.createCell(3).setCellValue(logApp.getValues());
                row.createCell(4).setCellValue(logApp.getTimeStamp().format(formatter));
            }
            // Resize all columns to fit the content size
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            // write doc on disk
            // Write the output to a file
            // FileOutputStream fileOut = new FileOutputStream("..\\backup_docs\\OCH Dummy.xlsx");
            // workbook.write(fileOut);
            // fileOut.close();

            // Write the output to a file
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                workbook.write(bos); // write excel data to a byte array
                //DataSource fds = new ByteArrayDataSource(bos.toByteArray(), "application/vnd.ms-excel");
                String fileName = "Boox_Report_" + (new SimpleDateFormat("yyyy-MM-dd")).format(new Date()) + ".xlsx";
                EmailAttachment xls = new EmailAttachment(bos.toByteArray(), fileName);
                // gets the recipients
                Token token = dataManager.load(Token.class).query("select e from Token e").one();
                if (token.getRecipients() != null) {
                    EmailInfo emi = EmailInfoBuilder.create()
                            .setAddresses(token.getRecipients())
                            .setSubject("Boox Daily Report.")
                            .setBody("Please find the report attached.")
                            .setBodyContentType("text/plain; charset=UTF-8")
                            .setAttachments(xls)
                            .build();
                    emailer.sendEmail(emi);
                    flushLogs();
                }
            } catch (IOException e) {
                saveLogApp("Scheduler", TypeMsg.ERROR, "Can't write Excel attachment", e.toString());
            } catch (EmailException e) {
                saveLogApp("MainProcess::fireLogs", TypeMsg.ERROR,"Error", e.getMessage());
            }
            // Create the email attachment
            return null;
        });


    }

    @Authenticated
    @ManagedOperation
    private void flushLogs() {
        systemAuthenticator.withUser("admin", () -> {
            // delete the existing LogApp
            List<LogApp> logApps = dataManager.load(LogApp.class).all().list();
            dataManager.remove(logApps);
            return null;
        });
    }

    // *** JIRA functions -> get the teams
    private String getJiraToken() {
        if (jiraToken == null) {
            // reads the token
            try {
                jiraToken = dataManager.load(Token.class).query("select e from Token e").one().getToken();
            } catch (Exception e) {
                log.error("MPB Error when getting token");
            }
        }
        return jiraToken;
    }

    @ManagedOperation
    public void getJiraTeams() {
        systemAuthenticator.withUser("admin", () -> {
            saveLogApp("Scheduler", TypeMsg.INFO, "Jira Team import starting", "");
            StringBuilder msg = new StringBuilder();
            List<Team> teams = dataManager.load(Team.class).query("select e from Team e").list();
            String jiraReply = getJSON("https://jira.techno.ingenico.com/rest/tempo-teams/2/team/");
            JTeam[] jTeams = gson.fromJson(jiraReply, JTeam[].class);
            SaveContext saveContext = new SaveContext();
            for (JTeam jTeam : jTeams) {
                Optional<Team> target = teams.stream().filter(e -> jTeam.getId().toString().equals(e.getSourceID())).findFirst();
                Team cTeam;
                if (target.isPresent()) {
                    cTeam = target.get();
                } else {
                    cTeam = dataManager.create(Team.class);
                    cTeam.setSourceID(jTeam.getId().toString());
                    cTeam.setName("NEW " + jTeam.getId());
                    cTeam.setFullName("NEW " + jTeam.getId());
                    cTeam.setSelectable(false);
                    cTeam.setEnabled(true);
                    msg.append("\nNew team created [").append(jTeam.getId()).append("] - ").append(jTeam.getName());
                }
                cTeam.setSourceName(jTeam.getName());
                cTeam.setSource(Source.IJ);
                saveContext.saving(cTeam);
            }
            dataManager.save(saveContext);
            if (!"".equals(msg.toString())) {
                EmailInfo emailInfo = EmailInfoBuilder.create("olivier.choquet@ingenico.com", "Boox : New team detection", msg.toString())
                        .build();
                try {
                    emailer.sendEmail(emailInfo);
                } catch (EmailException e) {
                    log.info("MPB Error sending mail " + e.getMessage());
                }
            }
            saveLogApp("Scheduler", TypeMsg.INFO, "Jira Team import finishing", "");
            return null;
        });

    }

    private String getJSON(String url) {
        String answer = null;
        URL jiraURL;
        try {
            jiraURL = new URL(url);
            HttpURLConnection connectionIssue = (HttpURLConnection) jiraURL.openConnection();
            connectionIssue.setRequestMethod("GET");
            connectionIssue.setRequestProperty("Accept", "*/*");
            connectionIssue.setRequestProperty("Content-Type", "application/json");
            connectionIssue.setDoOutput(false);
            connectionIssue.setDoInput(true);
            // connection with token
            connectionIssue.setRequestProperty("Authorization", "Bearer " + getJiraToken());
            connectionIssue.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(connectionIssue.getInputStream(), StandardCharsets.UTF_8));
            answer = in.readLine();
            in.close();
            connectionIssue.disconnect();
        } catch (Exception e) {
            log.error("MPB Error when trying to read Jira: " + url);
        }
        return answer;
    }

    private String codifCategory(String jiraCategory)  {
        // re codification of the category to match with the simpler set of values requested by SvS
        if (jiraCategory==null) return "Non-Project";
        switch (jiraCategory.toLowerCase()) {
            case "custom":
            case "roadmap":
            case "regulatory / compliancy":
            case "conexflow":
                return "Product";
            case "maintenance":
            case "maintenance / compliance / support":
            case "incidents":
                return "Maintenance & Incidents";
            case "ooo":
                return "OoO";
            case "technical refactoring & debt reduction":
            case "technical debt":
                return "Tech Debt";
            case "not_found":
            case "process enhancement":
            case "management & coordination":
            case "none":
            case "not managed":
            case "internal":
            case "non-productive":
                return "Non-Project";
            default:
                return "?? " + jiraCategory;

        }
    }


    // *** Time utility Function

    // ***** DATE MANAGEMENT METHODS
    final int START_FINANCE_MONTH = 24;
    public SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");

    public Date getFinanceMonthStart(Date date) {
        // last previous 24th of a month
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (cal.get(Calendar.DATE) < START_FINANCE_MONTH) {
            cal.add(Calendar.MONTH, -1);
        }
        cal.set(Calendar.DATE, START_FINANCE_MONTH);
        return cal.getTime();
    }

    public String getFinancialMonth(Date date) {
        // day >= 24 ?  next month : current month
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return "fin" + (cal.get(Calendar.DATE) >= START_FINANCE_MONTH ? monthToString(addTime(date, 1, "MONTH")) : monthToString(date));
    }

    public String monthToString(Date ref) {
        // returns the provided date 'ref' under the common Dumbo's month format
        return sdf.format(giveMonthStart(ref));
    }
    public Date giveMonthStart(Date ref) {
        // returns the first day of the month the provided date 'ref' belongs to
        Calendar cal = Calendar.getInstance();
        cal.setTime(ref);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DATE, 1);
        return cal.getTime();
    }
    public Date addTime(Date ref, int delta, String unit) {
        // adds 'delta' 'unit' to the provided date 'ref'
        // managed units:  DAY / MONTH / YEAR
        Calendar cal = Calendar.getInstance();
        cal.setTime(ref);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        switch (unit) {
            case "DAY":
                cal.add(Calendar.DATE, delta);
                break;
            case "MONTH":
                cal.add(Calendar.MONTH, delta);
                break;
            case "YEAR":
                cal.add(Calendar.YEAR, delta);
                break;
        }
        return cal.getTime();
    }

}