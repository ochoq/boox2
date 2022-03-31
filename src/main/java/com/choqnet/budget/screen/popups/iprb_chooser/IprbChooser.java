package com.choqnet.budget.screen.popups.iprb_chooser;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@UiController("IprbChooser")
@UiDescriptor("iprb-chooser.xml")
@DialogMode(width = "500px", height = "300px")
public class IprbChooser extends Screen {
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ComboBox<IPRB> cmbIPRB;
    @Autowired
    private Button btnMove;
    @Autowired
    private Button btnCancel;

    private Set<Detail> detailsToMove;
    private Budget budget;
    @Autowired
    private UtilBean utilBean;

    @Subscribe
    public void onInit(InitEvent event) {
        List<IPRB> iprbs = dataManager.load(IPRB.class).all().list();
        cmbIPRB.setOptionsList(iprbs);
        btnMove.setEnabled(false);
    }

    public void setContext(Set<Detail> details, Budget budget) {
        detailsToMove = details;
        this.budget = budget;
    }

    @Subscribe("cmbIPRB")
    public void onCmbIPRBValueChange(HasValue.ValueChangeEvent event) {
        btnMove.setEnabled(true);
    }

    @Subscribe("btnCancel")
    public void onBtnCancelClick(Button.ClickEvent event) {
        closeWithDefaultAction();
    }

    @Subscribe("btnMove")
    public void onBtnMoveClick(Button.ClickEvent event) {
        // gets or creates the demand
        if (cmbIPRB.getValue()!=null) {
            IPRB iprbTarget = cmbIPRB.getValue();
            Progress target = dataManager.load(Progress.class)
                    .query("select e from Progress e where e.budget = :budget and e.iprb = :iprb")
                    .parameter("budget", budget)
                    .parameter("iprb", iprbTarget)
                    .list()
                    .stream()
                    .findFirst()
                    .orElse(dataManager.create(Progress.class));
            target.setBudget(budget);
            target.setIprb(iprbTarget);
            target = dataManager.save(target);
            for (Detail detail : detailsToMove) {
                Progress source = detail.getProgress();
                utilBean.moveDetail(source, target, detail);
            }
            /*
            Demand demand = dataManager
                    .load(Demand.class)
                    .query("select e from Demand e where e.budget = :budget and e.iprb = :iprb")
                    .parameter("budget", budget)
                    .parameter("iprb", iprbTarget)
                    .list()
                    .stream()
                    .findFirst()
                    .orElse(dataManager.create(Demand.class));
            demand.setBudget(budget);
            demand.setIprb(iprbTarget);
            demand = dataManager.save(demand);
            SaveContext sc = new SaveContext();
            for (Detail detail: detailsToMove) {
                detail.setDemand(demand);
                sc.saving(detail);
            }
            dataManager.save(sc);
             */
        }
        closeWithDefaultAction();
    }






}