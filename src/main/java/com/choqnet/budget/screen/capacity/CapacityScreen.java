package com.choqnet.budget.screen.capacity;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.communications.UserNotification;
import com.choqnet.budget.entity.Budget;
import com.choqnet.budget.entity.Capacity;
import com.choqnet.budget.entity.ChangeHistory;
import com.choqnet.budget.entity.Team;
import com.choqnet.budget.screen.changes.Changes;
import com.choqnet.budget.screen.popups.team_selector.TeamSelector;
import com.choqnet.budget.screen.popups.upload_capacities.UploadCapacities;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.list.BulkEditAction;
import io.jmix.ui.app.bulk.ColumnsMode;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.inject.Named;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@UiController("CapacityScreen")
@UiDescriptor("capacity-screen.xml")
public class CapacityScreen extends Screen {

    private static final Logger log = LoggerFactory.getLogger(CapacityScreen.class);
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ComboBox<Budget> cmbBudget;
    @Autowired
    private CollectionLoader<Capacity> capacitiesDl;
    @Autowired
    private DataGrid<Capacity> capacitiesTable;
    @Autowired
    private Filter filter;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private Notifications notifications;

    private Budget budget;
    @Autowired
    private Button btnUpload;
    @Named("capacitiesTable.bulkEdit")
    private BulkEditAction capacitiesTableBulkEdit;
    @Autowired
    private UtilBean utilBean;

    // *** init & decoration functions
    @Subscribe
    public void onInit(InitEvent event) {
        // upload feature only visible from admin
        btnUpload.setVisible("admin".equals(((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()));
        // sets the options and default value for the Budget list
        List<Budget> budgets = dataManager.load(Budget.class)
                .query("select e from Budget e order by e.year desc, e.name asc")
                .list();
        cmbBudget.setOptionsList(budgets);
        Optional<Budget> prefBudget = budgets.stream().filter(Budget::getPreferred).findFirst();
        prefBudget.ifPresent(value -> cmbBudget.setValue(value));
        // folds the filter
        filter.setExpanded(false);
        capacitiesTableBulkEdit.setColumnsMode(ColumnsMode.TWO_COLUMNS);

        // columns' style
        capacitiesTable.getColumn("mdY")
                .setStyleProvider(capacity -> {
                    return "crotr";
                });
        Objects.requireNonNull(capacitiesTable.getColumn("nbWorkingDays"))
                .setStyleProvider(capacity -> {
                    return "ce2r";
                });
        capacitiesTable.getColumn("rateY")
                .setStyleProvider(capacity -> {
                    return "ce2r";
                });
        for (int i =1; i < 5; i++) {
            int finalI = i;
            capacitiesTable.getColumn("mdQ" + i)
                    .setStyleProvider(capacity -> {
                        return budget.getCloseQx(finalI) ? "cror" : "ce1r";
                    });
            capacitiesTable.getColumn("fteQ" + i)
                    .setStyleProvider(capacity ->  "cror");
        }
        capacitiesTable.getHeaderRow(0).getCell("nbWorkingDays").setStyleName("h1r");
        capacitiesTable.getHeaderRow(0).getCell("fteQ1").setStyleName("h1r");
        capacitiesTable.getHeaderRow(0).getCell("fteQ2").setStyleName("h1r");
        capacitiesTable.getHeaderRow(0).getCell("fteQ3").setStyleName("h1r");
        capacitiesTable.getHeaderRow(0).getCell("fteQ4").setStyleName("h1r");
        capacitiesTable.getHeaderRow(0).getCell("mdY").setStyleName("h1r");
        capacitiesTable.getHeaderRow(0).getCell("mdQ1").setStyleName("h1r");
        capacitiesTable.getHeaderRow(0).getCell("mdQ2").setStyleName("h1r");
        capacitiesTable.getHeaderRow(0).getCell("mdQ3").setStyleName("h1r");
        capacitiesTable.getHeaderRow(0).getCell("mdQ4").setStyleName("h1r");
        capacitiesTable.getHeaderRow(0).getCell("rateY").setStyleName("h1r");
    }

    // *** UI functions
    @Subscribe("cmbBudget")
    public void onCmbBudgetValueChange(HasValue.ValueChangeEvent event) {
        // loads the capacity records when the budget changes
        if (cmbBudget.getValue()!=null) {
            capacitiesDl.setQuery("select e from Capacity e where e.budget = :budget and e.team.enabled = true order by e.team.fullName asc");
            capacitiesDl.setParameter("budget", cmbBudget.getValue());
            capacitiesDl.load();

            // defines edit / read-only columns for the fteQx
            budget = cmbBudget.getValue();
            for (int i=1; i<5; i++) {
                int finalI = i;
                capacitiesTable.getColumn("mdQ" + i).setEditable(!budget.getCloseQx(i));
                capacitiesTable.getColumn("fteQ"+i).setCaption("FTE Q" +i + (budget.getCloseQx(i) ? "(x)" : ""));
                capacitiesTable.getColumn("mdQ"+i).setCaption("MD Q" +i + (budget.getCloseQx(i) ? "(x)" : ""));
                capacitiesTable.getColumn("fteQ"+i).setStyleProvider(e -> { return budget.getCloseQx(finalI) ? "cror" : "";});
                capacitiesTable.getColumn("mdQ"+i).setStyleProvider(e -> { return budget.getCloseQx(finalI) ? "cror" : "";});
            }
        }
        capacitiesTable.sort("team", DataGrid.SortDirection.ASCENDING);
    }

    @Subscribe("capacitiesTable")
    public void onCapacitiesTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Capacity> event) {
        dataManager.save(event.getItem());
        capacitiesDl.load();
    }

    @Subscribe("btnAdd")
    public void onBtnAddClick(Button.ClickEvent event) {
        // adds more teams, if any
        List<Team> allTeams = dataManager
                .load(Team.class)
                .condition(
                        PropertyCondition.equal("enabled", true)
                )
                .list();
        List<Team> usedTeams = dataManager
                .load(Capacity.class)
                .query("select e from Capacity e where e.budget = :budget and e.team.enabled = true")
                .parameter("budget",cmbBudget.getValue())
                .list()
                .stream()
                .map(Capacity::getTeam)
                .distinct()
                .collect(Collectors.toList());
        for (Team team: usedTeams) {
            allTeams.remove(team);
        }
        // call a popup to choose teams in what remains in allTeam (i.e. teams not yet used for a Capacity of this budget
        TeamSelector teamSelector = screenBuilders.screen(this)
                .withScreenClass(TeamSelector.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    capacitiesDl.load();
                })
                .build();
        teamSelector.show();
        teamSelector.setTeams(allTeams, cmbBudget.getValue());
    }

    @Subscribe("btnUpload")
    public void onBtnUploadClick(Button.ClickEvent event) {
        // opens the dialog screen for uploading the Excel file
        UploadCapacities uploadCapacities = screenBuilders.screen(this)
                .withScreenClass(UploadCapacities.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    notifications.create()
                            .withType(Notifications.NotificationType.TRAY)
                            .withCaption("File upload utility closed.")
                            .show();
                    capacitiesDl.load();
                    capacitiesTable.sort("team", DataGrid.SortDirection.ASCENDING);
                })
                .build();
        uploadCapacities.setBudget(budget);
        uploadCapacities.show();
    }
    // *** Data functions

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

    @Subscribe("capacitiesTable")
    public void onCapacitiesTableContextClick(DataGrid.ContextClickEvent event) {
        List<ChangeHistory> changeHistoryList = utilBean.giveChangeHistory(capacitiesTable.getSingleSelected().getId());

        Changes changes = screenBuilders.screen(this)
                .withScreenClass(Changes.class)
                .withOpenMode(OpenMode.DIALOG)
                .build();
        changes.show();
        changes.setData(changeHistoryList);

    }

}