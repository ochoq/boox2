package com.choqnet.budget.screen.control_tower;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.XLUtils;
import com.choqnet.budget.app.UMsgBean;
import com.choqnet.budget.communications.UserNotification;
import com.choqnet.budget.entity.Budget;
import com.choqnet.budget.entity.Team;
import com.choqnet.budget.entity.Template;
import io.jmix.core.DataManager;
import io.jmix.core.FileStorage;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.TextArea;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.screen.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@UiController("ControlTower")
@UiDescriptor("control-tower.xml")
public class ControlTower extends Screen {
    @Autowired
    private UMsgBean umb;
    @Autowired
    private TextArea<String> txtMessage;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private ComboBox<Budget> cmbBudget;
    @Autowired
    private FileStorage fileStorage;

    private XSSFWorkbook wb;
    @Autowired
    private XLUtils xLUtils;
    @Autowired
    private Downloader downloader;
    @Autowired
    private UtilBean utilBean;


    // *** Initialisations
    @Subscribe
    public void onInit(InitEvent event) {
        List<Budget> budgets = dataManager.load(Budget.class)
                .query("select e from Budget e order by e.year desc, e.name asc")
                .list();
        cmbBudget.setOptionsList(budgets);
        Optional<Budget> prefBudget = budgets.stream().filter(Budget::getPreferred).findFirst();
        prefBudget.ifPresent(value -> cmbBudget.setValue(value));
    }


    // *** communications


    // *** utilities
    @Subscribe("btnSendMessage")
    public void onBtnSendMessageClick(Button.ClickEvent event) {
        umb.publishEvent(new UserNotification(this, txtMessage.getValue()));
    }

    @Subscribe("btnExportBudget")
    public void onBtnExportBudgetClick(Button.ClickEvent event) {
        // launches the export of the budget Excel file
        Template template = dataManager.load(Template.class)
                .query("select e from Template e where e.code = 'MSBUDGET'")
                .list().get(0);

        try {
            wb = new XSSFWorkbook(fileStorage.openStream(template.getFile()));
            wb = xLUtils.processFile(wb, cmbBudget.getValue());
            File directory = new File("tmp");
            directory.mkdir();
            FileOutputStream outFile = new FileOutputStream("tmp\\tmp_budget.xlsx");
            wb.write(outFile);
            outFile.close();

            String filePath = "tmp\\tmp_budget.xlsx";
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            // file to byte[], File -> Path
            File file2 = new File(filePath);
            byte[] bytes2 = Files.readAllBytes(file2.toPath());
            downloader.download(bytes2, "MS Budget - Acceptance.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe("cleanTeams")
    public void onCleanTeamsClick(Button.ClickEvent event) {
        //
        List<Team> zeroTeam = dataManager.load(Team.class)
                .query("select e from Team e where e.parent is null")
                .list();
        System.out.println("liste " + zeroTeam.size());
        for (Team team: zeroTeam) {
            utilBean.HierarchicalData(team);
        }
    }



    /*
    @EventListener
    private void getUMessage(UserNotification event) {
        notifications.create()
                .withDescription(event.getMessage())
                .withCaption("System Message")
                .withType(Notifications.NotificationType.SYSTEM)
                .show();
    }

    @EventListener
    private void received(UserNotification event) {
        notifications.create()
                .withCaption("System Communication")
                .withDescription(event.getMessage())
                .withType(Notifications.NotificationType.WARNING)
                .withPosition(Notifications.Position.TOP_CENTER)
                .show();
    }

     */




}