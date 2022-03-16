package com.choqnet.budget.screen.worklogs;

import com.choqnet.budget.entity.Worklog;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.Label;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@UiController("Worklogs")
@UiDescriptor("worklogs.xml")
public class Worklogs extends Screen {
    @Autowired
    private CollectionLoader<Worklog> worklogsDl;
    @Autowired
    private CheckBox chk2021;
    @Autowired
    private CheckBox chk2022;

    @Subscribe
    public void onInit(InitEvent event) {
        chk2022.setValue(true);
        makeQuery();
    }

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
        query = "select e from Worklog e" + ((!chk2021.getValue() && !chk2022.getValue()) ? " where e.finMonth is null" : query);
        System.out.println(query);
        worklogsDl.setQuery(query);
        worklogsDl.load();
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