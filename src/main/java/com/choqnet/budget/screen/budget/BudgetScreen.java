package com.choqnet.budget.screen.budget;

import com.choqnet.budget.entity.Budget;
import com.choqnet.budget.screen.popups.create_budget.CreateBudget;
import com.choqnet.budget.screen.popups.delete_budget.DeleteBudget;
import io.jmix.core.DataManager;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.Filter;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Subscribe
    public void onInit(InitEvent event) {
        filter.setExpanded(false);
        btnRemove.setEnabled(false);
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
        notifications.create()
                .withCaption("<!> System")
                .withDescription("The clone function is planned for a next release to come.")
                .withType(Notifications.NotificationType.WARNING)
                .show();
    }



    @Subscribe("budgetsTable")
    public void onBudgetsTableSelection(DataGrid.SelectionEvent<Budget> event) {
        budgetsTable.getSelected();
        btnRemove.setEnabled(true);
    }

    // *** data functions
    @Subscribe("budgetsTable")
    public void onBudgetsTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Budget> event) {
        dataManager.save(event.getItem());
        budgetsDl.load();
    }


}