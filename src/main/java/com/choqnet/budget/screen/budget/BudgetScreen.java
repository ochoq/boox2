package com.choqnet.budget.screen.budget;

import com.choqnet.budget.communications.UserNotification;
import com.choqnet.budget.entity.*;
import com.choqnet.budget.screen.popups.create_budget.CreateBudget;
import com.choqnet.budget.screen.popups.delete_budget.DeleteBudget;
import io.jmix.core.DataManager;
import io.jmix.core.MetadataTools;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.Filter;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@UiController("BudgetScreen")
@UiDescriptor("budget-screen.xml")
public class BudgetScreen extends Screen {
    private static final Logger log = LoggerFactory.getLogger(BudgetScreen.class);

    @Autowired
    private CollectionLoader<Budget> budgetsDl;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private DataGrid<Budget> budgetsTable;
    @Autowired
    private Filter filter;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private Button btnRemove;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Button btnClone;
    @Autowired
    private MetadataTools metadataTools;

    @Subscribe
    public void onInit(InitEvent event) {
        filter.setExpanded(false);
        btnRemove.setEnabled(false);
        btnClone.setEnabled(false);
        budgetsDl.load();
    }

    // *** UI Functions
    @Subscribe("btnCreate")
    public void onBtnCreateClick(Button.ClickEvent event) {
        CreateBudget createBudget = screenBuilders.screen(this)
                .withScreenClass(CreateBudget.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    budgetsDl.load();
                })
                .build();
        createBudget.show();
    }

    @Subscribe("btnRemove")
    public void onBtnRemoveClick(Button.ClickEvent event) {
        DeleteBudget deleteBudget = screenBuilders.screen(this)
                .withScreenClass(DeleteBudget.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    budgetsDl.load();
                })
                .build();
        deleteBudget.setBudgetToDelete(budgetsTable.getSingleSelected());
        deleteBudget.show();
    }

    @Subscribe("btnClone")
    public void onBtnCloneClick(Button.ClickEvent event) {
        dialogs.createOptionDialog()
                .withMessage("You're about to close the budget " + budgetsTable.getSingleSelected().getName() + ". Do you confirm?")
                .withCaption("Budget Clone")
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withHandler(e -> cloneBudget()),
                        new DialogAction(DialogAction.Type.CANCEL)
                )
                .show();
    }

    private void cloneBudget() {
        Budget source = budgetsTable.getSingleSelected();
        if (source == null) {
            return;
        }
        // clone of the root
        Budget clonedBudget = cloneBudget(source);

        // clone of the capacities
        List<Capacity> capacities = source.getCapacities();
        List<Capacity> clonedCapacities = new ArrayList<>();
        for (Capacity capacity : capacities) {
            clonedCapacities.add(cloneCapacity(capacity, clonedBudget));
        }
        clonedBudget.setCapacities(clonedCapacities);
        capacities = new ArrayList<>();
        clonedCapacities = new ArrayList<>();
        dataManager.save(clonedBudget);

        // clone of the progresses
        List<Progress> progresses = dataManager.load(Progress.class)
                .query("select e from Progress e where e.budget = :budget")
                .parameter("budget", source)
                .fetchPlan("progAll")
                .list();
        for(Progress progress: progresses) {
            Progress clonedProgress = cloneProgress(progress, clonedBudget);
            // clone the expenses
            List<Expense> expenses = progress.getExpenses();
            List<Expense> clonedExpenses = new ArrayList<>();
            for (Expense expense: expenses) {
                clonedExpenses.add(cloneExpense(expense, clonedProgress, clonedBudget));
            }
            clonedProgress.setExpenses(clonedExpenses);

            // clone the details
            List<Detail> details = progress.getDetails();
            List<Detail> clonedDetails = new ArrayList<>();
            for (Detail detail: details) {
                clonedDetails.add(cloneDetail(detail, clonedProgress, clonedBudget));
            }
            clonedProgress.setDetails(clonedDetails);
            dataManager.save(clonedProgress);
        }
        // refreshes the display
        budgetsDl.load();
    }

    @Subscribe("budgetsTable")
    public void onBudgetsTableSelection(DataGrid.SelectionEvent<Budget> event) {
        budgetsTable.getSelected();
        btnRemove.setEnabled(true);
        btnClone.setEnabled(true);
    }

    // *** data functions
    @Subscribe("budgetsTable")
    public void onBudgetsTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Budget> event) {
        dataManager.save(event.getItem());
        budgetsDl.load();
    }

    // *** data basic function
    private Budget cloneBudget(Budget source) {
        Budget target = dataManager.create(Budget.class);
        UUID uuid = target.getId();
        metadataTools.copy(source, target);
        target.setName(target.getName() + " ~clone");
        target.setPreferred(false);
        target.setId(uuid);
        return dataManager.save(target);
    }

    private Capacity cloneCapacity(Capacity source, Budget budget) {
        Capacity target = dataManager.create(Capacity.class);
        UUID uuid = target.getId();
        metadataTools.copy(source, target);
        target.setBudget(budget);
        target.setId(uuid);
        target.setTeam(source.getTeam());
        return dataManager.save(target);
    }

    private Progress cloneProgress(Progress source, Budget budget) {
        Progress target = dataManager.create(Progress.class);
        UUID uuid = target.getId();
        metadataTools.copy(source, target);
        target.setBudget(budget);
        target.setId(uuid);
        target.setExpenses(null);
        target.setDetails(null);
        target.setActualQ1(0.0);
        target.setActualQ2(0.0);
        target.setActualQ3(0.0);
        target.setActualQ4(0.0);
        target.setIprb(source.getIprb());
        return dataManager.save(target);
    }

    private Expense cloneExpense(Expense source, Progress progress, Budget budget) {
        Expense target = dataManager.create(Expense.class);
        UUID uuid = target.getId();
        metadataTools.copy(source, target);
        target.setBudget(budget);
        target.setId(uuid);
        target.setProgress(progress);
        target.setIprb(source.getIprb());
        return dataManager.save(target);
    }

    private Detail cloneDetail(Detail source, Progress progress, Budget budget) {
        Detail target = dataManager.create(Detail.class);
        UUID uuid = target.getId();
        metadataTools.copy(source, target);
        target.setBudget(budget);
        target.setId(uuid);
        target.setProgress(progress);
        target.setTeam(source.getTeam());
        target.setIprb(source.getIprb());
        return dataManager.save(target);
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