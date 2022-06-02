package com.choqnet.budget.screen.progressmanagement;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.communications.UserNotification;
import com.choqnet.budget.entity.*;
import com.choqnet.budget.screen.popups.iprb_selector.IprbSelector;
import com.choqnet.budget.screen.popups.progressedit.ProgressEdit;
import io.jmix.core.DataManager;
import io.jmix.core.metamodel.annotation.DateTimeFormat;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UiController("ProgressManagement")
@UiDescriptor("progress-management.xml")
public class ProgressManagement extends Screen {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ProgressManagement.class);
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ComboBox<Budget> cmbBudget;

    private Budget budget;
    @Autowired
    private DataGrid<Progress> progTable;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private Notifications notifications;
    @Autowired
    private UtilBean utilBean;
    @Autowired
    private Filter filter;
    @Autowired
    private CollectionContainer<Progress> progressesDc;
    @Autowired
    private CollectionLoader<Progress> progressesDl;

    @Subscribe
    public void onInit(InitEvent event) {
        initialiseBudgetCombo();
        loadData();
        setStylesAndModes();

    }

    private void initialiseBudgetCombo() {
        List<Budget> budgets = dataManager.load(Budget.class)
                .query("select e from Budget e order by e.year desc, e.name asc")
                .list();
        cmbBudget.setOptionsList(budgets);
        Optional<Budget> prefBudget = budgets.stream().filter(Budget::getPreferred).findFirst();
        prefBudget.ifPresent(value -> cmbBudget.setValue(value));
        prefBudget.ifPresent(value -> budget = value);
    }

    @Subscribe("progTable")
    public void onProgTableItemClick(DataGrid.ItemClickEvent<Progress> event) {
        log.info("progress " + event.getItem().getExpense());
        ProgressEdit pe = screenBuilders.screen(this)
                .withScreenClass(ProgressEdit.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    //updateProgress(event.getItem());
                    loadData();
                    progTable.deselectAll();
                })
                .build();
        pe.setContext(event.getItem());
        pe.show();

    }



    @Subscribe("cmbBudget")
    public void onCmbBudgetValueChange(HasValue.ValueChangeEvent<Budget> event) {
        if (event.isUserOriginated()) {
            budget = event.getValue();
            loadData();
            setStylesAndModes();
        }
    }

    private void loadData() {
        progressesDl.setQuery("select e from Progress e where e.budget = :budget order by e.iprb.reference");
        progressesDl.setParameter("budget", budget);
        progressesDl.load();
    }

    private void setStylesAndModes() {
        progTable.getColumn("demandQ1").setStyleProvider(e -> { return (budget.getFrozen() || budget.getCloseQ1()) ? "cror" : "ce1r";});
        progTable.getColumn("demandQ2").setStyleProvider(e -> { return (budget.getFrozen() || budget.getCloseQ2()) ? "cror" : "ce1r";});
        progTable.getColumn("demandQ3").setStyleProvider(e -> { return (budget.getFrozen() || budget.getCloseQ3()) ? "cror" : "ce1r";});
        progTable.getColumn("demandQ4").setStyleProvider(e -> { return (budget.getFrozen() || budget.getCloseQ4()) ? "cror" : "ce1r";});
        progTable.getColumn("demandNY").setStyleProvider(e -> "ce1r");
        progTable.getColumn("expense").setStyleProvider(e -> { return (budget.getFrozen()) ? "cror" : "ce1r";});
        progTable.getColumn("actualQ1").setStyleProvider(e ->  "cror");
        progTable.getColumn("actualQ2").setStyleProvider(e ->  "cror");
        progTable.getColumn("actualQ3").setStyleProvider(e ->  "cror");
        progTable.getColumn("actualQ4").setStyleProvider(e ->  "cror");
        String quarter = getCurrentQuarter();
        String year = getCurrentYear();
        progTable.getColumn("actualQ1").setVisible(quarter.compareTo("Q1")>=0 || budget.getYear().compareTo(year)<0);
        progTable.getColumn("actualQ2").setVisible(quarter.compareTo("Q2")>=0 || budget.getYear().compareTo(year)<0);
        progTable.getColumn("actualQ3").setVisible(quarter.compareTo("Q3")>=0 || budget.getYear().compareTo(year)<0);
        progTable.getColumn("actualQ4").setVisible(quarter.compareTo("Q4")>=0 || budget.getYear().compareTo(year)<0);
        progTable.getColumn("expectedLanding").setStyleProvider(e -> "cror");
    }

    // *** Utilities
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
    final int START_FINANCE_MONTH = 24;
    public String getFinancialMonth(Date date) {
        // day >= 24 ?  next month : current month
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return "fin" + (cal.get(Calendar.DATE) >= START_FINANCE_MONTH ? monthToString(addTime(date, 1, "MONTH")) : monthToString(date));
    }
    public String monthToString(Date ref) {
        // returns the provided date 'ref' under the common Dumbo's month format
        return sdf.format(giveMonthStart(ref));
    }
    public Date addTime(Date ref, int delta, String unit) {
        // adds 'delta' 'unit' to the provided date 'ref'
        // managed units:  DAY / MONTH / YEAR
        Calendar cal = Calendar.getInstance();
        cal.setTime(ref);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        switch (unit) {
            case "DAY":
                cal.add(Calendar.DATE, delta);
                break;
            case "MONTH":
                cal.add(Calendar.MONTH, delta);
                break;
            case "YEAR":
                cal.add(Calendar.YEAR, delta);
                break;
        }
        return cal.getTime();
    }
    public Date giveMonthStart(Date ref) {
        // returns the first day of the month the provided date 'ref' belongs to
        Calendar cal = Calendar.getInstance();
        cal.setTime(ref);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DATE, 1);
        return cal.getTime();
    }
    private String getCurrentYear() {
        DateTimeFormatter dft = DateTimeFormatter.ofPattern("yyyy");
        return dft.format(LocalDateTime.now());
    }
    private String getCurrentQuarter() {
        String month = getFinancialMonth(new Date()).substring(7);
        switch(month) {
            case "01":
            case "02":
            case "03":
                return "Q1";
            case "04":
            case "05":
            case "06":
                return "Q2";
            case "07":
            case "08":
            case "09":
                return "Q3";
            case "10":
            case "11":
            case "12":
            default:
                return "Q4";
        }
    }

    /**
     * Adds a existing-but-not-yet-connected IPRB to the budget's progress list
     */
    @Subscribe("btnAddIPRB")
    public void onBtnAddIPRBClick(Button.ClickEvent event) {
        // get full list of IPRB
        List<IPRB> allIPRBs = dataManager.load(IPRB.class).all().list();
        // get the already connected ones
        List<IPRB> usedIPRBs = dataManager.load(Progress.class)
                .query("select e from Progress e where e.budget = :budget")
                .parameter("budget", budget)
                .list()
                .stream()
                .map(Progress::getIprb)
                .distinct()
                .collect(Collectors.toList());
        // remove the used IPRB from the full IPRB list
        for (IPRB iprb:usedIPRBs) {
            allIPRBs.remove(iprb);
        }
        // display a popup to choose IPRB not having Progress yet
        IprbSelector iprbSelector = screenBuilders.screen(this)
                .withScreenClass(IprbSelector.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    loadData();
                    notifications.create()
                            .withDescription("IPRB(s) added")
                            .show();
                })
                .build();
        iprbSelector.show();
        iprbSelector.setIPRBs(allIPRBs, budget);
    }

    @Subscribe("btnRemove")
    public void onBtnRemoveClick(Button.ClickEvent event) {
        for (Progress progress : progTable.getSelected()) {
            utilBean.deleteProgress(progress);
        }
        loadData();
    }

    // *** communications
    // --- General Messaging
    @EventListener
    private void received(UserNotification event) {
        notifications.create()
                .withCaption("System Communication")
                .withDescription(">> " + event.getMessage())
                .withType(Notifications.NotificationType.WARNING)
                .withPosition(Notifications.Position.TOP_CENTER)
                .show();
    }
}