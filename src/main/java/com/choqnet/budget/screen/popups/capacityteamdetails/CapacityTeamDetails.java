package com.choqnet.budget.screen.popups.capacityteamdetails;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.entity.*;
import com.choqnet.budget.entity.datalists.Priority;
import com.choqnet.budget.entity.datalists.TShirt;
import com.choqnet.budget.screen.popups.iprb_chooser.IprbChooser;
import io.jmix.charts.component.SerialChart;
import io.jmix.core.DataManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.data.impl.ListDataProvider;
import io.jmix.ui.data.impl.MapDataItem;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import liquibase.pro.packaged.D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;


@UiController("CapacityTeamDetails")
@UiDescriptor("capacity-team-details.xml")
@DialogMode(width = "95%", height = "95%")
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

    // *** screen's initialisation
    @Subscribe
    public void onInit(InitEvent event) {
        btnChangeIPRB.setEnabled(false);
        // table's style
        detailsTable.getHeaderRow(0).getCell("mdY").setStyleName("boldCell");
        detailsTable.getHeaderRow(0).getCell("mdQ1").setStyleName("boldCell");
        detailsTable.getHeaderRow(0).getCell("mdQ2").setStyleName("boldCell");
        detailsTable.getHeaderRow(0).getCell("mdQ3").setStyleName("boldCell");
        detailsTable.getHeaderRow(0).getCell("mdQ4").setStyleName("boldCell");
        for (int i = 1; i < 5; i++) {
            detailsTable.getColumn("mdQ" + i).setStyleProvider(e -> "rightCell");
        }
        detailsTable.getColumn("mdY").setStyleProvider(e -> "rightCell");
        // color code on priority
        detailsTable.getColumn("priority")
                .setStyleProvider(detail -> {
                    try {
                        if (detail.getPriority().isLessOrEqual(detail.getDemand().getBudget().getPrioThreshold())) {
                            return "green";
                        } else {
                            return "red";
                        }
                    } catch (Exception e) {
                        return "gray";
                    }

                });
    }

    public void setContext(CPTeam item, Budget budget) {
        this.budget = budget;
        this.
                team = dataManager.load(Team.class).query("select e from Team e where e=:team")
                .parameter("team", item.getCapacity().getTeam())
                .list().get(0);
        if (team == null) {
            return;
        }
        title.setValue(team.getFullName() + " is detailed here, for budget: " + budget.getName());
        // loads the details
        detailsDl.setQuery("select e from Detail e where e.demand.budget = :budget and e.team = :team order by e.demand.iprb.reference asc");
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
            detailsTable.getColumn("tShirt").setEditable(false);
            detailsTable.getColumn("tShirt").setStyleProvider(detail -> "readonly");
        }
        drawChart();
    }

    // *** UI Customisation
    @Install(to = "detailsTable.team", subject = "editFieldGenerator")
    private Field<Team> editingTeam(DataGrid.EditorFieldGenerationContext<Detail> editorContext) {
        ComboBox<Team> cb = uiComponents.create(ComboBox.NAME);
        cb.setValueSource((ValueSource<Team>) editorContext.getValueSourceProvider().getValueSource("team"));
        cb.setOptionsList(dataManager.load(Team.class).query("select e from Team e where e.selectable = true").list());
        return cb;
    }

    // *** UI functions
    @Subscribe("btnClose")
    public void onBtnCloseClick(Button.ClickEvent event) {
        closeWithDefaultAction();
    }

    @Subscribe("detailsTable")
    public void onDetailsTableSelection(DataGrid.SelectionEvent<Detail> event) {
        btnChangeIPRB.setEnabled(true);
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
        iprbRef = event.getItem().getDemand().getIprb().getReference();
    }

    @Subscribe("detailsTable")
    public void onDetailsTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Detail> event) {
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
                if (!event.getItem().getTShirt().equals(tShirt)) {
                    Double effort = event.getItem().getTShirt().getId() * 1.0;
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
                .query("select e from Detail e where e.team = :team and e.demand.budget = :budget")
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


}