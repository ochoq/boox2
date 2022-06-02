package com.choqnet.budget.screen.detail;

import com.choqnet.budget.entity.Budget;
import com.choqnet.budget.entity.Detail;
import io.jmix.core.DataManager;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("Detail.browse2")
@UiDescriptor("detail-browse2.xml")
public class DetailBrowse2 extends Screen {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private ComboBox<Budget> cmbBudget;
    @Autowired
    private CollectionLoader<Detail> detailsDl;



    @Subscribe
    public void onInit(InitEvent event) {
        List<Budget> budgets = dataManager.load(Budget.class).all().list();
        cmbBudget.setOptionsList(budgets);
        Budget targetBudget = budgets.stream().filter(Budget::getPreferred).findFirst().orElse(null);
        cmbBudget.setValue(targetBudget);
        detailsDl.setQuery("select e from Detail e where e.budget = :budget");
        detailsDl.setParameter("budget", targetBudget);
        detailsDl.load();
    }

    @Subscribe("cmbBudget")
    public void onCmbBudgetValueChange(HasValue.ValueChangeEvent<Budget> event) {
        detailsDl.setQuery("select e from Detail e where e.budget = :budget");
        detailsDl.setParameter("budget", event.getValue());
        detailsDl.load();
    }



}