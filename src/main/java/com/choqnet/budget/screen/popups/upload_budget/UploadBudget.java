package com.choqnet.budget.screen.popups.upload_budget;

import com.choqnet.budget.XLUtils;
import com.choqnet.budget.entity.Budget;
import com.choqnet.budget.entity.Template;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.*;
import io.jmix.ui.download.DownloadDataProvider;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import io.jmix.ui.upload.TemporaryStorage;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@UiController("UploadBudget")
@UiDescriptor("upload-budget.xml")
@DialogMode(width = "600px", height = "700px")
public class UploadBudget extends Screen {
    private static final Logger log = LoggerFactory.getLogger(UploadBudget.class);
    @Autowired
    private Button btnProcess;
    @Autowired
    private TextArea<String> txtResult;
    @Autowired
    private Notifications notifications;
    @Autowired
    private TemporaryStorage temporaryStorage;
    @Autowired
    private FileStorageUploadField upload;
    @Autowired
    private Downloader downloader;
    @Autowired
    private XLUtils xLUtils;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ComboBox<Budget> cmbBudget;

    private XSSFWorkbook wb;
    private XSSFSheet newProjects, onGoingProjects, bauProjects;
    private XSSFSheet cSht; // current sheet
    CellStyle cellStyle;
    @Autowired
    private CollectionContainer<Template> templatesDc;
    @Autowired
    private InstanceContainer<Template> templateDc;
    @Autowired
    private FileStorage fileStorage;


    // *** Initialisations

    @Subscribe
    public void onInit(InitEvent event) {
        // UI setup
        btnProcess.setVisible(true);
        txtResult.setValue("Please provide the MS-Acceptance 2022 budget ");
        List<Budget> budgets = dataManager.load(Budget.class)
                .query("select e from Budget e order by e.year desc, e.name asc")
                .list();
        cmbBudget.setOptionsList(budgets);
        Optional<Budget> prefBudget = budgets.stream().filter(Budget::getPreferred).findFirst();
        prefBudget.ifPresent(value -> cmbBudget.setValue(value));

    }


    // *** UI functions

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
        Template template = dataManager.load(Template.class)
                .query("select e from Template e where e.code = 'MSBUDGET'")
                .list().get(0);

        try {
            wb = new XSSFWorkbook(fileStorage.openStream(template.getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // get the file from th uploader

       // UUID fileID = upload.getFileId();
        //File file = temporaryStorage.getFile(fileID);
        try {

            //FileInputStream fis = new FileInputStream(file);
            //wb = new XSSFWorkbook(fis);
            // file processing
            wb = xLUtils.processFile(wb, cmbBudget.getValue());
            //fis.close();
            File directory = new File("tmp");
            directory.mkdir();
            FileOutputStream outFile = new FileOutputStream("tmp\\tmp_budget.xlsx");
            wb.write(outFile);
            outFile.close();

            String filePath = "tmp\\tmp_budget.xlsx";
            // file to byte[], Path
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            // file to byte[], File -> Path
            File file2 = new File(filePath);
            byte[] bytes2 = Files.readAllBytes(file2.toPath());
            downloader.download(bytes2, "test.xlsx");
            //temporaryStorage.deleteFile(fileID);


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("PLanté");
        }

    }


    @Subscribe("btnClose")
    public void onBtnCloseClick(Button.ClickEvent event) {
        // close the current window
        closeWithDefaultAction();
    }

    // *** Utilities
    private void sendMessage(String message) {
        notifications.create()
                .withCaption("System Message")
                .withDescription(message)
                .withType(Notifications.NotificationType.ERROR)
                .show();
    }

}