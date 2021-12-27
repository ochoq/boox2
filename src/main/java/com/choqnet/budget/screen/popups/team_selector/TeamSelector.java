package com.choqnet.budget.screen.popups.team_selector;

import com.choqnet.budget.entity.Budget;
import com.choqnet.budget.entity.Capacity;
import com.choqnet.budget.entity.Team;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("TeamSelector")
@UiDescriptor("team-selector.xml")
@DialogMode(width = "90%", height = "90%")
public class TeamSelector extends Screen {
    @Autowired
    private CollectionContainer<Team> teamsDc;

    private List<Team> teams;
    private Budget budget;
    @Autowired
    private DataGrid<Team> teamsTable;
    @Autowired
    private DataManager dataManager;

    public void setTeams(List<Team> teams,Budget budget) {
        this.budget = budget;
        this.teams = teams;
        teamsDc.setItems(teams);
    }

    @Subscribe("btnCancel")
    public void onBtnCancelClick(Button.ClickEvent event) {
        closeWithDefaultAction();
    }

    @Subscribe("btnAdd")
    public void onBtnAddClick(Button.ClickEvent event) {
        SaveContext sc = new SaveContext();
        for (Team team: teamsTable.getSelected()) {
            Capacity capacity = dataManager.create(Capacity.class);
            capacity.setTeam(team);
            capacity.setBudget(budget);
            sc.saving(capacity);
        }
        dataManager.save(sc);
        closeWithDefaultAction();
    }

}