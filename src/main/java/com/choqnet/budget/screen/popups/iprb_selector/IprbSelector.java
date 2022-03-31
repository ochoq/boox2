package com.choqnet.budget.screen.popups.iprb_selector;

import com.choqnet.budget.entity.Budget;
import com.choqnet.budget.entity.IPRB;
import com.choqnet.budget.entity.Progress;
import io.jmix.core.DataManager;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@UiController("IprbSelector")
@UiDescriptor("iprb-selector.xml")
@DialogMode(width = "90%", height = "90%")
public class IprbSelector extends Screen {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(IprbSelector.class);
    private Budget budget;
    @Autowired
    private CollectionContainer<IPRB> iPRBsDc;
    @Autowired
    private DataGrid<IPRB> iPRBsTable;
    @Autowired
    private DataManager dataManager;

    public void setIPRBs(List<IPRB> iprbs, Budget budget) {
        this.budget = budget;
        iprbs = iprbs.stream().sorted(Comparator.comparing(IPRB::getReference)).collect(Collectors.toList());
        iPRBsDc.setItems(iprbs);
    }

    @Subscribe("btnCancel")
    public void onBtnCancelClick(Button.ClickEvent event) {
        closeWithDefaultAction();

    }

    @Subscribe("btnAdd")
    public void onBtnAddClick(Button.ClickEvent event) {
        for (IPRB iprb: iPRBsTable.getSelected()) {
            Progress progress = dataManager.create(Progress.class);
            progress.setIprb(iprb);
            progress.setBudget(budget);
            dataManager.save(progress);
        }
        closeWithDefaultAction();
    }

}