package com.choqnet.budget.screen.popups.upload_demand;

import com.choqnet.budget.entity.*;
import com.choqnet.budget.entity.datalists.Category;
import com.choqnet.budget.entity.datalists.Priority;
import com.choqnet.budget.entity.datalists.TShirt;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.FileStorageUploadField;
import io.jmix.ui.component.SingleFileUploadField;
import io.jmix.ui.component.TextArea;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import io.jmix.ui.upload.TemporaryStorage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@UiController("UploadDemand")
@UiDescriptor("upload-demand.xml")
public class UploadDemand extends Screen {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UploadDemand.class);
    List<Detail> details;
    List<Demand> demands;
    List<Expense> expenses;
    List<Team> teams;
    List<OnePager> onePagers;
    List<IPRB> iprbs;
    private Budget budget;
    @Autowired
    private Button btnProcess;
    @Autowired
    private TextArea<String> txtResult;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private FileStorageUploadField upload;
    @Autowired
    private TemporaryStorage temporaryStorage;
    @Autowired
    private Notifications notifications;

    @Subscribe
    public void onInit(InitEvent event) {
        btnProcess.setVisible(false);
        txtResult.setValue("Data must be in the tabs [Demand MD] and [Extra Demand in €]");
        teams = dataManager.load(Team.class).all().list();
        onePagers = dataManager.load(OnePager.class).all().list();
        iprbs = dataManager.load(IPRB.class).all().list();
        // euroDemands = dataManager.load(EuroDemand.class).all().list();
    }

    @Subscribe("upload")
    public void onUploadAfterValueClear(SingleFileUploadField.AfterValueClearEvent event) {
        btnProcess.setVisible(false);
    }

    @Subscribe("upload")
    public void onUploadFileUploadSucceed(SingleFileUploadField.FileUploadSucceedEvent event) {
        btnProcess.setVisible(true);
    }

    @Subscribe("btnProcess")
    public void onBtnProcessClick(Button.ClickEvent event) {
        SaveContext sc = new SaveContext();
        // process the file
        try {
            UUID fileID = upload.getFileId();
            File file = temporaryStorage.getFile(fileID);
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
            // *** DETAILS MANAGEMENT (DEMAND MD)
            XSSFSheet sht = wb.getSheet("Demand MD");
            if (sht == null) {
                sendMessage("This Excel doesn't get a tab called Demand MD");
                temporaryStorage.deleteFile(fileID);
                return;
            }
            boolean header = true;
            for (Row row : sht) {
                if (header) {
                    header = false;
                    if (!handleHeader(row)) {
                        sendMessage("This Excel file doesn't get the right column setup.");
                        temporaryStorage.deleteFile(fileID);
                        return;
                    }
                } else {
                    Detail detail = handleDemandRow(row);
                    if (detail != null) {
                        sc.saving(detail);
                    }
                }
            }

            // *** EURODEMAND MANAGEMENT (EXTRA DEMAND IN €)
            sht = wb.getSheet("Extra Demand in €");
            if (sht == null) {
                sendMessage("This Excel doesn't get a tab called Extra Demand in €");
                temporaryStorage.deleteFile(fileID);
                return;
            }
            header = true;
            for (Row row : sht) {
                if (header) {
                    header = false;
                    if (!handleHeader(row)) {
                        sendMessage("This Excel file doesn't get the right column setup.");
                        temporaryStorage.deleteFile(fileID);
                        return;
                    }
                } else {
                    Expense expense = handleExpenseRow(row);
                    if (expense != null) {
                        sc.saving(expense);
                    }
                }
            }
            dataManager.save(sc);
            notifications.create()
                    .withType(Notifications.NotificationType.SYSTEM)
                    .withDescription("Import completed.")
                    .show();
            txtResult.setValue("Import completed");

        } catch (Exception e) {
            txtResult.setValue("Problem:\n" + e);
        }
    }


    private boolean handleHeader(Row row) {
        // test the tab's layout
        return true;
    }

    private Detail handleDemandRow(Row row) {
        // get the iprb reference and get the demand
        String iprbRef = readCell(row.getCell(1));
        if (!"".equals(iprbRef)) {
            Demand demand = demands.stream()
                    .filter(e -> iprbRef.equals(e.getIprb().getReference()))
                    .findFirst()
                    .orElse(null);
            if (demand == null) {
                Optional<IPRB> iprb = iprbs.stream()
                        .filter(e -> iprbRef.equals(e.getReference()))
                        .findFirst();
                if (iprb.isPresent()) {
                    demand = dataManager.create(Demand.class);
                    demand.setIprb(iprb.get());
                    demand.setBudget(budget);
                    demand = dataManager.save(demand);
                    demands.add(demand);
                } else {
                    log.info(">>> Demand MD WARNING, no IPRB for " + iprbRef);
                    return null;
                }
            }
            Detail detail = dataManager.create(Detail.class);
            detail.setDemand(demand);
            detail.setRoadmap(readCell(row.getCell(2)));
            detail.setDetail(readCell(row.getCell(3)));
            String teamName = readCell(row.getCell(4));
            Team team = teams.stream()
                    .filter(e -> teamName.equals(e.getName()))
                    .findFirst()
                    .orElse(null);
            detail.setTeam(team);
            // override if total year doesn't match with TShirt
            String size = readCell(row.getCell(5)).toUpperCase(Locale.ROOT);
            double effortTS = 0;
            double effort = getDouble(readCell(row.getCell(6)));
            TShirt targetTS = TShirt.FREE;
            double effort1 = getDouble(readCell(row.getCell(9)));
            double effort2 = getDouble(readCell(row.getCell(10)));
            double effort3 = getDouble(readCell(row.getCell(11)));
            double effort4 = getDouble(readCell(row.getCell(12)));
            // debug
            if ("ACC - 0113".equals(iprbRef)) {
                log.info(readCell(row.getCell(6)));
                log.info("fpound");
            }

            switch (size) {
                case "XXXL":
                    targetTS= (TShirt.XXXL);
                    effortTS = 800.0;
                    break;
                case "XXL":
                    targetTS= (TShirt.XXL);
                    effortTS = 400.0;
                    break;
                case "XL":
                    targetTS= (TShirt.XL);
                    effortTS = 300.0;
                    break;
                case "L":
                    targetTS= (TShirt.L);
                    effortTS = 200.0;
                    break;
                case "M":
                    targetTS= (TShirt.M);
                    effortTS = 100.0;
                    break;
                case "S":
                    targetTS= (TShirt.S);
                    effortTS = 50.0;
                    break;
                case "XS":
                    targetTS= (TShirt.XS);
                    effortTS = 20.0;
                    break;
                case "FREE":
                default:
                    targetTS= (TShirt.FREE);
                    break;
            }
            if (Double.compare(effort, effortTS)==0) {
                // TShirt drives the effort
                detail.setMdY(effortTS);
                detail.setMdQ1(effort1);
                detail.setMdQ2(effort2);
                detail.setMdQ3(effort3);
                detail.setMdQ4(effort4);
                detail.setTShirt(targetTS);
            } else {
                // efforts override the TShirt size
                detail.setMdY(effort);
                detail.setMdQ1(effort1);
                detail.setMdQ2(effort2);
                detail.setMdQ3(effort3);
                detail.setMdQ4(effort4);
                detail.setTShirt(TShirt.FREE);
            }
            String priority = readCell(row.getCell(13));
            priority = "".equals(priority) ? "P5" : priority;
            detail.setPriority(Priority.valueOf(priority));
            detail.setTopic(readCell(row.getCell(15)));
            String category = readCell(row.getCell(16));
            switch (category) {
                case "Product":
                    detail.setCategory(Category.ROADMAP);
                    break;
                case "Tech. Debt":
                    detail.setCategory(Category.TECH_DEBT);
                    break;
                case "Synergies":
                    detail.setCategory(Category.SYNERGIES);
                    break;
                case "Maintenance & Incidents":
                    detail.setCategory(Category.MAINTENANCE);
                    break;
                case "Marco Polo":
                    detail.setCategory(Category.MARCOPOLO);
                    break;
            }
            String onePagerName = readCell(row.getCell(18));
            OnePager onePager = onePagers.stream()
                    .filter(e -> onePagerName.equals(e.getValue()))
                    .findFirst()
                    .orElse(null);
            detail.setOnePager(onePager);
            return detail;
        } else {
            return null;
        }

    }

    private Expense handleExpenseRow(Row row) {
        String iprbRef = readCell(row.getCell(0));
        if (!"".equals(iprbRef)) {
            Demand demand = demands.stream()
                    .filter(e -> iprbRef.equals(e.getIprb().getReference()))
                    .findFirst()
                    .orElse(null);
            if (demand == null) {
                Optional<IPRB> iprb = iprbs.stream()
                        .filter(e -> iprbRef.equals(e.getReference()))
                        .findFirst();
                if (iprb.isPresent()) {
                    demand = dataManager.create(Demand.class);
                    demand.setIprb(iprb.get());
                    demand.setBudget(budget);
                    demand = dataManager.save(demand);
                    demands.add(demand);
                } else {
                    log.info(">>> Demand €€ WARNING, no IPRB for " + iprbRef);
                    return null;
                }
            }
            Expense expense = dataManager.create(Expense.class);
            expense.setDemand(demand);
            expense.setName(readCell(row.getCell(2)));
            expense.setAmountKEuro(getDouble(readCell(row.getCell(3))) + getDouble(readCell(row.getCell(4))));
            expense.setCapex(!"".equals(readCell(row.getCell(3))));
            expense.setAccepted(true);
            return expense;
        } else {
            return null;
        }
    }

    @Subscribe("btnClose")
    public void onBtnCloseClick(Button.ClickEvent event) {
        // close the current window
        closeWithDefaultAction();
    }

    // *** Data functions
    public void setBudget(Budget budget) {
        this.budget = budget;
        demands = dataManager.load(Demand.class)
                .query("select e from Demand e where e.budget = :budget")
                .parameter("budget", budget)
                .list();
        details = dataManager.load(Detail.class)
                .query("select e from Detail e where e.demand.budget = :budget")
                .parameter("budget", budget)
                .list();
        dataManager.remove(details);
        expenses = dataManager.load(Expense.class)
                .query("select e from Expense e where e.demand.budget = :budget")
                .parameter("budget", budget)
                .list();
        dataManager.remove(expenses);
    }

    // *** Utility Functions
    private String readCell(Cell c) {
        if (c == null) {
            return "";
        }
        switch (c.getCellType()) {
            case NUMERIC:
                return String.valueOf(c.getNumericCellValue());
            case STRING:
                return c.getStringCellValue();
            case FORMULA:
                switch (c.getCachedFormulaResultType().toString()) {
                    case "STRING":
                        return c.getStringCellValue();
                    case "NUMERIC":
                        return new DecimalFormat("#.0#").format(c.getNumericCellValue());
                    default:
                        log.info(">>> UploadIPRB (warning message) unknown type: " + c.getCachedFormulaResultType().toString());
                        return c.getCachedFormulaResultType().toString();
                }
            case BOOLEAN:
                return (c.getBooleanCellValue() ? "yes" : "no");
            case ERROR:
                return "Err";
            case _NONE:
            case BLANK:
                return "";
            default:
                return "???";
        }
    }

    private double getDouble(String str) {
        try {
            return Double.parseDouble(str.replace(",","."));
        } catch (NumberFormatException e) {
            return 0.00;
        }
    }

    private void sendMessage(String message) {
        notifications.create()
                .withCaption("System Message")
                .withDescription(message)
                .withType(Notifications.NotificationType.ERROR)
                .show();
    }
}