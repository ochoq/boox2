package com.choqnet.budget.screen.popups.editexpenses;

import com.choqnet.budget.entity.Budget;
import com.choqnet.budget.entity.Demand;
import com.choqnet.budget.entity.Expense;
import io.jmix.core.DataManager;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.Label;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("EditExpenses")
@UiDescriptor("edit_expenses.xml")
public class EditExpenses extends Screen {
    @Autowired
    private Label title;

    // *** Properties
    Demand demand;
    @Autowired
    private CollectionLoader<Expense> expensesDl;
    @Autowired
    private Filter filter;
    @Autowired
    private Button btnRemove;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private DataGrid<Expense> euroDemandsTable;

    // *** Initialization functions

    public void setContext(Demand demand) {
        title.setValue("Extra Demands in k€ for the IPRB: " + demand.getIprb().getReference() + ", in budget " + demand.getBudget().getName());
        this.demand = demand;
        Budget budget = demand.getBudget();
        // loads the relevant set of data
        expensesDl.setQuery("select e from EuroDemand e where e.demand = :demand");
        expensesDl.setParameter("demand", demand);
        expensesDl.load();
    }

    @Subscribe
    public void onInit(InitEvent event) {
        filter.setExpanded(false);
        btnRemove.setEnabled(false);
    }

    // *** UI Functions

    @Subscribe("btnAdd")
    public void onBtnAddClick(Button.ClickEvent event) {
        Expense expense = dataManager.create(Expense.class);
        expense.setDemand(demand);
        dataManager.save(expense);
        expensesDl.load();
    }

    @Subscribe("euroDemandsTable")
    public void onEuroDemandsTableSelection(DataGrid.SelectionEvent<Expense> event) {
        btnRemove.setEnabled(true);
    }

    @Subscribe("btnClose")
    public void onBtnCloseClick(Button.ClickEvent event) {
        closeWithDefaultAction();
    }

    @Subscribe("btnRemove")
    public void onBtnRemoveClick(Button.ClickEvent event) {
        dataManager.remove(euroDemandsTable.getSelected());
        expensesDl.load();
    }

    // *** Data Functions

    @Subscribe("euroDemandsTable")
    public void onEuroDemandsTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Expense> event) {
        dataManager.save(event.getItem());
        expensesDl.load();
    }


}