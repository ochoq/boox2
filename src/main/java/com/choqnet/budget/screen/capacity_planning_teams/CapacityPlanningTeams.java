package com.choqnet.budget.screen.capacity_planning_teams;

import com.choqnet.budget.entity.Budget;
import com.choqnet.budget.entity.CPTeam;
import com.choqnet.budget.entity.Capacity;
import com.choqnet.budget.entity.Detail;
import com.choqnet.budget.entity.datalists.Priority;
import io.jmix.core.DataManager;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@UiController("CapacityPlanningTeams")
@UiDescriptor("capacity-planning-teams.xml")
public class CapacityPlanningTeams extends Screen {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CapacityPlanningTeams.class);
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ComboBox<Budget> cmbBudget;
    @Autowired
    private CollectionContainer<CPTeam> cPTeamsDc;
    @Autowired
    private DataGrid<CPTeam> cPTeamsTable;
    @Autowired
    private ComboBox<Priority> cmbPriority;

    private Budget budget;
    private Priority priority;
    private List<Detail> details;
    private List<Capacity> capacities;
    private List<CPTeam> cpTeams = new ArrayList<>();

    // *** Initializations

    @Subscribe
    public void onInit(InitEvent event) {
        // populates the list of budget
        List<Budget> budgets = dataManager.load(Budget.class)
                .query("select e from Budget e order by e.year desc, e.name asc")
                .list();
        cmbBudget.setOptionsList(budgets);
        // sets the default budget
        Optional<Budget> prefBudget = budgets.stream().filter(Budget::getPreferred).findFirst();
        prefBudget.ifPresent(value -> cmbBudget.setValue(value));
        // populates the list of priorities
        cmbPriority.setOptionsList(Arrays.asList(Priority.values()));

        /*
        // temp code
        List<Capacity> capacities = dataManager.load(Capacity.class).all().list();
        List<CPTeam> cpTeams = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CPTeam cpTeam = dataManager.create(CPTeam.class);
            cpTeam.setPriority(Priority.P1);
            cpTeam.setDemandQ1(20.00 * i);
            cpTeam.setCapacity(capacities.get(i));
            cpTeams.add(cpTeam);
        }
        cPTeamsDc.setItems(cpTeams);
        cPTeamsTable.repaint();
        */
    }


    // *** UI functions
    @Subscribe("cmbBudget")
    public void onCmbBudgetValueChange(HasValue.ValueChangeEvent<Budget> event) {
        log.info("budget updated");
        budget = event.getValue();
        cmbPriority.setValue(event.getValue().getPrioThreshold());
        // updates the dataset for the capacity planning
        updateCapacityPlanningData();
        // updates the default priority threshold
    }


    @Subscribe("cmbPriority")
    public void onCmbPriorityValueChange(HasValue.ValueChangeEvent<Priority> event) {
        log.info("priority updated");
        priority = event.getValue();
        // updates the data set for the capacity planning
        updateCapacityPlanningData();
    }

    // *** Data functions
    private void updateCapacityPlanningData() {
        // refines the capacity and demand.details dataset for the CP report
        details = dataManager.load(Detail.class)
                .query("select e from Detail e where e.demand.budget = :budget")
                .parameter("budget", budget)
                .list();
        capacities = dataManager.load(Capacity.class)
                .query("select e from Capacity e where e.budget = :budget")
                .parameter("budget", budget)
                .list();
        if (priority!= null && budget!=null) {
            // creation of the CPTeam records
            cpTeams = new ArrayList<>();
            for (Capacity capacity : capacities) {

                System.out.println("managing ... " + capacity.getTeam().getName() + " - " + capacity.getTeam().getFullName());

                CPTeam cpTeam = dataManager.create(CPTeam.class);
                cpTeam.setCapacity(capacity);
                cpTeam.setDemandQ1(details.stream()
                        .filter(e -> e.getPriority().isLessOrEqual(priority) && e.getTeam()!=null && e.getTeam().equals(capacity.getTeam()))
                        .map(Detail::getMdQ1)
                        .reduce(Double::sum).orElse(0.0));
                cpTeam.setDemandQ2(details.stream()
                        .filter(e -> e.getPriority().isLessOrEqual(priority) && e.getTeam()!=null && e.getTeam().equals(capacity.getTeam()))
                        .map(Detail::getMdQ2)
                        .reduce(Double::sum).orElse(0.0));
                cpTeam.setDemandQ3(details.stream()
                        .filter(e -> e.getPriority().isLessOrEqual(priority) && e.getTeam()!=null && e.getTeam().equals(capacity.getTeam()))
                        .map(Detail::getMdQ3)
                        .reduce(Double::sum).orElse(0.0));
                cpTeam.setDemandQ4(details.stream()
                        .filter(e -> e.getPriority().isLessOrEqual(priority) && e.getTeam()!=null && e.getTeam().equals(capacity.getTeam()))
                        .map(Detail::getMdQ4)
                        .reduce(Double::sum).orElse(0.0));
                cpTeams.add(cpTeam);
            }
            cPTeamsDc.setItems(cpTeams);
            cPTeamsTable.repaint();
            log.info("Refreshed");
        }

    }
}