package com.choqnet.budget.screen.worklog_summary;

import com.choqnet.budget.entity.Actual;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@UiController("WorklogSummary")
@UiDescriptor("worklog-summary.xml")
public class WorklogSummary extends Screen {
    @Autowired
    private CollectionLoader<Actual> actualsDl;
    @Autowired
    private CheckBox chk2021;
    @Autowired
    private CheckBox chk2022;

    private void makeQuery() {
        List<String> years = new ArrayList<>();
        if (chk2021.getValue()) years.add("fin2021");
        if (chk2022.getValue()) years.add("fin2022");
        int i = 0;
        String query = "";
        for (String year: years) {
            query = query + (i==0 ? " WHERE e.finMonth like '" + year + "%'" : " OR e.finMonth like '" + year + "%'");
            i++;
        }
        query = "select e from Actual e" + ((!chk2021.getValue() && !chk2022.getValue()) ? " where e.finMonth is null" : query);
        System.out.println(query);
        actualsDl.setQuery(query);
        actualsDl.load();
    }

    @Subscribe
    public void onInit(InitEvent event) {
        chk2022.setValue(true);
        makeQuery();
        //actualsDl.load();
    }

    @Subscribe("chk2021")
    public void onChk2021ValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.isUserOriginated()) makeQuery();
    }

    @Subscribe("chk2022")
    public void onChk2022ValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.isUserOriginated()) makeQuery();
    }





}