package com.choqnet.budget.screen.capacity_planning_teams;

import com.choqnet.budget.communications.UserNotification;
import com.choqnet.budget.entity.Budget;
import com.choqnet.budget.entity.CPTeam;
import com.choqnet.budget.entity.Capacity;
import com.choqnet.budget.entity.Detail;
import com.choqnet.budget.entity.datalists.Priority;
import com.choqnet.budget.screen.popups.capacity_team_details.CapacityTeamDetails;
import io.jmix.core.DataManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UiController("CapacityPlanningTeams")
@UiDescriptor("capacity-planning-teams.xml")
public class CapacityPlanningTeams extends Screen {
    private static final Logger log = LoggerFactory.getLogger(CapacityPlanningTeams.class);
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
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private Notifications notifications;
    @Autowired
    private TextField<String> txtFilter;

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
        // Set styles for the dataGrid
        cPTeamsTable.getColumn("labelQ1")
                .setStyleProvider(CPTeam::getStyleQ1);
        cPTeamsTable.getColumn("labelQ2")
                .setStyleProvider(CPTeam::getStyleQ2);
        cPTeamsTable.getColumn("labelQ3")
                .setStyleProvider(CPTeam::getStyleQ3);
        cPTeamsTable.getColumn("labelQ4")
                .setStyleProvider(CPTeam::getStyleQ4);
        cPTeamsTable.getColumn("labelY")
                .setStyleProvider(CPTeam::getStyleY);
        // headers
        cPTeamsTable.getHeaderRow(0).getCell("labelQ1").setStyleName("boldCell");
        cPTeamsTable.getHeaderRow(0).getCell("labelQ2").setStyleName("boldCell");
        cPTeamsTable.getHeaderRow(0).getCell("labelQ3").setStyleName("boldCell");
        cPTeamsTable.getHeaderRow(0).getCell("labelQ4").setStyleName("boldCell");
        cPTeamsTable.getHeaderRow(0).getCell("labelY").setStyleName("boldCell");
        //cPTeamsTable.getHeaderRow(0).getCell("detail").setStyleName("center");

        DataGrid.HeaderRow headerRow = cPTeamsTable.prependHeaderRow();
        DataGrid.HeaderCell headerCell = headerRow.join("labelQ1", "labelQ2", "labelQ3", "labelQ4", "spacer", "labelY");
        headerCell.setText("Demand / Capacity in MD");
        headerCell.setStyleName("center");
    }

    // open details view (old version, driven by a click on a dedicated icon)
    /*
    @Install(to = "cPTeamsTable.detail", subject = "columnGenerator")
    private Component editDetail(DataGrid.ColumnGeneratorEvent<CPTeam> cEvent) {
        HBoxLayout hbl = uiComponents.create(HBoxLayout.NAME);
        hbl.setWidth("100%");
        hbl.setAlignment(Component.Alignment.MIDDLE_CENTER);
        LinkButton detail = uiComponents.create(LinkButton.NAME);
        detail.setIconFromSet(JmixIcon.EDIT);
        detail.setStyleName("center");
        detail.setAlignment(Component.Alignment.MIDDLE_CENTER);
        detail.addClickListener(e -> editCPTeam(cEvent.getItem()));
        hbl.add(detail);
        return hbl;
    }
    */

    // open details view by clicking on the name
    @Install(to = "cPTeamsTable.capaName", subject = "columnGenerator")
    private Component editDetail(DataGrid.ColumnGeneratorEvent<CPTeam> cEvent) {
        LinkButton detail = uiComponents.create(LinkButton.NAME);
        detail.setIconFromSet(JmixIcon.ARROW_CIRCLE_RIGHT);
        detail.addClickListener(e -> editCPTeam(cEvent.getItem()));
        detail.setCaption(cEvent.getItem().getCapacity().getTeam().getFullName());
        return detail;
    }


    private void editCPTeam(CPTeam item) {
        CapacityTeamDetails ctd = screenBuilders.screen(this)
                .withScreenClass(CapacityTeamDetails.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    notifications.create()
                            .withDescription("Details screen closed")
                            .withPosition(Notifications.Position.BOTTOM_RIGHT)
                            .show();
                    updateCapacityPlanningData();

                })
                .build();
        ctd.setContext(item, budget);
        ctd.show();
    }

    private void refreshData() {

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

    @Subscribe("txtFilter")
    public void onTxtFilterTextChange(TextInputField.TextChangeEvent event) {
        if ("".equals(event.getText())) {
            cPTeamsDc.setItems(cpTeams);
        } else {
            cPTeamsDc.setItems(
                    cpTeams.stream().filter(e -> e.getCapacity().getTeam().getFullName().toUpperCase().contains(event.getText().toUpperCase())).collect(Collectors.toList())
            );
        }
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
        if (priority != null && budget != null) {
            // creation of the CPTeam records
            cpTeams = new ArrayList<>();
            for (Capacity capacity : capacities) {

                // System.out.println("managing ... " + capacity.getTeam().getName() + " - " + capacity.getTeam().getFullName());

                CPTeam cpTeam = dataManager.create(CPTeam.class);
                cpTeam.setCapacity(capacity);
                cpTeam.setDemandQ1(details.stream()
                        .filter(e -> e.getPriority().isLessOrEqual(priority) && e.getTeam() != null && e.getTeam().equals(capacity.getTeam()))
                        .map(Detail::getMdQ1)
                        .reduce(Double::sum).orElse(0.0));
                cpTeam.setDemandQ2(details.stream()
                        .filter(e -> e.getPriority().isLessOrEqual(priority) && e.getTeam() != null && e.getTeam().equals(capacity.getTeam()))
                        .map(Detail::getMdQ2)
                        .reduce(Double::sum).orElse(0.0));
                cpTeam.setDemandQ3(details.stream()
                        .filter(e -> e.getPriority().isLessOrEqual(priority) && e.getTeam() != null && e.getTeam().equals(capacity.getTeam()))
                        .map(Detail::getMdQ3)
                        .reduce(Double::sum).orElse(0.0));
                cpTeam.setDemandQ4(details.stream()
                        .filter(e -> e.getPriority().isLessOrEqual(priority) && e.getTeam() != null && e.getTeam().equals(capacity.getTeam()))
                        .map(Detail::getMdQ4)
                        .reduce(Double::sum).orElse(0.0));
                cpTeams.add(cpTeam);
            }
            // filterTeams();
            cPTeamsDc.setItems(cpTeams);
            cPTeamsTable.repaint();
            if (cPTeamsDc.getItems().size() != 0) {
                cPTeamsTable.sort("capaName", DataGrid.SortDirection.ASCENDING);
            }
        }

    }

    // *** communications
    // --- General Messaging
    @EventListener
    private void received(UserNotification event) {
        notifications.create()
                .withCaption("System Communication")
                .withDescription(">>" + event.getMessage())
                .withType(Notifications.NotificationType.WARNING)
                .withPosition(Notifications.Position.TOP_CENTER)
                .show();
    }
}