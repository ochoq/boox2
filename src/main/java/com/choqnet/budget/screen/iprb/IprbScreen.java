package com.choqnet.budget.screen.iprb;

import com.choqnet.budget.communications.UserNotification;
import com.choqnet.budget.entity.IPRB;
import com.choqnet.budget.screen.popups.upload_iprb.UploadIprb;
import io.jmix.core.DataManager;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.action.list.RefreshAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.Filter;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.inject.Named;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UiController("IprbScreen")
@UiDescriptor("iprb-screen.xml")
public class IprbScreen extends Screen {
    private static final Logger log = LoggerFactory.getLogger(IprbScreen.class);
    @Autowired
    private Filter filter;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private CollectionLoader<IPRB> iPRBsDl;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private DataGrid<IPRB> iPRBsTable;
    @Autowired
    private Button btnClose;
    @Named("iPRBsTable.refresh")
    private RefreshAction iPRBsTableRefresh;
    @Autowired
    private Button btnUpload;

    @Subscribe
    public void onInit(InitEvent event) {
        filter.setExpanded(false);
        iPRBsDl.load();
        btnClose.setEnabled(false);
        // upload feature only visible from admin
        boolean allowed = "admin".equals(((UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
        btnUpload.setVisible("admin".equals(((UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()));
    }

    // *** UI Functions
    @Subscribe("btnCreate")
    public void onBtnCreateClick(Button.ClickEvent event) {
        // creates an IPRB and sets its reference (logged_user + date)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssa");
        IPRB iprb= dataManager.create(IPRB.class);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        iprb.setReference(" "+ ((UserDetails)principal).getUsername() + LocalDateTime.now().format(formatter));
        dataManager.save(iprb);
        iPRBsDl.load();
        iPRBsTable.sort("reference", DataGrid.SortDirection.ASCENDING);
    }

    @Subscribe("btnUpload")
    public void onBtnUploadClick(Button.ClickEvent event) {
        // opens the dialog screen for uploading the Excel file
        UploadIprb uploadIPRB = screenBuilders.screen(this)
                .withScreenClass(UploadIprb.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    notifications.create()
                            .withType(Notifications.NotificationType.TRAY)
                            .withCaption("File upload utility closed.")
                            .show();
                    iPRBsDl.load();
                })
                .build();
        uploadIPRB.show();
    }

    @Subscribe("btnClose")
    public void onBtnCloseClick(Button.ClickEvent event) {
        // closes the selected IPRBs
        dialogs.createOptionDialog()
                .withCaption("IPRB Closing")
                .withMessage("You're about to close " + iPRBsTable.getSelected().size() + " IPRBs. Do you confirm?")
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withHandler(e -> closeIPRBs()),
                        new DialogAction(DialogAction.Type.CANCEL)
                )
                .show();
    }

    @Subscribe("iPRBsTable")
    public void onIPRBsTableSelection(DataGrid.SelectionEvent<IPRB> event) {
        btnClose.setEnabled(iPRBsTable.getSelected().size()!=0);
    }

    // *** Data Management functions
    private void closeIPRBs() {
        // actually closes the IPRB
        DateTimeFormatter dft = DateTimeFormatter.ofPattern("yyyyMM");
        String firstMonthOff = "fin" + (LocalDateTime.now().getDayOfMonth()>=24 ? LocalDateTime.now().plusMonths(2).format(dft) : LocalDateTime.now().plusMonths(1).format(dft));
        for (IPRB iprb: iPRBsTable.getSelected()) {
            iprb.setFirstMonthOff(firstMonthOff);
            dataManager.save(iprb);
        }
        notifications.create()
                .withCaption("IPRB Closing")
                .withDescription("No time tracking will be accepted on these IPRBs from " + firstMonthOff + ". In case people track time against them, the logged times will be reassigned to the IPRB [CLOSED] - Closed Projects time collector.")
                .withType(Notifications.NotificationType.WARNING)
                .show();
    }

    @Subscribe("iPRBsTable")
    public void onIPRBsTableEditorPostCommit(DataGrid.EditorPostCommitEvent<IPRB> event) {
        IPRB iprb = event.getItem();
        // Warning if missing items
        List<String> missings = new ArrayList<>();
        if (iprb.getPortfolioClassification()==null) missings.add("Portfolio Classification");
        if (iprb.getLegalEntity()==null) missings.add("Legal Entity");
        if (iprb.getStrategicProgram()==null) missings.add("Strategic Program");
        if (iprb.getActivityType()==null) missings.add("Activity Type");
        if (iprb.getNewProductIndicator()==null) missings.add("NPI");
        if (iprb.getGroupOffering()==null) missings.add("Group Offering");
        if (missings.size()!=0) {
            String missMessage = "";
            for (String missing: missings) {
                missMessage += missing + "\n";
            }
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption("Data Quality Check")
                    .withDescription("Your record misses the following information:\n" + missMessage)
                    .show();
        }

        // DefaultValueProvider for Owner
        if (iprb.getOwner()==null || Objects.equals(iprb.getOwner(), "")) {
            iprb.setOwner(((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
        }
        // save changes permanently
        iprb.setUpdated(LocalDateTime.now());
        dataManager.save(iprb);
        iPRBsDl.load();
        iPRBsTableRefresh.execute();
    }

    // *** communications
    // --- General Messaging
    @EventListener
    private void received(UserNotification event) {
        notifications.create()
                .withCaption("System Communication")
                .withDescription(">> " + event.getMessage())
                .withType(Notifications.NotificationType.WARNING)
                .withPosition(Notifications.Position.TOP_CENTER)
                .show();
    }


}