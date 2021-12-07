package com.choqnet.budget.screen.capacity;

import com.choqnet.budget.entity.Budget;
import com.choqnet.budget.entity.Capacity;
import com.choqnet.budget.entity.Team;
import com.choqnet.budget.screen.popups.team_selector.TeamSelector;
import com.choqnet.budget.screen.popups.upload_capacities.UploadCapacities;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

    // *** init & decoration functions
    @Subscribe
    public void onInit(InitEvent event) {
        // sets the options and default value for the Budget list
        List<Budget> budgets = dataManager.load(Budget.class)
                .query("select e from Budget e order by e.year desc, e.name asc")
                .list();
        cmbBudget.setOptionsList(budgets);
        Optional<Budget> prefBudget = budgets.stream().filter(Budget::getPreferred).findFirst();
        prefBudget.ifPresent(value -> cmbBudget.setValue(value));
        // folds the filter
        filter.setExpanded(false);


        // columns' style
        capacitiesTable.getColumn("mdY")
                .setStyleProvider(capacity -> {
                    return "readonlytotal";
                });
        Objects.requireNonNull(capacitiesTable.getColumn("nbWorkingDays"))
                .setStyleProvider(capacity -> {
                    return "boldCell";
                });
        for (int i =1; i < 5; i++) {
            capacitiesTable.getColumn("mdQ" + i)
                    .setStyleProvider(capacity -> {
                        return "readonly";
                    });
            capacitiesTable.getColumn("rateQ" + i)
                    .setStyleProvider(capacity -> {
                        return "boldCell";
                    });
            capacitiesTable.getColumn("fteQ" + i)
                    .setStyleProvider(capacity -> {
                        return "basicCell";
                    });
        }

    }

    // *** UI functions
    @Subscribe("cmbBudget")
    public void onCmbBudgetValueChange(HasValue.ValueChangeEvent event) {
        // loads the capacity records when the budget changes
        if (cmbBudget.getValue()!=null) {
            capacitiesDl.setQuery("select e from Capacity e where e.budget = :budget order by e.team.fullName asc");
            capacitiesDl.setParameter("budget", cmbBudget.getValue());
            capacitiesDl.load();

            // defines edit / read-only columns for the fteQx
            budget = cmbBudget.getValue();
            for (int i=1; i<5; i++) {
                int finalI = i;
                capacitiesTable.getColumn("fteQ"+i).setEditable(!budget.getCloseQx(i));
                capacitiesTable.getColumn("fteQ"+i).setCaption(
                        capacitiesTable.getColumn("fteQ"+i).getCaption() +
                                (budget.getCloseQx(i) ? "(x)" : "")
                );
                capacitiesTable.getColumn("fteQ"+i).setStyleProvider(e -> {
                    return budget.getCloseQx(finalI) ? "readonly" : "";
                });
            }
        }
        capacitiesTable.sort("team", DataGrid.SortDirection.ASCENDING);
    }

    @Subscribe("capacitiesTable")
    public void onCapacitiesTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Capacity> event) {
        dataManager.save(capacitiesTable.getSingleSelected());
        //capacitiesDl.load();
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
                .query("select e from Capacity e where e.budget = :budget")
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

}