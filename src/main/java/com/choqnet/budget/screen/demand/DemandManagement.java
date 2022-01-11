package com.choqnet.budget.screen.demand;

import com.choqnet.budget.communications.UserNotification;
import com.choqnet.budget.entity.*;
import com.choqnet.budget.screen.popups.edit_details.EditDetails;
import com.choqnet.budget.screen.popups.edit_expenses.EditExpenses;
import com.choqnet.budget.screen.popups.iprb_selector.IprbSelector;
import com.choqnet.budget.screen.popups.upload_demand.UploadDemand;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UiController("DemandManagement")
@UiDescriptor("demand_management.xml")
public class DemandManagement extends Screen {
    private static final Logger log = LoggerFactory.getLogger(DemandManagement.class);
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ComboBox<Budget> cmbBudget;
    @Autowired
    private Filter filter;

    private Budget budget;
    @Autowired
    private CollectionLoader<Demand> demandsDl;
    @Autowired
    private DataGrid<Demand> demandsTable;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Button btnRemove;
    @Autowired
    private FetchPlans fetchPlans;
    @Autowired
    private Button btnUpload;
    @Autowired
    private CollectionLoader<Detail> detailsDl;
    @Autowired
    private TabSheet screen;

    private boolean demandToUpdate = false;
    private boolean detailsToUpdate = false;
    // todo setup the loading of Detail/Demand data only when opening a tab and only if required
    /*
    RESTE A FAIRE:
    Quand le budget change:
        si onglet actif = demand (par default)
            mettre demand à jour
            mettre demandToUpdate à faux
            mettre detailToUpdate à vrai
        sinon
            mettre details à jour
            mettre detailToUpdate à faux
        mettre demandToUpdate à vrai

    Quand un onglet change:
        si c'est demand
            si demandToUpdate
                mettre demand à jour
                mettre demandToUpdate à faux
        sinon
            si detailToUpdate
                mettre detail à jour
                mettre detailToUpdate à faux
     */

    // *** init & decoration functions
    @Subscribe
    public void onInit(InitEvent event) {
        // upload feature only visible from admin
        btnUpload.setVisible("admin".equals(((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername()));

        List<Budget> budgets = dataManager.load(Budget.class)
                .query("select e from Budget e order by e.year desc, e.name asc")
                .list();
        cmbBudget.setOptionsList(budgets);
        Optional<Budget> prefBudget = budgets.stream().filter(Budget::getPreferred).findFirst();
        prefBudget.ifPresent(value -> cmbBudget.setValue(value));
        // folds the filter
        filter.setExpanded(false);
        btnRemove.setEnabled(false);
        // style if the table
        demandsTable.getHeaderRow(0).getCell("mdY").setStyleName("boldCell");
        demandsTable.getHeaderRow(0).getCell("mdQ1").setStyleName("boldCell");
        demandsTable.getHeaderRow(0).getCell("mdQ2").setStyleName("boldCell");
        demandsTable.getHeaderRow(0).getCell("mdQ3").setStyleName("boldCell");
        demandsTable.getHeaderRow(0).getCell("mdQ4").setStyleName("boldCell");
        demandsTable.getHeaderRow(0).getCell("euroAmount").setStyleName("boldCell");
        for (int i=1; i<5; i++) {
            demandsTable.getColumn("mdQ"+ i).setStyleProvider(e -> "rightCell");
        }
        demandsTable.getColumn("mdY").setStyleProvider(e -> "rightCell");
        demandsTable.getColumn("euroAmount").setStyleProvider(e -> "rightCell");
    }

    // add details / euroDemand buttons
    @Install(to="demandsTable.details", subject = "columnGenerator")
    private Component editDetails(DataGrid.ColumnGeneratorEvent<Demand> columnGeneratorEvent) {
        LinkButton editButton = uiComponents.create(LinkButton.NAME);
        editButton.setIconFromSet(JmixIcon.EDIT);
        editButton.addClickListener(e -> {
            editLabor(columnGeneratorEvent.getItem());
        });
        return editButton;
    }

    @Install(to="demandsTable.services", subject = "columnGenerator")
    private Component editServices(DataGrid.ColumnGeneratorEvent<Demand> columnGeneratorEvent) {
        LinkButton euroButton = uiComponents.create(LinkButton.NAME);
        euroButton.setIconFromSet(JmixIcon.EURO);
        euroButton.addClickListener(e -> {
            editEuro(columnGeneratorEvent.getItem());
        });
        return euroButton;
    }

    // *** UI Functions

    private void editLabor(Demand demand) {
        EditDetails editDetails = screenBuilders.screen(this)
                .withScreenClass(EditDetails.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    notifications.create()
                            .withDescription("Details screen closed")
                            .withPosition(Notifications.Position.BOTTOM_RIGHT)
                            .show();
                    demandsDl.load();
                })
                .build();
        editDetails.setContext(demand);
        editDetails.show();
    }

    private void editEuro(Demand demand) {
        EditExpenses editExpenses = screenBuilders.screen(this)
                .withScreenClass(EditExpenses.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    notifications.create()
                            .withDescription("Extra Expenses screen closed")
                            .withPosition(Notifications.Position.BOTTOM_RIGHT)
                            .show();
                    demandsDl.load();
                })
                .build();
        editExpenses.setContext(demand);
        editExpenses.show();
    }

    @Subscribe("cmbBudget")
    public void onCmbBudgetValueChange(HasValue.ValueChangeEvent event) {
        if (screen.getSelectedTab()!=null) {
            budget = cmbBudget.getValue();
            switch (screen.getSelectedTab().getName()) {
                case "summary":
                    log.info("Budget changes, demand refreshed " + screen.getSelectedTab().getName());
                    demandsDl.setQuery("select e from Demand e where e.budget = :budget order by e.iprb.reference asc");
                    demandsDl.setParameter("budget", cmbBudget.getValue());
                    demandsDl.load();
                    demandToUpdate = false;
                    detailsToUpdate = true;
                    demandsTable.sort("iprb", DataGrid.SortDirection.ASCENDING);
                    break;
                default:
                    log.info("Budget changes, detail refreshed " + screen.getSelectedTab().getName());
                    detailsDl.setQuery("select e from Detail e where e.demand.budget = :budget order by e.demand.iprb.reference asc");
                    detailsDl.setParameter("budget", cmbBudget.getValue());
                    detailsDl.load();
                    demandToUpdate = true;
                    detailsToUpdate = false;
                    break;
            }
        } else {
            // this doesn't happen
        }
    }

    @Subscribe("screen")
    public void onScreenSelectedTabChange(TabSheet.SelectedTabChangeEvent event) {
        switch(event.getSelectedTab().getName()) {
            case "summary":
                if (demandToUpdate) {
                    log.info("summary open and data refreshed");
                    demandsDl.setQuery("select e from Demand e where e.budget = :budget order by e.iprb.reference asc");
                    demandsDl.setParameter("budget", cmbBudget.getValue());
                    demandsDl.load();
                    demandToUpdate = false;
                } else {
                    log.info("summary open and data not refreshed");
                }
                break;
            default:
                if (detailsToUpdate) {
                    log.info("detail / pivot open and data refreshed");
                    detailsDl.setQuery("select e from Detail e where e.demand.budget = :budget order by e.demand.iprb.reference asc");
                    detailsDl.setParameter("budget", cmbBudget.getValue());
                    detailsDl.load();
                    detailsToUpdate = false;
                } else {
                    log.info("detail / pivot open and data not refreshed");
                }
                break;
        }
    }

    @Subscribe("demandsTable")
    public void onDemandsTableSelection(DataGrid.SelectionEvent<Demand> event) {
        btnRemove.setEnabled(true);
    }

    @Subscribe("btnAdd")
    public void onBtnAddClick(Button.ClickEvent event) {
        // add demand lines for existing IPRB, non having a demand already for the budget
        List<IPRB> allIPRBs = dataManager
                .load(IPRB.class)
                .all()
                .list();
        // todo : do not load IPRB already closed
        List<IPRB> usedIPRB = dataManager
                .load(Demand.class)
                .query("select e from Demand e where e.budget = :budget")
                .parameter("budget", budget)
                .list()
                .stream()
                .map(e -> e.getIprb())
                .distinct()
                .collect(Collectors.toList());
        for (IPRB iprb: usedIPRB) {
            allIPRBs.remove(iprb);
        }
        // call a popup to choose IPRB not having a demand yet
        IprbSelector iprbSelector = screenBuilders.screen(this)
                .withScreenClass(IprbSelector.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    demandsDl.load();
                })
                .build();
        iprbSelector.show();
        iprbSelector.setIPRBs(allIPRBs, cmbBudget.getValue());
    }

    @Subscribe("btnUpload")
    public void onBtnUploadClick(Button.ClickEvent event) {
        UploadDemand uploadDemand = screenBuilders.screen(this)
                .withScreenClass(UploadDemand.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    notifications.create()
                            .withType(Notifications.NotificationType.TRAY)
                            .withCaption("File upload utility closed.")
                            .show();
                    demandsDl.load();
                })
                .build();
        uploadDemand.setBudget(budget);
        uploadDemand.show();

    }

    @Subscribe("btnRemove")
    public void onBtnRemoveClick(Button.ClickEvent event) {
        for (Demand demand: demandsTable.getSelected()) {
            FetchPlan fetchPlan = fetchPlans.builder(Detail.class)
                    .addFetchPlan(FetchPlan.BASE)
                    .add("demand")
                    .build();
            List<Detail> details = dataManager.load(Detail.class)
                    .query("select e from Detail e where e.demand = :demand")
                    .parameter("demand", demand)
                    .fetchPlan(fetchPlan)
                    .list();
            dataManager.remove(details);
            fetchPlan = fetchPlans.builder(Expense.class)
                    .addFetchPlan(FetchPlan.BASE)
                    .add("demand")
                    .build();
            List<Expense> expenses = dataManager.load(Expense.class)
                    .query("select e from Expense e where e.demand = :demand")
                    .parameter("demand", demand)
                    .fetchPlan(fetchPlan)
                    .list();
            dataManager.remove(expenses);
        }
        demandsDl.load();
        for (Demand demand: demandsTable.getSelected()) {
            dataManager.remove(demand);
        }
        demandsDl.load();
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

    @Subscribe("detailsTable")
    public void onDetailsTableSelection(Table.SelectionEvent<Detail> event) {
        Detail detail = event.getSource().getSingleSelected();
        log.info(detail.getTFullName());
        log.info(detail.getTeam().getFullName());
        log.info(detail.getTeam().getParent().getFullName());
        log.info("");
    }





}