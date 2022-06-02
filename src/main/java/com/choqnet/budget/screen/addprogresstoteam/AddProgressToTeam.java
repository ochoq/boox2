package com.choqnet.budget.screen.addprogresstoteam;

import com.choqnet.budget.entity.*;
import io.jmix.core.DataManager;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.Label;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UiController("AddProgressToTeam")
@UiDescriptor("add-progress-to-team.xml")
@DialogMode(width = "500px", height = "300px")
public class AddProgressToTeam extends Screen {
    private Team team;
    private Budget budget;

    @Autowired
    private DataManager dataManager;
    @Autowired
    private ComboBox<IPRB> cmbIPRB;
    @Autowired
    private Button btnCreate;
    @Autowired
    private Label<String> lblName;

    @Subscribe
    public void onInit(InitEvent event) {
        List<IPRB> iprbs = dataManager.load(IPRB.class).all().list();
        cmbIPRB.setOptionsList(iprbs);
        btnCreate.setEnabled(false);
    }

    public void setContent(Budget budget, Team team) {
        this.budget = budget;
        this.team = team;
    }

    @Subscribe("cmbIPRB")
    public void onCmbIPRBValueChange(HasValue.ValueChangeEvent<IPRB> event) {
        btnCreate.setEnabled(true);
        lblName.setValue(Objects.requireNonNull(event.getValue()).getName());
    }

    @Subscribe("btnCancel")
    public void onBtnCancelClick(Button.ClickEvent event) {
        closeWithDefaultAction();
    }

    @Subscribe("btnCreate")
    public void onBtnCreateClick(Button.ClickEvent event) {
        if (cmbIPRB.getValue() != null) {
            IPRB iprbTarget = cmbIPRB.getValue();
            Progress targetProgress = dataManager.load(Progress.class)
                    .query("select e from Progress e where e.budget = :budget and e.iprb = :iprb")
                    .parameter("budget", budget)
                    .parameter("iprb", iprbTarget)
                    .list()
                    .stream()
                    .findFirst()
                    .orElse(dataManager.create(Progress.class));
            targetProgress.setBudget(budget);
            targetProgress.setIprb(iprbTarget);
            targetProgress = dataManager.save(targetProgress);
            Detail newDetail = dataManager.create(Detail.class);
            newDetail.setProgress(targetProgress);
            newDetail.setBudget(budget);
            newDetail.setIprb(iprbTarget);
            newDetail.setTeam(team);
            newDetail = dataManager.save(newDetail);
            List<Detail> details = new ArrayList<>();
            details.add(newDetail);
            targetProgress.setDetails(details);
            dataManager.save(targetProgress);
        }
        closeWithDefaultAction();
    }
}