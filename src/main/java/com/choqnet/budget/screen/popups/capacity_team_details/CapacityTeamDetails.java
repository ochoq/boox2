package com.choqnet.budget.screen.popups.capacity_team_details;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.entity.*;
import com.choqnet.budget.entity.datalists.Priority;
import com.choqnet.budget.entity.datalists.TShirt;
import com.choqnet.budget.screen.addprogresstoteam.AddProgressToTeam;
import com.choqnet.budget.screen.changes.Changes;
import com.choqnet.budget.screen.popups.iprb_chooser.IprbChooser;
import com.choqnet.budget.screen.popups.progressedit.ProgressEdit;
import io.jmix.charts.component.SerialChart;
import io.jmix.core.DataManager;
import io.jmix.core.MetadataTools;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.data.impl.ListDataProvider;
import io.jmix.ui.data.impl.MapDataItem;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@UiController("CapacityTeamDetails")
@UiDescriptor("capacity-team-details.xml")
@DialogMode(width = "100%", height = "100%")
public class CapacityTeamDetails extends Screen {
    private static final Logger log = LoggerFactory.getLogger(CapacityTeamDetails.class);
    @Autowired
    private Label<String> title;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private SerialChart capaChart;
    @Autowired
    private CollectionLoader<Detail> detailsDl;
    @Autowired
    private DataGrid<Detail> detailsTable;
    @Autowired
    private UtilBean utilBean;
    @Autowired
    private UiComponents uiComponents;

    private Budget budget;
    private Team team;
    private Double mdY, mdQ1, mdQ2, mdQ3, mdQ4;
    private TShirt tShirt;
    private String iprbRef;
    @Autowired
    private Button btnChangeIPRB;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private Notifications notifications;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private Button btnCloneDetail;
    @Autowired
    private Button btnDeleteDetail;
    @Autowired
    private CollectionContainer<Detail> detailsDc;
    @Autowired
    private CollectionContainer<FollowUp> followUpsDc;
    @Autowired
    private DataGrid<FollowUp> followUpsTable;

    // *** screen's initialisation
    @Subscribe
    public void onInit(InitEvent event) {
        btnChangeIPRB.setEnabled(false);
        btnCloneDetail.setEnabled(false);
        btnDeleteDetail.setEnabled(false);
        // table's style
        detailsTable.getHeaderRow(0).getCell("mdY").setStyleName("boldCell");
        detailsTable.getHeaderRow(0).getCell("mdQ1").setStyleName("boldCell");
        detailsTable.getHeaderRow(0).getCell("mdQ2").setStyleName("boldCell");
        detailsTable.getHeaderRow(0).getCell("mdQ3").setStyleName("boldCell");
        detailsTable.getHeaderRow(0).getCell("mdQ4").setStyleName("boldCell");
        detailsTable.getHeaderRow(0).getCell("mdNY").setStyleName("boldCell");

        for (int i = 1; i < 5; i++) {
            detailsTable.getColumn("mdQ" + i).setStyleProvider(e -> "rightCell");
        }
        detailsTable.getColumn("mdNY").setStyleProvider(e -> "rightCell");
        detailsTable.getColumn("mdY").setStyleProvider(e -> "rightCell");
        // color code on priority
        detailsTable.getColumn("priority")
                .setStyleProvider(detail -> {
                    try {
                        if (detail.getPriority().isLessOrEqual(detail.getBudget().getPrioThreshold())) {
                            return "green";
                        } else {
                            return "red";
                        }
                    } catch (Exception e) {
                        return "gray";
                    }
                });
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        showFollowUp();
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

    public void setContext(CPTeam item, Budget budget) {
        this.budget = budget;
        this.team = dataManager.load(Team.class).query("select e from Team e where e=:team")
                .parameter("team", item.getCapacity().getTeam())
                .list().get(0);
        if (team == null) {
            return;
        }
        setDirectContext(team, budget);
        /*
        this.team = dataManager.load(Team.class).query("select e from Team e where e=:team")
                .parameter("team", item.getCapacity().getTeam())
                .list().get(0);
        if (team == null) {
            return;
        }
        title.setValue(team.getFullName() + " is detailed here, for budget: " + budget.getName());
        // loads the details
        detailsDl.setQuery("select e from Detail e where e.budget = :budget and e.team = :team order by e.iprb.reference asc");
        detailsDl.setParameter("budget", budget);
        detailsDl.setParameter("team", team);
        detailsDl.load();
        // set the columns' editable mode, depending on the budget's lifecycle
        boolean oneQuarterClosed = false;
        for (int i = 1; i < 5; i++) {
            boolean edit = utilBean.giveProp(budget, "closeQ" + i).equals("false");
            oneQuarterClosed = oneQuarterClosed || !edit;
            detailsTable.getColumn("mdQ" + i).setEditable(edit);
            detailsTable.getColumn("mdQ" + i).setStyleProvider(e -> edit ? "rightCell" : "readonly");
        }
        if (oneQuarterClosed) {
            detailsTable.getColumn("mdY").setEditable(false);
            detailsTable.getColumn("mdY").setStyleProvider(detail -> "readonly");
            detailsTable.getColumn("mdNY").setEditable(true);
            detailsTable.getColumn("mdNY").setStyleProvider(detail -> "rightCell");
            detailsTable.getColumn("tShirt").setEditable(false);
            detailsTable.getColumn("tShirt").setStyleProvider(detail -> "readonly");
        }
        drawChart();

         */
    }

    public void setDirectContext(Team directTeam, Budget budget) {
        this.budget = budget;
        this.team = directTeam;
        if (team == null) {
            return;
        }
        title.setValue(team.getFullName() + " is detailed here, for budget: " + budget.getName());
        // loads the details
        detailsDl.setQuery("select e from Detail e where e.budget = :budget and e.team = :team order by e.iprb.reference asc");
        detailsDl.setParameter("budget", budget);
        detailsDl.setParameter("team", team);
        detailsDl.load();
        // set the columns' editable mode, depending on the budget's lifecycle
        boolean oneQuarterClosed = false;
        for (int i = 1; i < 5; i++) {
            boolean edit = utilBean.giveProp(budget, "closeQ" + i).equals("false");
            oneQuarterClosed = oneQuarterClosed || !edit;
            detailsTable.getColumn("mdQ" + i).setEditable(edit);
            detailsTable.getColumn("mdQ" + i).setStyleProvider(e -> edit ? "rightCell" : "cror");
        }
        if (oneQuarterClosed) {
            detailsTable.getColumn("mdY").setEditable(false);
            detailsTable.getColumn("mdY").setStyleProvider(detail -> "cror");
            detailsTable.getColumn("mdNY").setEditable(true);
            detailsTable.getColumn("mdNY").setStyleProvider(detail -> "rightCell");
            detailsTable.getColumn("tShirt").setEditable(false);
            detailsTable.getColumn("tShirt").setStyleProvider(detail -> "cror");
        }
        drawChart();
        showFollowUp();
    }

    // *** UI Customisation
    @Install(to = "detailsTable.team", subject = "editFieldGenerator")
    private Field<Team> editingTeam(DataGrid.EditorFieldGenerationContext<Detail> editorContext) {
        ComboBox<Team> cb = uiComponents.create(ComboBox.NAME);
        cb.setValueSource((ValueSource<Team>) editorContext.getValueSourceProvider().getValueSource("team"));
        cb.setOptionsList(dataManager.load(Team.class).query("select e from Team e where e.selectable = true").list());
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

    // *** UI functions
    @Subscribe("detailsTable")
    public void onDetailsTableSelection(DataGrid.SelectionEvent<Detail> event) {
        btnChangeIPRB.setEnabled(true);
        btnCloneDetail.setEnabled(true);
        btnDeleteDetail.setEnabled(true);
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

    // *** data functions
    @Subscribe("detailsTable")
    public void onDetailsTableEditorOpen(DataGrid.EditorOpenEvent<Detail> event) {
        // saves the values of the effort properties for a further comparison
        mdY = event.getItem().getMdY();
        mdQ1 = event.getItem().getMdQ1();
        mdQ2 = event.getItem().getMdQ2();
        mdQ3 = event.getItem().getMdQ3();
        mdQ4 = event.getItem().getMdQ4();
        tShirt = event.getItem().getTShirt();
        iprbRef = event.getItem().getIprb().getReference();
    }

    @Subscribe("detailsTable")
    public void onDetailsTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Detail> event) {
        if (event.getItem().getTShirt()==null) {
            event.getItem().setTShirt(TShirt.FREE);
        }
        // propagates the changes in values and actually saves them
        if (!event.getItem().getMdQ1().equals(mdQ1) ||
                !event.getItem().getMdQ2().equals(mdQ2) ||
                !event.getItem().getMdQ3().equals(mdQ3) ||
                !event.getItem().getMdQ4().equals(mdQ4)
        ) {
            event.getItem().setTShirt(TShirt.FREE);
            event.getItem().setMdY(event.getItem().getMdQ1() +
                    event.getItem().getMdQ2() +
                    event.getItem().getMdQ3() +
                    event.getItem().getMdQ4());
        } else {
            if (!event.getItem().getMdY().equals(mdY)) {
                Double effort = event.getItem().getMdY();
                event.getItem().setTShirt(TShirt.FREE);
                event.getItem().setMdQ1(effort / 4);
                event.getItem().setMdQ2(effort / 4);
                event.getItem().setMdQ3(effort / 4);
                event.getItem().setMdQ4(effort / 4);
            } else {
                if (event.getItem().getTShirt()!= null && !event.getItem().getTShirt().equals(tShirt)) {
                    double effort = event.getItem().getTShirt().getId() * 1.0;
                    event.getItem().setMdY(effort);
                    event.getItem().setMdQ1(effort / 4);
                    event.getItem().setMdQ2(effort / 4);
                    event.getItem().setMdQ3(effort / 4);
                    event.getItem().setMdQ4(effort / 4);
                }
            }
        }
        // actual recording of the changes
        dataManager.save(event.getItem());
        detailsDl.load();
        drawChart();
    }

    // *** graphical functions
    private void drawChart() {
        // creation of the chart
        ListDataProvider ldp = new ListDataProvider();
        List<Detail> details = dataManager.load(Detail.class)
                .query("select e from Detail e where e.team = :team and e.budget = :budget")
                .parameter("team", team)
                .parameter("budget", budget)
                .list();
        Capacity capacity = dataManager.load(Capacity.class)
                .query("select e from Capacity e where e.team = :team and e.budget = :budget")
                .parameter("team", team)
                .parameter("budget", budget)
                .list()
                .get(0);
        for (int i = 1; i < 5; i++) {
            MapDataItem mdi = new MapDataItem();
            mdi.add("quarter", "Q" + i);
            Double value;
            int finalI = i;
            value = details.stream().filter(e -> Priority.P1.equals(e.getPriority())).map(e -> e.getQx(finalI)).reduce(Double::sum).orElse(0.0);
            mdi.add("p1", details.stream().filter(e -> Priority.P1.equals(e.getPriority())).map(e -> e.getQx(finalI)).reduce(Double::sum).orElse(0.0));
            mdi.add("p2", details.stream().filter(e -> Priority.P2.equals(e.getPriority())).map(e -> e.getQx(finalI)).reduce(Double::sum).orElse(0.0));
            mdi.add("p3", details.stream().filter(e -> Priority.P3.equals(e.getPriority())).map(e -> e.getQx(finalI)).reduce(Double::sum).orElse(0.0));
            mdi.add("p4", details.stream().filter(e -> Priority.P4.equals(e.getPriority())).map(e -> e.getQx(finalI)).reduce(Double::sum).orElse(0.0));
            mdi.add("p5", details.stream().filter(e -> Priority.P5.equals(e.getPriority())).map(e -> e.getQx(finalI)).reduce(Double::sum).orElse(0.0));
            mdi.add("capacity", capacity.getMdQx(finalI));
            ldp.addItem(mdi);
        }
        capaChart.setDataProvider(ldp);
    }

    // Clones the element selected in the table of details.
    @Subscribe("btnCloneDetail")
    public void onBtnCloneDetailClick(Button.ClickEvent event) {
        Detail source = detailsTable.getSingleSelected();
        Detail target = dataManager.create(Detail.class);
        UUID uuid = target.getId();
        metadataTools.copy(source, target);
        target.setBudget(source.getBudget());
        target.setId(uuid);
        target.setProgress(source.getProgress());
        target.setTeam(source.getTeam());
        target.setIprb(source.getIprb());
        dataManager.save(target);
        detailsDl.load();

    }

    @Subscribe("btnAdd")
    public void onBtnAddClick(Button.ClickEvent event) {
        AddProgressToTeam addProgressToTeam = screenBuilders.screen(this)
                .withScreenClass(AddProgressToTeam.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    detailsDl.load();
                })
                .build();
        addProgressToTeam.setContent(budget, team);
        addProgressToTeam.show();

    }

    @Subscribe("btnDeleteDetail")
    public void onBtnDeleteDetailClick(Button.ClickEvent event) {
        // build the list of all progress' details but the removed ones
        List<Detail> dets = new ArrayList<>();
        Detail target = detailsTable.getSingleSelected();
        Progress progress = target.getProgress();
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

    @Install(to="detailsTable.linkToIPRB", subject = "columnGenerator")
    private Component linkIPRB(DataGrid.ColumnGeneratorEvent<Detail> cge) {
        LinkButton linkButton = uiComponents.create(LinkButton.NAME);
        linkButton.setIconFromSet(JmixIcon.VIEW_ACTION);
        linkButton.addClickListener(e -> {
            ProgressEdit progressEdit = screenBuilders.screen(this)
                    .withScreenClass(ProgressEdit.class)
                    .withOpenMode(OpenMode.DIALOG)
                    .build();
            progressEdit.show();
            Progress prog = cge.getItem().getProgress();
            progressEdit.setContext(prog);
            closeWithDefaultAction();
        });
        return linkButton;
    }


    public void showFollowUp() {
        // create the FollowUp records and show them in a table
        String year = DateTimeFormatter.ofPattern("yyyy").format(LocalDate.now());
        List<Detail> dets = detailsDc.getItems().stream().filter(e -> e.getPriority().isLessOrEqual(budget.getPrioThreshold())).collect(Collectors.toList());
        List<Actual> actuals = dataManager.load(Actual.class)
                .query("select e from Actual e where e.finMonth like 'fin" + year +"%' and e.team = :team")
                .parameter("team", team)
                .list();
        List<XLActual> xlActuals = dataManager.load(XLActual.class)
                .query("select e from XLActual e where e.team = :team")
                .parameter("team", team)
                .list();
        // get the list of iprb
        List<FollowUp> followUps = new ArrayList<>();
        List<IPRB> iprbs = Stream.concat(dets.stream().map(e -> e.getIprb()), actuals.stream().map(e -> e.getIprb()).distinct()).collect(Collectors.toList());
        iprbs = Stream.concat(iprbs.stream(), xlActuals.stream().map(e -> e.getIprb())).distinct().collect(Collectors.toList());
        for (IPRB iprb: iprbs) {
            if (iprb!=null) {
                FollowUp followUp = dataManager.create(FollowUp.class);
                followUp.setTeam(team);
                followUp.setIprb(iprb);
                followUp.setDemandQ1(dets.stream().filter(e -> iprb.equals(e.getIprb())).map(e -> e.getMdQ1()).reduce(0.0, Double::sum));
                followUp.setDemandQ2(dets.stream().filter(e -> iprb.equals(e.getIprb())).map(e -> e.getMdQ2()).reduce(0.0, Double::sum));
                followUp.setDemandQ3(dets.stream().filter(e -> iprb.equals(e.getIprb())).map(e -> e.getMdQ3()).reduce(0.0, Double::sum));
                followUp.setDemandQ4(dets.stream().filter(e -> iprb.equals(e.getIprb())).map(e -> e.getMdQ4()).reduce(0.0, Double::sum));
                followUp.setActualQ1(actuals.stream().filter(e -> iprb.equals(e.getIprb())).map(e-> e.getValue(year, "Q1")).reduce(0.0,Double::sum) +
                        xlActuals.stream().filter(e -> iprb.equals(e.getIprb()) && year.equals(e.getYear()) && "Q1".equals(e.getQuarter())).map(e -> e.getEffort()).reduce(0.0, Double::sum));
                followUp.setActualQ2(actuals.stream().filter(e -> iprb.equals(e.getIprb())).map(e-> e.getValue(year, "Q2")).reduce(0.0,Double::sum) +
                        xlActuals.stream().filter(e -> iprb.equals(e.getIprb()) && year.equals(e.getYear()) && "Q2".equals(e.getQuarter())).map(e -> e.getEffort()).reduce(0.0, Double::sum));
                followUp.setActualQ3(actuals.stream().filter(e -> iprb.equals(e.getIprb())).map(e-> e.getValue(year, "Q3")).reduce(0.0,Double::sum) +
                        xlActuals.stream().filter(e -> iprb.equals(e.getIprb()) && year.equals(e.getYear()) && "Q3".equals(e.getQuarter())).map(e -> e.getEffort()).reduce(0.0, Double::sum));
                followUp.setActualQ4(actuals.stream().filter(e -> iprb.equals(e.getIprb())).map(e-> e.getValue(year, "Q4")).reduce(0.0,Double::sum) +
                        xlActuals.stream().filter(e -> iprb.equals(e.getIprb()) && year.equals(e.getYear()) && "Q4".equals(e.getQuarter())).map(e -> e.getEffort()).reduce(0.0, Double::sum));
                followUps.add(followUp);
            }
        }
        followUpsDc.setItems(followUps);
        followUpsTable.repaint();
    }


}