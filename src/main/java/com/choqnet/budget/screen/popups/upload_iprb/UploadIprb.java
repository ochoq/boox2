package com.choqnet.budget.screen.popups.upload_iprb;

import com.choqnet.budget.communications.UserNotification;
import com.choqnet.budget.entity.IPRB;
import com.choqnet.budget.entity.datalists.*;
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
import org.springframework.context.event.EventListener;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@UiController("UploadIprb")
@UiDescriptor("upload-iprb.xml")
public class UploadIprb extends Screen {
    private static final Logger log = LoggerFactory.getLogger(UploadIprb.class);
    @Autowired
    private Button btnProcess;
    @Autowired
    private TextArea<String> txtResult;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Notifications notifications;
    @Autowired
    private FileStorageUploadField upload;
    @Autowired
    private TemporaryStorage temporaryStorage;

    List<IPRB> iprbs;

    @Subscribe
    public void onInit(InitEvent event) {
        // UI setup
        btnProcess.setVisible(false);
        txtResult.setValue("Data must be in the tab [IPRB].");
        iprbs = dataManager.load(IPRB.class).all().list();
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
            XSSFSheet sht = wb.getSheet("IPRB");
            if (sht == null) {
                sendMessage("This Excel file doesn't get a tab called IPRB.");
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
                    IPRB iprb = handleRow(row);
                    if (iprb != null) {
                        sc.saving(iprb);
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

    private IPRB handleRow(Row row) {
        IPRB targetIprb;
        String value;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String reference = readCell(row.getCell(0));
        if (!"".equals(reference)) {
            targetIprb = iprbs.stream().filter(e -> reference.equals(e.getReference())).findFirst().orElse(dataManager.create(IPRB.class));
            targetIprb.setReference(reference);
            targetIprb.setName(readCell(row.getCell(1)));
            targetIprb.setOwner(readCell(row.getCell(7)));
            value = readCell(row.getCell(8));
            if (!"".equals(value)) {
                targetIprb.setStrategicProgram(StrategicProgram.fromId(value));
            }
            value = readCell(row.getCell(9));
            if (!"".equals(value)) {
                targetIprb.setPortfolioClassification(PortfolioClassification.fromId(value));
            }
            value = readCell(row.getCell(10));
            if (!"".equals(value)) {
                targetIprb.setLegalEntity(LegalEntity.fromId(value));
            }
            value = readCell(row.getCell(11));
            if (!"".equals(value)) {
                targetIprb.setActivityType(ActivityType.fromId(value));
            }
            value = readCell(row.getCell(12));
            if (!"".equals(value)) {
                targetIprb.setNewProductIndicator(NewProductIndicator.fromId(value));
            }
            value = readCell(row.getCell(13));
            if (!"".equals(value)) {
                targetIprb.setGroupOffering(GroupOffering.fromId(value));
            }
            value = readCell(row.getCell(14));
            if (!"".equals(value)) {
                CAPI test2 = CAPI.fromId(value);
                targetIprb.setEstCAPI(CAPI.fromId(value));
            }
            value = readCell(row.getCell(15));
            if (!"".equals(value)) {
                OOI test = OOI.fromId(value);
                targetIprb.setEstOOI(OOI.fromId(value));
            }
            value = readCell(row.getCell(16));
            if (!"".equals(value)) {

                targetIprb.setStartDate(LocalDate.parse(value, dtf));
            }
            value = readCell(row.getCell(17));
            if (!"".equals(value)) {

                targetIprb.setEndDate(LocalDate.parse(value, dtf));
            }
            targetIprb.setUpdated(LocalDateTime.now());
            return targetIprb;
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