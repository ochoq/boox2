package com.choqnet.budget.screen.popups.upload_teams;

import com.choqnet.budget.entity.datalists.GTM;
import com.choqnet.budget.entity.Team;
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
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@UiController("UploadTeams")
@UiDescriptor("upload-teams.xml")
public class UploadTeams extends Screen {
    private static final Logger log = LoggerFactory.getLogger(UploadTeams.class);
    @Autowired
    private Button btnProcess;
    @Autowired
    private TextArea txtResult;
    @Autowired
    private FileStorageUploadField upload;
    @Autowired
    private TemporaryStorage temporaryStorage;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DataManager dataManager;

    List<Team> teams ;


    // *** UI Functions
    @Subscribe
    public void onInit(InitEvent event) {
        // UI setup
        btnProcess.setVisible(false);
        txtResult.setValue("Data must be in the tab [Teams]\n- col A: name\n- col B: source name\n- col W: IC Target\n- col X: Main GTM");
        teams = dataManager.load(Team.class).all().list();
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
        // Processes the file
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
            for (Row row : sht) {
                if (header) {
                    header = false;
                    if (!handleHeader(row)) {
                        sendMessage("This Excel file doesn't get the right column setup.");
                        temporaryStorage.deleteFile(fileID);
                        return;
                    }
                } else {
                    Team team = handleRow(row);
                    if (team != null) {
                        sc.saving(team);
                    }
                }
            }
            dataManager.save(sc);
            notifications.create()
                    .withType(Notifications.NotificationType.SYSTEM)
                    .withDescription("Import completed.")
                    .show();
        } catch (Exception e) {
            txtResult.setValue("Problem:\n" + e);
        }
    }

    @Subscribe("btnClose")
    public void onBtnCloseClick(Button.ClickEvent event) {
        // close the current window
        closeWithDefaultAction();
    }

    // *** Data Functions
    private Team handleRow(Row row) {
        // create or upodate
        Team targetTeam;
        String sqName = readCell(row.getCell(0));
        if (!"".equals(sqName)) {
            targetTeam = teams.stream().filter(o -> readCell(row.getCell(0)).equals(o.getName())).findFirst().orElse(dataManager.create(Team.class));
            targetTeam.setName(readCell(row.getCell(0)));
            targetTeam.setFullName(readCell(row.getCell(0)));
            targetTeam.setSourceName(readCell(row.getCell(1)));
            targetTeam.setIcTarget(readCell(row.getCell(22)));
            targetTeam.setUpdated(LocalDateTime.now());
            String value  = readCell(row.getCell(23));
            switch (value){
                case "eCOM":
                    targetTeam.setMainGTM(GTM.RB);
                    break;
                case "OTHER":
                    targetTeam.setMainGTM(GTM.OTH);
                    break;
                case "GSV":
                    targetTeam.setMainGTM(GTM.GSV);
                    break;
                case "DC":
                    targetTeam.setMainGTM(GTM.DC);
                    break;
                case "OCH":
                    targetTeam.setMainGTM(GTM.OCH);
                    break;
            }
            return targetTeam;
        } else {
            return null;
        }
    }

    private boolean handleHeader(Row row) {
        boolean result;
        result = "Sq Code".equals(readCell(row.getCell(0)))
                && "Sq Name".equals(readCell(row.getCell(1)))
                && "IC Mapping (auto)".equals(readCell(row.getCell(22)))
                && "GTM".equals(readCell(row.getCell(23)));
        return result;

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
                        log.info(">>> UploadTeam (warning message) unknown type: " + c.getCachedFormulaResultType().toString() );
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