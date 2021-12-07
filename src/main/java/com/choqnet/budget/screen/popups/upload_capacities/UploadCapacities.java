package com.choqnet.budget.screen.popups.upload_capacities;

import com.choqnet.budget.entity.Budget;
import com.choqnet.budget.entity.Capacity;
import com.choqnet.budget.entity.Team;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlans;
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
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@UiController("UploadCapacities")
@UiDescriptor("upload-capacities.xml")
public class UploadCapacities extends Screen {


    private static final Logger log = LoggerFactory.getLogger(UploadCapacities.class);
    List<Capacity> capacities;
    List<Team> teams;
    String feedback = "";
    private Budget budget = null;

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
    @Autowired
    private FetchPlans fetchPlans;

    @Subscribe
    public void onInit(InitEvent event) {
        // UI setup
        btnProcess.setVisible(false);
        txtResult.setValue("Data must be in the tab [Teams]");
        capacities = dataManager.load(Capacity.class).all().list();
        //FetchPlan fp = fetchPlans.builder(Team.class).add(FetchPlan.BASE).add("parent").build();
        teams = dataManager.load(Team.class).all().fetchPlan("teams").list();
    }

    @Subscribe("upload")
    public void onUploadAfterValueClear(SingleFileUploadField.AfterValueClearEvent event) {
        // file cleared, button [Process] is removed
        btnProcess.setVisible(false);

    }

    @Subscribe("upload")
    public void onUploadFileUploadSucceed(SingleFileUploadField.FileUploadSucceedEvent event) {
        // file correctly set; button [Process] is shown
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
            XSSFSheet sht = wb.getSheet("Teams");
            if (sht == null) {
                sendMessage("This Excel file doesn't get a tab called Teams.");
                temporaryStorage.deleteFile(fileID);
                return;
            }
            boolean header = true;
            for (Row row: sht) {
                if (header) {
                    header = false;
                    if (!handleHeader(row)) {
                        sendMessage("This Excel file doesn't get the right column setup.");
                        temporaryStorage.deleteFile(fileID);
                        return;
                    }
                } else {
                    Capacity capacity = handleRow(row);
                    if (capacity != null) {
                        sc.saving(capacity);
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

    private Capacity handleRow(Row row) {
        Capacity targetCapacity;
        String value;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String sqName = readCell(row.getCell(0));
        if (!"".equals(sqName)) {

            // debug
            if ("AFC3".equals(sqName) || "AFMOB".equals(sqName) || "AFR".equals(sqName)) {
                log.info("found " + sqName );
            }

            Optional<Team> targetTeam = teams.stream().filter(e -> sqName.equals(e.getName())).findFirst();
            if (targetTeam.isPresent()) {
                targetCapacity = capacities.stream().filter(e -> targetTeam.get().equals(e.getTeam())).findFirst().orElse(dataManager.create(Capacity.class));
                targetCapacity.setTeam(targetTeam.get());
                targetCapacity.setNbWorkingDays((int) getDouble(readCell(row.getCell(3))));
                Double rate = getDouble(readCell(row.getCell(5)));
                targetCapacity.setRateQ1(rate);
                targetCapacity.setRateQ2(rate);
                targetCapacity.setRateQ3(rate);
                targetCapacity.setRateQ4(rate);
                targetCapacity.setBudget(budget);
                targetCapacity.setFteQ1(getDouble(readCell(row.getCell(6))));
                targetCapacity.setFteQ2(getDouble(readCell(row.getCell(7))));
                targetCapacity.setFteQ3(getDouble(readCell(row.getCell(8))));
                targetCapacity.setFteQ4(getDouble(readCell(row.getCell(9))));
                return targetCapacity;

            } else {
                feedback += sqName + " was not found in teams\n";
                return null;
            }
        } else {
            return null;
        }
    }

    private boolean handleHeader(Row row) {
        // test the tab's layout
        return true;
    }


    @Subscribe("btnClose")
    public void onBtnCloseClick(Button.ClickEvent event) {
        // close the current window
        closeWithDefaultAction();
    }

    // *** Data functions
    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    // *** Utility Functions
    private String readCell(Cell c) {
        if (c==null) {
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
                        return new DecimalFormat("#.0#").format( c.getNumericCellValue());
                    default:
                        log.info(">>> UploadIPRB (warning message) unknown type: " + c.getCachedFormulaResultType().toString() );
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
            return Double.parseDouble(str);
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