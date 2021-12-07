package com.choqnet.budget.screen.popups.delete_budget;

import com.choqnet.budget.entity.*;
import io.jmix.core.DataManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.Label;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("DeleteBudget")
@UiDescriptor("delete-budget.xml")
public class DeleteBudget extends Screen {
    private Budget budget;
    @Autowired
    private Label txtWarning;
    @Autowired
    private Button btnDelete;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DataManager dataManager;

    public void setBudgetToDelete(Budget budget) {
        this.budget = budget;
        txtWarning.setValue(budget.getName() + " (" + budget.getYear() + ")");
    }

    @Subscribe
    public void onInit(InitEvent event) {
        btnDelete.setEnabled(false);
    }

    @Subscribe("txtConfirm")
    public void onTxtConfirmValueChange(HasValue.ValueChangeEvent event) {
        btnDelete.setEnabled(event.getValue().equals(budget.getName()));
    }

    @Subscribe("btnDelete")
    public void onBtnDeleteClick(Button.ClickEvent event) {
        String budgetName = budget.getName();
        // actual deletions in cascade
        List<Capacity> capacities = dataManager.load(Capacity.class)
                .query("select e from Capacity e where e.budget = :budget")
                .parameter("budget", budget)
                .list();
        int nbCapacities = capacities.size();
        dataManager.remove(capacities);
        List<Detail> details = dataManager.load(Detail.class)
                .query("select e from Detail e where e.demand.budget = :budget")
                .parameter("budget", budget)
                .list();
        int nbDetails = details.size();
        dataManager.remove(details);
        List<Expense> expenses = dataManager.load(Expense.class)
                .query("select e from Expense e where e.demand.budget = :budget")
                .parameter("budget", budget)
                .list();
        int nbEuroDemand = expenses.size();
        dataManager.remove(expenses);
        List<Demand> demands = dataManager.load(Demand.class)
                .query("select e from Demand e where e.budget = :budget")
                .parameter("budget", budget)
                .list();
        int nbDemands = demands.size();
        dataManager.remove(demands);
        dataManager.remove(budget);
        notifications.create()
                .withCaption("Budget Deletion")
                .withDescription(budgetName + " is deleted\n- 1 budget\n- " + nbCapacities + " capacity lines\n- " + nbDemands + " demands with " +nbDetails + " detail lines and " + nbEuroDemand + " expense lines")
                .withType(Notifications.NotificationType.WARNING)
                .show();
        closeWithDefaultAction();
    }

    @Subscribe("btnCancel")
    public void onBtnCancelClick(Button.ClickEvent event) {
        closeWithDefaultAction();
    }


}