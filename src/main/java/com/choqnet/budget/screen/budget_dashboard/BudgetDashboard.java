package com.choqnet.budget.screen.budget_dashboard;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.communications.UserNotification;
import com.choqnet.budget.entity.*;
import com.choqnet.budget.entity.datalists.Priority;
import io.jmix.core.DataManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.stream.Collectors;

@UiController("BudgetDashboard")
@UiDescriptor("budget_dashboard.xml")
public class BudgetDashboard extends Screen {

    @Autowired
    private DataManager dataManager;
    @Autowired
    private ComboBox<Budget> cmbBudget;
    @Autowired
    private DataGrid<BDashboard> dTable;
    @Autowired
    private CollectionContainer<BDashboard> bDashboardsDc;
    @Autowired
    private UtilBean utilBean;

    // *** class variables
    private Priority prioThreshold;
    private Budget budget;
    @Autowired
    private CheckBox chkB;
    @Autowired
    private CheckBox chkC;
    @Autowired
    private CheckBox chkD;
    @Autowired
    private CheckBox chkEuro;
    @Autowired
    private CheckBox chkY;
    @Autowired
    private CheckBox chkMD;
    @Autowired
    private CheckBox chkT;
    @Autowired
    private CheckBox chkQ;
    boolean firstLine;
    @Autowired
    private Notifications notifications;


    // *** initializations
    @Subscribe
    public void onInit(InitEvent event) {
        // populates the list of budgets
        List<Budget> budgets = dataManager.load(Budget.class)
                .query("select e from Budget e order by e.year desc, e.name asc")
                .list();
        cmbBudget.setOptionsList(budgets);
        // set the default budget
        Optional<Budget> prefBudget = budgets.stream().filter(Budget::getPreferred).findFirst();
        prefBudget.ifPresent(value -> cmbBudget.setValue(value));
        setAllStyles();
        manageColumns();

    }

    private void setAllStyles() {
        // UI styles
        try {
            dTable.getHeaderRow(0).getCell("domain").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("platform").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("demandUMP").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("demandBAU").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("demandRUN").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("demandQ1").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("demandQ2").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("demandQ3").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("demandQ4").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("demand").setStyleName("h1r");

            dTable.getHeaderRow(0).getCell("budgetUMP").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("budgetBAU").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("budgetRUN").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("budgetQ1").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("budgetQ2").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("budgetQ3").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("budgetQ4").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("budget").setStyleName("h1r");

            dTable.getHeaderRow(0).getCell("kEurUMP").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("kEurBAU").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("kEurRUN").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("kEurQ1").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("kEurQ2").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("kEurQ3").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("kEurQ4").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("kEurY").setStyleName("h1r");

            dTable.getHeaderRow(0).getCell("capacity").setStyleName("h1r");
            dTable.getHeaderRow(0).getCell("kEurCapacity").setStyleName("h1r");

            dTable.getColumn("domain").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "1");
            dTable.getColumn("platform").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "2");

            dTable.getColumn("demandUMP").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("demandBAU").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("demandRUN").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("demandQ1").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("demandQ2").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("demandQ3").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("demandQ4").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("demand").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");

            dTable.getColumn("budgetUMP").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("budgetBAU").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("budgetRUN").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("budgetQ1").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("budgetQ2").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("budgetQ3").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("budgetQ4").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("budget").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");

            dTable.getColumn("kEurUMP").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("kEurBAU").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("kEurRUN").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("kEurQ1").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("kEurQ2").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("kEurQ3").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("kEurQ4").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("kEurY").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");

            dTable.getColumn("capacity").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");
            dTable.getColumn("kEurCapacity").setStyleProvider(e -> "d" + getCode(e.getDomain()) + "3");

        } catch (Exception e) {
            System.out.println("BudgetDashboard: error in setAllStyles()");
            e.printStackTrace();
        }

    }
    private String getCode(String domain) {
        switch (domain.toUpperCase())  {
            case "IN-STORE":
                return "g";
            case "ECOM":
                return "e";
            case "ONLINE":
                return "o";
            case "OCH":
                return "c";
            default:
                return "t";
        }
    }

    // *** UI functions
    @Subscribe("cmbBudget")
    public void onCmbBudgetValueChange(HasValue.ValueChangeEvent<Budget> event) {
        budget = event.getValue();
        prioThreshold = Objects.requireNonNull(event.getValue()).getPrioThreshold();
        // updates the dataset for the capacity planning
        updateDashboard();
        chkB.setValue(true);
        chkD.setValue(true);
        chkC.setValue(true);
        chkMD.setValue(false);
        chkEuro.setValue(true);
        chkT.setValue(true);
        chkQ.setValue(false);
        chkY.setValue(true);
    }

    // graphical switches to show / hide data in the dashboard

    @Subscribe("chkD")
    public void onChkDValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.isUserOriginated()) {
            manageColumns();
        }
    }
    @Subscribe("chkB")
    public void onChkBValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.isUserOriginated()) {
            manageColumns();
        }
    }
    @Subscribe("chkC")
    public void onChkCValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.isUserOriginated()) {
            manageColumns();
        }
    }
    @Subscribe("chkMD")
    public void onChkMDValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.isUserOriginated()) {
            manageColumns();
        }
    }
    @Subscribe("chkEuro")
    public void onChkEuroValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.isUserOriginated()) {
            manageColumns();
        }
    }
    @Subscribe("chkT")
    public void onChkTValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.isUserOriginated()) {
            manageColumns();
        }
    }
    @Subscribe("chkQ")
    public void onChkQValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.isUserOriginated()) {
            manageColumns();
        }
    }
    @Subscribe("chkY")
    public void onChkYValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.isUserOriginated()) {
            manageColumns();
        }
    }

    private void manageColumns() {
        // set colulmns' styles
        dTable.getColumn("budget").setVisible(chkB.getValue() && chkMD.getValue() && chkY.getValue());
        dTable.getColumn("budgetUMP").setVisible(chkB.getValue() && chkMD.getValue() && chkT.getValue());
        dTable.getColumn("budgetBAU").setVisible(chkB.getValue() && chkMD.getValue() && chkT.getValue());
        dTable.getColumn("budgetRUN").setVisible(chkB.getValue() && chkMD.getValue() && chkT.getValue());
        dTable.getColumn("budgetQ1").setVisible(chkB.getValue() && chkMD.getValue() && chkQ.getValue());
        dTable.getColumn("budgetQ2").setVisible(chkB.getValue() && chkMD.getValue() && chkQ.getValue());
        dTable.getColumn("budgetQ3").setVisible(chkB.getValue() && chkMD.getValue() && chkQ.getValue());
        dTable.getColumn("budgetQ4").setVisible(chkB.getValue() && chkMD.getValue() && chkQ.getValue());

        dTable.getColumn("demand").setVisible(chkD.getValue() && chkMD.getValue() && chkY.getValue());
        dTable.getColumn("demandUMP").setVisible(chkD.getValue() && chkMD.getValue() && chkT.getValue());
        dTable.getColumn("demandBAU").setVisible(chkD.getValue() && chkMD.getValue() && chkT.getValue());
        dTable.getColumn("demandRUN").setVisible(chkD.getValue() && chkMD.getValue() && chkT.getValue());
        dTable.getColumn("demandQ1").setVisible(chkD.getValue() && chkMD.getValue() && chkQ.getValue());
        dTable.getColumn("demandQ2").setVisible(chkD.getValue() && chkMD.getValue() && chkQ.getValue());
        dTable.getColumn("demandQ3").setVisible(chkD.getValue() && chkMD.getValue() && chkQ.getValue());
        dTable.getColumn("demandQ4").setVisible(chkD.getValue() && chkMD.getValue() && chkQ.getValue());

        dTable.getColumn("kEurY").setVisible(chkB.getValue() && chkEuro.getValue() && chkY.getValue());
        dTable.getColumn("kEurUMP").setVisible(chkB.getValue() && chkEuro.getValue() && chkT.getValue());
        dTable.getColumn("kEurBAU").setVisible(chkB.getValue() && chkEuro.getValue() && chkT.getValue());
        dTable.getColumn("kEurRUN").setVisible(chkB.getValue() && chkEuro.getValue() && chkT.getValue());
        dTable.getColumn("kEurQ1").setVisible(chkB.getValue() && chkEuro.getValue() && chkQ.getValue());
        dTable.getColumn("kEurQ2").setVisible(chkB.getValue() && chkEuro.getValue() && chkQ.getValue());
        dTable.getColumn("kEurQ3").setVisible(chkB.getValue() && chkEuro.getValue() && chkQ.getValue());
        dTable.getColumn("kEurQ4").setVisible(chkB.getValue() && chkEuro.getValue() && chkQ.getValue());

        dTable.getColumn("capacity").setVisible(chkC.getValue() && chkMD.getValue());
        dTable.getColumn("kEurCapacity").setVisible(chkC.getValue() && chkEuro.getValue());

    }


    // *** data functions
    private void updateDashboard() {
        // create the list of BDashboard objects to display (detail = false)
        bDashboardsDc.setItems(createDashObjects());
    }

    private List<BDashboard> createDashObjects() {
        // 1. Get all details with P > P.budget
        double bau, ump, run, c1,c2,c3,c4,capa,ceur;
        List<BDashboard> dashboardData = new ArrayList<>();
        // 1. Get the data: all capacities and all details with P <= P.budget
        List<Capacity> capacities = dataManager.load(Capacity.class)
                .query("select e from Capacity e where e.budget = :budget")
                .parameter("budget", budget)
                .fetchPlan("capacities")
                .list();
        List<Detail> details = dataManager.load(Detail.class)
                .query("select e from Detail e where e.budget = :budget and e.priority <= :priority order by e.simpleDomain, e.simplePlatform asc")
                .parameter("budget", budget)
                .parameter("priority", prioThreshold.getId())
                .fetchPlan("details")
                .list();
        List<Team> teams = dataManager.load(Team.class)
                .query("select e from Team e")
                .fetchPlan("teams")
                .list();

        // 2. Extract and fetch the list of simpeDomains
        List<String> simpleDomains = details.stream().filter(e -> e.getTeam()!=null).map(e -> e.getTeam().getSimpleDomain()).distinct().sorted().collect(Collectors.toList());
        for (String simpleDomain: simpleDomains) {
            // Extract and fetch the list of attached simple platforms
            List<String> simplePlatforms = details.stream()
                    .filter(e -> e.getTeam()!=null && simpleDomain.equals(e.getTeam().getSimpleDomain()))
                    .map(e -> e.getTeam().getSimplePlatform())
                    .distinct().sorted().collect(Collectors.toList());
            firstLine = true;
            for (String simplePlatform: simplePlatforms) {
                List<Detail> focus = details.stream()
                        .filter(e-> e.getTeam()!=null && simpleDomain.equals(e.getTeam().getSimpleDomain()) && simplePlatform.equals(e.getTeam().getSimplePlatform()))
                        .collect(Collectors.toList());
                BDashboard bDashboard = dataManager.create(BDashboard.class);
                if (firstLine) {
                    // the domain name is just mentionned once
                    bDashboard.setDomain2Display(simpleDomain);
                    firstLine = false;
                }
                bDashboard.setDomain(simpleDomain);
                bDashboard.setPlatform(utilBean.renamePlatform(simplePlatform));
                bDashboard.setDemand(focus.stream().map(Detail::getMdY).reduce(0.0, Double::sum));

                bDashboard.setDemandBAU(focus.stream().filter(e -> "BAU".equals(e.getType())).map(Detail::getMdY).reduce(0.0, Double::sum));
                bDashboard.setDemandUMP(focus.stream().filter(e -> "UMP".equals(e.getType())).map(Detail::getMdY).reduce(0.0, Double::sum));
                bDashboard.setDemandRUN(focus.stream().filter(e -> "RUN".equals(e.getType())).map(Detail::getMdY).reduce(0.0, Double::sum));

                bDashboard.setDemandQ1(focus.stream().map(Detail::getMdQ1).reduce(0.0, Double::sum));
                bDashboard.setDemandQ2(focus.stream().map(Detail::getMdQ2).reduce(0.0, Double::sum));
                bDashboard.setDemandQ3(focus.stream().map(Detail::getMdQ3).reduce(0.0, Double::sum));
                bDashboard.setDemandQ4(focus.stream().map(Detail::getMdQ4).reduce(0.0, Double::sum));

                bDashboard.setBudgetQ1(focus.stream().filter(Detail::getIncluded).map(Detail::getMdQ1).reduce(0.0, Double::sum));
                bDashboard.setBudgetQ2(focus.stream().filter(Detail::getIncluded).map(Detail::getMdQ2).reduce(0.0, Double::sum));
                bDashboard.setBudgetQ3(focus.stream().filter(Detail::getIncluded).map(Detail::getMdQ3).reduce(0.0, Double::sum));
                bDashboard.setBudgetQ4(focus.stream().filter(Detail::getIncluded).map(Detail::getMdQ4).reduce(0.0, Double::sum));

                bDashboard.setBudgetBAU(focus.stream().filter(e -> "BAU".equals(e.getType()) && e.getIncluded()).map(Detail::getMdY).reduce(0.0, Double::sum));
                bDashboard.setBudgetUMP(focus.stream().filter(e -> "UMP".equals(e.getType()) && e.getIncluded()).map(Detail::getMdY).reduce(0.0, Double::sum));
                bDashboard.setBudgetRUN(focus.stream().filter(e -> "RUN".equals(e.getType()) && e.getIncluded()).map(Detail::getMdY).reduce(0.0, Double::sum));

                bDashboard.setBudget(focus.stream().filter(Detail::getIncluded).map(Detail::getMdY).reduce(0.0, Double::sum));

                // teams specific data
                List<Team> theTeams = teams.stream().filter(e -> simpleDomain.equals(e.getSimpleDomain()) && simplePlatform.equals(e.getSimplePlatform())).collect(Collectors.toList());
                bau= 0.0; ump = 0.0; run = 0.0; capa = 0.0;
                c1 = 0.0; c2 = 0.0; c3 = 0.0; c4 = 0.0; ceur = 0.0;
                for (Team team: theTeams) {
                    // Increase capacity
                    Capacity capacity = capacities.stream().filter(e -> team.equals(e.getTeam())).findFirst().orElse(null);
                    if (capacity != null) {
                        capa = capa + capacity.getMdY();
                        ceur = ceur + (capacity.getMdQ1()*capacity.getRateQ1() + capacity.getMdQ2()*capacity.getRateQ2() + capacity.getMdQ3()*capacity.getRateQ3() + capacity.getMdQ4()*capacity.getRateQ4()) / 1000;
                        // get the cost
                        List<Detail> subFocus = focus.stream().filter(e -> team.equals(e.getTeam())).collect(Collectors.toList());
                        if (subFocus != null) {
                            bau = bau + subFocus.stream().filter(e -> "BAU".equals(e.getType()) && e.getIncluded()).map(Detail::getMdY).reduce(0.0, Double::sum)*(capacity==null ? 0.0 : capacity.getRateY())/1000;
                            ump = ump + subFocus.stream().filter(e -> "UMP".equals(e.getType()) && e.getIncluded()).map(Detail::getMdY).reduce(0.0, Double::sum)*(capacity==null ? 0.0 : capacity.getRateY())/1000;
                            run = run + subFocus.stream().filter(e -> "RUN".equals(e.getType()) && e.getIncluded()).map(Detail::getMdY).reduce(0.0, Double::sum)*(capacity==null ? 0.0 : capacity.getRateY())/1000;
                            c1 = c1 + subFocus.stream().filter(Detail::getIncluded).map(Detail::getMdQ1).reduce(0.0, Double::sum) * capacity.getRateQ1() / 1000;
                            c2 = c2 + subFocus.stream().filter(Detail::getIncluded).map(Detail::getMdQ2).reduce(0.0, Double::sum) * capacity.getRateQ2() / 1000;
                            c3 = c3 + subFocus.stream().filter(Detail::getIncluded).map(Detail::getMdQ3).reduce(0.0, Double::sum) * capacity.getRateQ3() / 1000;
                            c4 = c4 + subFocus.stream().filter(Detail::getIncluded).map(Detail::getMdQ4).reduce(0.0, Double::sum) * capacity.getRateQ4() / 1000;

                        }
                    } else {
                        System.out.println("<!> error: no capacity for the team " + team.getFullName());
                    }


                }
                bDashboard.setCapacity(capa);
                bDashboard.setKEurBAU(bau);
                bDashboard.setKEurUMP(ump);
                bDashboard.setKEurRUN(run);
                bDashboard.setKEurY(bau+run+ump);
                bDashboard.setKEurQ1(c1);
                bDashboard.setKEurQ2(c2);
                bDashboard.setKEurQ3(c3);
                bDashboard.setKEurQ4(c4);
                bDashboard.setKEurCapacity(ceur);

                dashboardData.add(bDashboard);

            }
        }

//                platform = team==null ? " No team" : utilBean.renamePlatform(team.getSimplePlatform());
        return dashboardData;
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