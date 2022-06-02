package com.choqnet.budget.screen.actualspivot;

import com.choqnet.budget.entity.Actual;
import io.jmix.pivottable.component.PivotTable;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@UiController("ActualsPivot")
@UiDescriptor("actuals-pivot.xml")
public class ActualsPivot extends Screen {
    @Autowired
    private CollectionLoader<Actual> actualsDl;
    @Autowired
    private PivotTable pivot;

    @Subscribe
    public void onInit(InitEvent event) {
        actualsDl.load();
        Map<String, String> props = new HashMap<String, String>();
        props.put("finMonth", "Finance Month");
        props.put("iprbRef", "IPRB #");
        props.put("iprb.name", "IPRB Name");
        props.put("iprb.groupOffering", "IPRB Group Offering");
        props.put("iprb.legalEntity", "IPRB Platform");
        props.put("team.fullName", "Team full Name");
        props.put("team.name", "Team Name");
        props.put("team.simpleDomain", "Team Domain");
        props.put("team.simplePlatform", "Team Platform");
        props.put("team.inBudget", "Team in Budget?");
        props.put("costCenter", "Team Cost Center");
        props.put("initRef", "Initiative #");
        props.put("initName", "Initiative Name");
        props.put("jiraProject", "Jira Project");
        props.put("jiraProjectDomain", "Jira Project's Domain");
        props.put("jiraProjectPlatform", "Jira Project's Platform");
        props.put("effort", "Effort MD");
        pivot.setProperties(props);
        pivot.setAutoSortUnusedProperties(true);
        pivot.repaint();







    }

}