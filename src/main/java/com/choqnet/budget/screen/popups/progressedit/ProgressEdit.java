package com.choqnet.budget.screen.popups.progressedit;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.entity.*;
import com.choqnet.budget.entity.datalists.TShirt;
import com.choqnet.budget.screen.changes.Changes;
import com.choqnet.budget.screen.popups.capacity_team_details.CapacityTeamDetails;
import com.choqnet.budget.screen.popups.iprb_chooser.IprbChooser;
import io.jmix.audit.EntityLog;
import io.jmix.audit.entity.EntityLogAttr;
import io.jmix.audit.entity.EntityLogItem;
import io.jmix.audit.entity.LoggedAttribute;
import io.jmix.audit.entity.LoggedEntity;
import io.jmix.core.DataManager;
import io.jmix.core.MetadataTools;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UiController("ProgressEdit")
@UiDescriptor("progress-edit.xml")
@DialogMode(width = "100%", height = "100%")
public class ProgressEdit extends Screen {
    private static final Logger log = LoggerFactory.getLogger(ProgressEdit.class);
    private Progress progress;
    private Budget budget;
    private Double mdY, mdQ1, mdQ2, mdQ3, mdQ4;
    private TShirt tShirt;
    private List<Detail> dets;
    private List<Expense> exps;
    private Detail editedDetail;
    private Expense editedExpense;

    @Autowired
    private CollectionLoader<Detail> detailsDl;
    @Autowired
    private CollectionLoader<Expense> expensesDl;
    @Autowired
    private Label<Serializable> lblTitle;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private DataGrid<Detail> detailsTable;
    @Autowired
    private Filter filterDetails;
    @Autowired
    private Filter filterExpenses;
    @Autowired
    private CollectionContainer<Detail> detailsDc;
    @Autowired
    private DataGrid<Expense> expensesTable;
    @Autowired
    private CollectionContainer<Expense> expensesDc;
    @Autowired
    private UtilBean utilBean;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private Notifications notifications;
    @Autowired
    private CollectionContainer<FollowUp> followUpsDc;
    @Autowired
    private DataGrid<FollowUp> followUpsTable;
    @Autowired
    private CheckBox chkCost;

    @Subscribe
    public void onInit(InitEvent event) {
        filterDetails.setExpanded(false);
        filterExpenses.setExpanded(false);
        chkCost.setValue(false);
        detailsTable.getColumn("budgetCostQ1").setVisible(false);
        detailsTable.getColumn("budgetCostQ2").setVisible(false);
        detailsTable.getColumn("budgetCostQ3").setVisible(false);
        detailsTable.getColumn("budgetCostQ4").setVisible(false);

    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if(progress!=null) {
            showFollowUp();
        }
        followUpsTable.getColumn("demandQ1").setStyleProvider(e -> "cror");
        followUpsTable.getColumn("demandQ2").setStyleProvider(e -> "cror");
        followUpsTable.getColumn("demandQ3").setStyleProvider(e -> "cror");
        followUpsTable.getColumn("demandQ4").setStyleProvider(e -> "cror");
        followUpsTable.getColumn("actualQ1").setStyleProvider(e -> "cror");
        followUpsTable.getColumn("actualQ2").setStyleProvider(e -> "cror");
        followUpsTable.getColumn("actualQ3").setStyleProvider(e -> "cror");
        followUpsTable.getColumn("actualQ4").setStyleProvider(e -> "cror");
//        followUpsTable.getHeaderRow(0).getCell("demandQ1").setStyleName("boldCell");
//        followUpsTable.getHeaderRow(0).getCell("demandQ2").setStyleName("boldCell");
//        followUpsTable.getHeaderRow(0).getCell("demandQ3").setStyleName("boldCell");
//        followUpsTable.getHeaderRow(0).getCell("demandQ4").setStyleName("boldCell");
//        followUpsTable.getHeaderRow(0).getCell("actualQ1").setStyleName("boldCell");
//        followUpsTable.getHeaderRow(0).getCell("actualQ2").setStyleName("boldCell");
//        followUpsTable.getHeaderRow(0).getCell("actualQ3").setStyleName("boldCell");
//        followUpsTable.getHeaderRow(0).getCell("actualQ4").setStyleName("boldCell");

    }

    /**
     * Populates the screen with the data of the selected Progress item
     */
    public void setContext(Progress passProgress) {
        progress = passProgress;
        budget = progress.getBudget();
        if (progress!= null) {
            showFollowUp();
        }
        lblTitle.setValue("Details for " + progress.getIprb().getReference() + " - " + progress.getIprb().getName());
        detailsDl.setQuery("select e from Detail e where e.progress = :progress");
        detailsDl.setParameter("progress", progress);
        detailsDl.load();

        expensesDl.setQuery("select e from Expense e where e.progress = :progress");
        expensesDl.setParameter("progress", progress);
        expensesDl.load();
        setDetailsStylesAndModes();
        setExpensesStylesAndModes();
    }

    private void setExpensesStylesAndModes() {
        //sets the styles and edit/read modes for the expenses table
    }

    private void setDetailsStylesAndModes() {
        // sets the styles and edit/read modes for the detail table
        for (int i = 1; i < 5; i++) {
            detailsTable.getColumn("mdQ" + i).setEditable(!budget.getFrozen() && !budget.getCloseQx(i));
            int finalI = i;
            detailsTable.getColumn("mdQ" + i).setStyleProvider(e -> {
                return (!budget.getFrozen() && !budget.getCloseQx(finalI)) ? "ce1r" : "cror";
            });
        }
        boolean isSomethingLocked = budget.getFrozen() || budget.getCloseQ1() || budget.getCloseQ2() || budget.getCloseQ3() || budget.getCloseQ4();
        // TShirt can't be seen is 1 or more quarter is closed
        detailsTable.getColumn("tShirt").setVisible(!isSomethingLocked);
        detailsTable.getColumn("tShirt").setStyleProvider(e -> "ce1r");
        detailsTable.getColumn("mdY").setVisible(!isSomethingLocked);
        detailsTable.getColumn("mdY").setStyleProvider(e -> "crotr");
        detailsTable.getColumn("mdNY").setVisible(true);
        detailsTable.getColumn("mdNY").setStyleProvider(e -> "ce1r");
        detailsTable.getColumn("remaining").setVisible(isSomethingLocked);
        detailsTable.getColumn("remaining").setStyleProvider(e -> "crotr");
    }

    // *** UI Functions

    // TEAMS - edit mode
    // restricts the list of teams to selectable teams only; display it with a combo
    @Install(to = "detailsTable.team", subject = "editFieldGenerator")
    private Field<Team> editingTeam(DataGrid.EditorFieldGenerationContext<Detail> editorContext) {
        ComboBox<Team> cb = uiComponents.create(ComboBox.NAME);
        cb.setValueSource((ValueSource<Team>) editorContext.getValueSourceProvider().getValueSource("team"));
        cb.setOptionsList(dataManager.load(Team.class).query("select e from Team e where e.selectable = true").list());
        return cb;
    }

    @Install(to = "detailsTable.linkToTeam", subject = "columnGenerator")
    private Component linkTeam(DataGrid.ColumnGeneratorEvent<Detail> cge) {
        LinkButton linkButton = uiComponents.create(LinkButton.NAME);
        linkButton.setIconFromSet(JmixIcon.VIEW_ACTION);
        linkButton.addClickListener(e -> {
            CapacityTeamDetails capacityTeamDetails = screenBuilders.screen(this)
                    .withScreenClass(CapacityTeamDetails.class)
                    .withOpenMode(OpenMode.DIALOG)
                    .build();
            capacityTeamDetails.show();
            Progress prog = cge.getItem().getProgress();
            capacityTeamDetails.setDirectContext(cge.getItem().getTeam(), budget);
            closeWithDefaultAction();
        });
        return linkButton;
    }

    @Subscribe("chkCost")
    public void onChkCostValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        detailsTable.getColumn("budgetCostQ1").setVisible(event.getValue());
        detailsTable.getColumn("budgetCostQ2").setVisible(event.getValue());
        detailsTable.getColumn("budgetCostQ3").setVisible(event.getValue());
        detailsTable.getColumn("budgetCostQ4").setVisible(event.getValue());
    }



    // TEAMS - read mode
    @Install(to = "detailsTable.team", subject = "columnGenerator")
    private Component shortName(DataGrid.ColumnGeneratorEvent<Detail> cDetail) {
        Label<String> label = uiComponents.create(Label.NAME);
        Team team = cDetail.getItem().getTeam();
        label.setValue(team == null ? "???" : team.getName());
        return label;
    }

    // ONEPAGER - OnePager renderer is a comboBox
    @Install(to = "detailsTable.onePager", subject = "editFieldGenerator")
    private Field<OnePager> editOnePager(DataGrid.EditorFieldGenerationContext<OnePager> efc) {
        ComboBox<OnePager> cb = uiComponents.create(ComboBox.NAME);
        cb.setValueSource((ValueSource<OnePager>) efc.getValueSourceProvider().getValueSource("onePager"));
        cb.setOptionsList(dataManager.load(OnePager.class).all().list());
        return cb;
    }

    // JIRA link management - Edit mode
    @Install(to = "detailsTable.jira", subject = "editFieldGenerator")
    private Field<String> editJIRA(DataGrid.EditorFieldGenerationContext<String> efc) {
        TextField<String> txtJira = uiComponents.create(TextField.NAME);
        txtJira.setValueSource((ValueSource<String>) efc.getValueSourceProvider().getValueSource("jira"));
        return txtJira;
    }

    // JIRA link management - Read mode
    @Install(to = "detailsTable.jira", subject = "columnGenerator")
    private Component reachURL(DataGrid.ColumnGeneratorEvent<Detail> cDetail) {
        Link lnk = uiComponents.create(Link.NAME);
        lnk.setUrl(cDetail.getItem().getJira());
        // cut the key of the Initiative
        String[] heap = cDetail.getItem().getJira().split("/");
        lnk.setCaption(heap.length == 0 ? cDetail.getItem().getJira() : heap[heap.length - 1]);
        lnk.setHeightFull();
        lnk.setAlignment(Component.Alignment.MIDDLE_CENTER);
        lnk.setTarget("_blank");
        return lnk;
    }

    // *** Data functions : allow sizing the efforts by using TShirt, or yearly value, or quarterly values
    @Subscribe("detailsTable")
    public void onDetailsTableEditorOpen(DataGrid.EditorOpenEvent<Detail> event) {
        // saves the value of the edited detail
        editedDetail = event.getItem();
        // saves the values of the effort properties for a further comparison
        mdY = event.getItem().getMdY();
        mdQ1 = event.getItem().getMdQ1();
        mdQ2 = event.getItem().getMdQ2();
        mdQ3 = event.getItem().getMdQ3();
        mdQ4 = event.getItem().getMdQ4();
        tShirt = event.getItem().getTShirt();
    }

    @Subscribe("detailsTable")
    public void onDetailsTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Detail> event) {
        Detail detail = event.getItem();
        // DefaultValueProvider for Owner
        if (detail.getTopic() == null || Objects.equals(detail.getTopic(), "")) {
            detail.setTopic(((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
        }
        // propagates the changes in values and actually saves them
        if (!detail.getMdQ1().equals(mdQ1) ||
                !detail.getMdQ2().equals(mdQ2) ||
                !detail.getMdQ3().equals(mdQ3) ||
                !detail.getMdQ4().equals(mdQ4)
        ) {
            detail.setTShirt(TShirt.FREE);
            detail.setMdY(event.getItem().getMdQ1() +
                    detail.getMdQ2() +
                    detail.getMdQ3() +
                    detail.getMdQ4());
        } else {
            if (!detail.getMdY().equals(mdY)) {
                Double effort = detail.getMdY();
                detail.setTShirt(TShirt.FREE);
                detail.setMdQ1(effort / 4);
                detail.setMdQ2(effort / 4);
                detail.setMdQ3(effort / 4);
                detail.setMdQ4(effort / 4);
            } else {
                if (detail.getTShirt() != null && !detail.getTShirt().equals(tShirt)) {
                    double effort = detail.getTShirt().getId() * 1.0;
                    detail.setMdY(effort);
                    detail.setMdQ1(effort / 4);
                    detail.setMdQ2(effort / 4);
                    detail.setMdQ3(effort / 4);
                    detail.setMdQ4(effort / 4);
                }
            }
        }
        dataManager.save(detail);
        // update the parent progress
        dets = new ArrayList<>();
        for (Detail det : progress.getDetails()) {
            if (!det.equals(editedDetail)) {
                dets.add(det);
            }
        }
        dets.add(detail);
        progress.setDetails(dets);
        progress = utilBean.setProgressData(progress);
        dataManager.save(progress);
        detailsDl.load();

        // if only 1 item, it is unselected
        if (detailsDc.getItems().size() == 1) {
            detailsTable.deselectAll();
        }
    }

    @Subscribe("btnAdd")
    public void onBtnAddClick(Button.ClickEvent event) {
        // create a new detail record and connect it to the current Progress
        Detail detail = dataManager.create(Detail.class);
        detail.setProgress(progress);
        detail.setBudget(budget);
        detail.setIprb(progress.getIprb());
        detail = dataManager.save(detail);
        detailsDl.load();
        // add it to the Progress record
        dets = new ArrayList<>();
        dets.addAll(progress.getDetails());
        dets.add(detail);
        progress.setDetails(dets);
        progress = utilBean.setProgressData(progress);
        progress = dataManager.save(progress);
    }

    @Subscribe("btnRemove")
    public void onBtnRemoveClick(Button.ClickEvent event) {
        // build the list of all progress' details but the removed ones
        dets = new ArrayList<>();
        for (Detail det : progress.getDetails()) {
            if (!detailsTable.getSelected().contains(det)) {
                dets.add(det);
            }
        }
        // inject the new value
        progress.setDetails(dets);
        progress = utilBean.setProgressData(progress);
        dataManager.save(progress);
        dataManager.remove(detailsTable.getSelected());
        detailsDl.load();
    }

    @Subscribe("btnClone")
    public void onBtnCloneClick(Button.ClickEvent event) {
        /**
         * Clones the selected detail records
         */
        Detail detail = cloneDetail(detailsTable.getSingleSelected(), progress, budget);
        //detail = dataManager.save(detail);
        detailsDl.load();
        // add it to the Progress record
        dets = new ArrayList<>();
        dets.addAll(progress.getDetails());
        dets.add(detail);
        progress.setDetails(dets);
        progress = utilBean.setProgressData(progress);
        progress = dataManager.save(progress);
    }

    @Subscribe("btnChangeIPRB")
    public void onBtnChangeIPRBClick(Button.ClickEvent event) {
        // Displays the list of IPRB to choose the destination from
        IprbChooser ic = screenBuilders.screen(this)
                .withScreenClass(IprbChooser.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    notifications.create()
                            .withType(Notifications.NotificationType.TRAY)
                            .withCaption("IPRB mover closed.")
                            .show();
                    detailsDl.load();
                })
                .build();
        ic.setContext(detailsTable.getSelected(), budget);
        ic.show();
    }

    @Subscribe("expensesTable")
    public void onExpensesTableEditorOpen(DataGrid.EditorOpenEvent<Expense> event) {
        editedExpense = event.getItem();
    }

    @Subscribe("expensesTable")
    public void onExpensesTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Expense> event) {
        Expense expense = event.getItem();
        dataManager.save(expense);
        // update the parent progress
        exps = new ArrayList<>();
        for (Expense exp : progress.getExpenses()) {
            if (!exp.equals(editedExpense)) {
                exps.add(exp);
            }
        }
        exps.add(expense);
        progress.setExpenses(exps);
        progress = utilBean.setProgressData(progress);
        dataManager.save(progress);
        expensesDl.load();

        // if only 1 item, it is unselected
        if (expensesDc.getItems().size() == 1) {
            expensesTable.deselectAll();
        }
    }

    @Subscribe("btnExpAdd")
    public void onBtnExpAddClick(Button.ClickEvent event) {
        Expense expense = dataManager.create(Expense.class);
        expense.setProgress(progress);
        expense.setBudget(budget);
        expense.setIprb(progress.getIprb());
        expense = dataManager.save(expense);
        expensesDl.load();
        // add it to the Progress record
        exps = new ArrayList<>();
        exps.addAll(progress.getExpenses());
        exps.add(expense);
        progress.setExpenses(exps);
        progress = utilBean.setProgressData(progress);
        progress = dataManager.save(progress);
    }

    @Subscribe("btnExpRemove")
    public void onBtnExpRemoveClick(Button.ClickEvent event) {
        // build the list of all progress' expense but the removed ones
        exps = new ArrayList<>();
        for (Expense exp : progress.getExpenses()) {
            if (!expensesTable.getSelected().contains(exp)) {
                exps.add(exp);
            }
        }
        // inject the new value
        progress.setExpenses(exps);
        progress = utilBean.setProgressData(progress);
        dataManager.save(progress);
        dataManager.remove(expensesTable.getSelected());
        expensesDl.load();
    }

    // *** Debug Functions
    private void printProgress(Progress prog) {
        for (Detail detail : prog.getDetails()) {
            log.info(detail.getTeam() + " " + detail.getMdQ1() + " - " + detail.getMdQ2() + " - " + detail.getMdQ3() + " - " + detail.getMdQ4() + " - ");
        }
        log.info("");

    }


    private Detail cloneDetail(Detail source, Progress progress, Budget budget) {
        Detail target = dataManager.create(Detail.class);
        UUID uuid = target.getId();
        metadataTools.copy(source, target);
        target.setBudget(budget);
        target.setId(uuid);
        target.setProgress(progress);
        target.setTeam(source.getTeam());
        target.setIprb(source.getIprb());
        return dataManager.save(target);
    }

    private void temp() {
        log.info("targeted details start");
        List<EntityLogItem> entityLogItemList = dataManager.load(EntityLogItem.class).query("select e from audit_EntityLog e where e.entity='Detail'").list();

        log.info("targeted details end");
        log.info("globa details start");
        entityLogItemList = dataManager.load(EntityLogItem.class).all().list();
        entityLogItemList = entityLogItemList.stream().filter(e -> "Detail".equals(e.getEntity())).collect(Collectors.toList());
        log.info("globa details end");
        for (EntityLogItem eli: entityLogItemList) {
        }
        log.info("ok");
    }

    @Subscribe("detailsTable")
    public void onDetailsTableContextClick(DataGrid.ContextClickEvent event) {
        List<ChangeHistory> changeHistoryList = utilBean.giveChangeHistory(detailsTable.getSingleSelected().getId());

        Changes changes = screenBuilders.screen(this)
                .withScreenClass(Changes.class)
                .withOpenMode(OpenMode.DIALOG)
                .build();
        changes.show();
        changes.setData(changeHistoryList);
    }

    public void showFollowUp() {
        // create the FollowUp records and show them in a table
        String year = DateTimeFormatter.ofPattern("yyyy").format(LocalDate.now());
        List<Detail> dets = detailsDc.getItems().stream().filter(e -> e.getPriority().isLessOrEqual(budget.getPrioThreshold())).collect(Collectors.toList());
        List<Actual> actuals = dataManager.load(Actual.class)
                .query("select e from Actual e where e.finMonth like 'fin" + year +"%' and e.iprb = :iprb")
                .parameter("iprb", progress.getIprb())
                .list();
        List<XLActual> xlActuals = dataManager.load(XLActual.class)
                .query("select e from XLActual e where e.iprb = :iprb")
                .parameter("iprb", progress.getIprb())
                .list();
        // get the list of iprb
        List<FollowUp> followUps = new ArrayList<>();
        List<Team> teams = Stream.concat(dets.stream().map(e -> e.getTeam()), actuals.stream().map(e -> e.getTeam()).distinct()).collect(Collectors.toList());
        teams = Stream.concat(teams.stream(), xlActuals.stream().map(e -> e.getTeam())).distinct().collect(Collectors.toList());
        for (Team team: teams) {
            if (team!=null) {
                FollowUp followUp = dataManager.create(FollowUp.class);
                followUp.setTeam(team);
                followUp.setTeam(team);
                followUp.setDemandQ1(dets.stream().filter(e -> team.equals(e.getTeam())).map(e -> e.getMdQ1()).reduce(0.0, Double::sum));
                followUp.setDemandQ2(dets.stream().filter(e -> team.equals(e.getTeam())).map(e -> e.getMdQ2()).reduce(0.0, Double::sum));
                followUp.setDemandQ3(dets.stream().filter(e -> team.equals(e.getTeam())).map(e -> e.getMdQ3()).reduce(0.0, Double::sum));
                followUp.setDemandQ4(dets.stream().filter(e -> team.equals(e.getTeam())).map(e -> e.getMdQ4()).reduce(0.0, Double::sum));
                followUp.setActualQ1(actuals.stream().filter(e -> team.equals(e.getTeam())).map(e-> e.getValue(year, "Q1")).reduce(0.0,Double::sum) +
                        xlActuals.stream().filter(e -> team.equals(e.getTeam()) && year.equals(e.getYear()) && "Q1".equals(e.getQuarter())).map(e -> e.getEffort()).reduce(0.0, Double::sum));
                followUp.setActualQ2(actuals.stream().filter(e -> team.equals(e.getTeam())).map(e-> e.getValue(year, "Q2")).reduce(0.0,Double::sum) +
                        xlActuals.stream().filter(e -> team.equals(e.getTeam()) && year.equals(e.getYear()) && "Q2".equals(e.getQuarter())).map(e -> e.getEffort()).reduce(0.0, Double::sum));
                followUp.setActualQ3(actuals.stream().filter(e -> team.equals(e.getTeam())).map(e-> e.getValue(year, "Q3")).reduce(0.0,Double::sum) +
                        xlActuals.stream().filter(e -> team.equals(e.getTeam()) && year.equals(e.getYear()) && "Q3".equals(e.getQuarter())).map(e -> e.getEffort()).reduce(0.0, Double::sum));
                followUp.setActualQ4(actuals.stream().filter(e -> team.equals(e.getTeam())).map(e-> e.getValue(year, "Q4")).reduce(0.0,Double::sum) +
                        xlActuals.stream().filter(e -> team.equals(e.getTeam()) && year.equals(e.getYear()) && "Q4".equals(e.getQuarter())).map(e -> e.getEffort()).reduce(0.0, Double::sum));
                followUps.add(followUp);
            }
        }
        followUpsDc.setItems(followUps);
        followUpsTable.repaint();
    }
}