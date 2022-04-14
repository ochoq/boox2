package com.choqnet.budget.screen.worklog_pmo;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.XLUtils;
import com.choqnet.budget.entity.Worklog;
import io.jmix.core.DataManager;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@UiController("WorklogPmo")
@UiDescriptor("worklog-pmo.xml")
public class WorklogPmo extends Screen {
    @Autowired
    private UtilBean utilBean;
    @Autowired
    private XLUtils xLUtils;
    @Autowired
    private ComboBox<String> cmbMonths;
    @Autowired
    private CollectionLoader<Worklog> worklogsDl;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private CollectionContainer<Worklog> worklogsDc;
    @Autowired
    private DataGrid<Worklog> worklogsTable;

    @Subscribe
    public void onInit(InitEvent event) {
        // set the data to the former month
        List<String> months = new ArrayList<>();
        for (int i=0; i<12; i++) {
            months.add(utilBean.getFinancialMonth(utilBean.addTime(new Date(), -i, "MONTH")));
        }
        cmbMonths.setOptionsList(months);
        String lastMonth = utilBean.getFinancialMonth(utilBean.addTime(new Date(), -1, "MONTH"));
        cmbMonths.setValue(lastMonth);
        updateData(lastMonth);
    }

    @Subscribe("cmbMonths")
    public void onCmbMonthsValueChange(HasValue.ValueChangeEvent<String> event) {
        if (event.isUserOriginated()) {
            updateData(event.getValue());
        }
    }

    private void updateData(String month) {
        worklogsDl.setQuery("select e from Worklog e where e.finMonth = :month");
        worklogsDl.setParameter("month",month);
        worklogsDl.load();
    }


}