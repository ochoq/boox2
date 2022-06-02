package com.choqnet.budget.screen.popups.uploadactuals;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.XLUtils;
import com.choqnet.budget.entity.IPRB;
import com.choqnet.budget.entity.Team;
import com.choqnet.budget.entity.XLActual;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import io.jmix.ui.upload.TemporaryStorage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@UiController("UploadActuals")
@UiDescriptor("upload-actuals.xml")
public class UploadActuals extends Screen {
    private XSSFSheet sht;
    private List<Row> rows = new ArrayList<>();
    private List<String> createdIPRB = new ArrayList<>();
    private List<String> missingTeams = new ArrayList<>();
    private List<Team> teams;
    private List<IPRB> iprbs;

    @Autowired
    private Button btnCheck;
    @Autowired
    private Button btnProcess;


    @Autowired
    private Notifications notifications;
    @Autowired
    private TextArea<String> txtResult;
    @Autowired
    private FileStorageUploadField upload;
    @Autowired
    private TemporaryStorage temporaryStorage;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private XLUtils xLUtils;
    @Autowired
    private UtilBean utilBean;
    @Autowired
    private ComboBox<String> cmbYear;
    @Autowired
    private ComboBox<String> cmbQuarter;


    @Subscribe
    public void onInit(InitEvent event) {
        // UI setup
        btnCheck.setEnabled(false);
        btnProcess.setEnabled(false);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        List<String> years = Arrays.asList(sdf.format(utilBean.addTime(new Date(),-1, "YEAR")), sdf.format(new Date()),sdf.format(utilBean.addTime(new Date(),1,"YEAR")));
        cmbYear.setOptionsList(years);
        cmbYear.setValue(sdf.format(new Date()));
        cmbQuarter.setOptionsList(Arrays.asList("Q1", "Q2", "Q3", "Q4"));
    }



    // UI UPLOAD
    @Subscribe("upload")
    public void onUploadAfterValueClear(SingleFileUploadField.AfterValueClearEvent event) {
        btnProcess.setEnabled(false);
        btnCheck.setEnabled(false);
    }

    @Subscribe("upload")
    public void onUploadFileUploadSucceed(SingleFileUploadField.FileUploadSucceedEvent event) {
        btnProcess.setEnabled(false);
        btnCheck.setEnabled(true);
    }



    // UI BUTTONS
    @Subscribe("btnCheck")
    public void onBtnCheckClick(Button.ClickEvent event) {
        try {
            UUID fileID = upload.getFileId();
            File file = temporaryStorage.getFile(fileID);
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
            sht = wb.getSheetAt(0);
            if (checkData()) {
                btnProcess.setEnabled(true);
            } else {
                btnProcess.setEnabled(false);
            }
        } catch (Exception e) {
            txtResult.setValue("Problem:\n" + e.toString());
        }
    }

    @Subscribe("btnProcess")
    public void onBtnProcessClick(Button.ClickEvent event) {
        // checks that quarter and year are well defined
        if (cmbYear.getValue()==null || cmbQuarter.getValue()==null) {
            notifications.create().withType(Notifications.NotificationType.WARNING).withDescription("Please set a year and a quarter").show();
            return;
        }
        // purge and create XLActuals
        List<XLActual> xlActuals = dataManager.load(XLActual.class)
                .query("select e from XLActuals e where e.year = :year and e.quarter = :quarter")
                .parameter("year", cmbYear.getValue())
                .parameter("quarter", cmbQuarter.getValue())
                .list();
        dataManager.remove(xlActuals);
        SaveContext sc = new SaveContext();
        for (Row row: rows) {
            String tmpTeam = xLUtils.readCell(row.getCell(14)).replace("\n", "").replace("\r", "");

            Optional<Team> teamTarget = teams.stream().filter(e -> tmpTeam.equals(e.getName())).findFirst();
            String tmpIPRB = xLUtils.readCell(row.getCell(2)).replace(" ","");

            Optional<IPRB> iprbTarget = iprbs.stream().filter(e -> tmpIPRB.equals(e.getReference())).findFirst();
            if (teamTarget.isPresent() && iprbTarget.isPresent()) {
                XLActual xlActual = dataManager.create(XLActual.class);
                xlActual.setIprb(iprbTarget.get());
                xlActual.setTeam(teamTarget.get());
                try {
                    xlActual.setEffort(Double.parseDouble(xLUtils.readCell(row.getCell(15)).replace(",",".")));
                    xlActual.setBudgetCost(Double.parseDouble(xLUtils.readCell(row.getCell(11)).replace(",",".")));
                } catch (NumberFormatException e) {
                    xlActual.setEffort(0.0);
                    xlActual.setBudgetCost(0.0);
                }
                xlActual.setQuarter(cmbQuarter.getValue());
                xlActual.setYear(cmbYear.getValue());
                if (xlActual.getEffort()>0.0) {
                    sc.saving(xlActual);
                }
            }
        }
        dataManager.save(sc);
        notifications.create().withCaption("Excel Actuals Import").withDescription("Import successful of " + sc.getEntitiesToSave().size() + " items").show();
    }

    @Subscribe("btnClose")
    public void onBtnCloseClick(Button.ClickEvent event) {
        // close the current window
        closeWithDefaultAction();
    }

    // BUSINESS FUNCTIONS
    private boolean checkData() {
        /**
         * Fetches the provided Excel file and
         * (i) creates the missing IPRBs
         * (ii) checks that all the teams are known
         * returns true if no teams are missing, else false
         */
        loadExcelFile();
        checkAndCreateIPRB();
        checkAndAlertTeams();
        txtResult.setValue("IPRB Created:\n" + String.join(", ", createdIPRB) +  "\n\nTeams missing:\n" + String.join(", ", missingTeams));
        return missingTeams.size()==0;
    }

    private void checkAndAlertTeams() {
        teams = dataManager.load(Team.class).query("select e from Team e where e.source is null or e.source='Manual'").list();
        for (Row row: rows) {
            String team = xLUtils.readCell(row.getCell(14)).replace("\n", "").replace("\r", "");
            if (!"".equals(team) && teams.stream().noneMatch(e -> team.equals(e.getName()))) {
                missingTeams.add(team);
            }
        }
        missingTeams = missingTeams.stream().distinct().collect(Collectors.toList());
    }

    private void checkAndCreateIPRB() {
        iprbs = dataManager.load(IPRB.class).all().list();
        for (Row row: rows) {
            String iprbValue = xLUtils.readCell(row.getCell(2)).replace(" ","");
            if (!"".equals(iprbValue) && iprbs.stream().noneMatch(e -> iprbValue.equals(e.getReference()))) {
                createdIPRB.add(iprbValue);
                IPRB iprb = dataManager.create(IPRB.class);
                iprb.setReference(iprbValue);
                iprb.setName(xLUtils.readCell(row.getCell(3)));
                iprb = dataManager.save(iprb);
                iprbs.add(iprb);
            }
        }
        createdIPRB = createdIPRB.stream().distinct().collect(Collectors.toList());
    }

    private void loadExcelFile()  {
        boolean first = true;
        for (Row row: sht) {
            if (!first) rows.add(row);
            first= false;

        }
    }
}