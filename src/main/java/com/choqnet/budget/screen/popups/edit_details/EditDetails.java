package com.choqnet.budget.screen.popups.edit_details;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.entity.*;
import com.choqnet.budget.entity.datalists.Priority;
import com.choqnet.budget.entity.datalists.TShirt;
import io.jmix.core.DataManager;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("EditDetails")
@UiDescriptor("edit-details.xml")
@DialogMode(width = "90%", height = "90%")
public class EditDetails extends Screen {

    @Autowired
    private Label<String> title;
    @Autowired
    private CollectionLoader<Detail> detailsDl;
    @Autowired
    private DataGrid<Detail> detailsTable;
    @Autowired
    private UtilBean utilBean;
    @Autowired
    private Filter filter;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Button btnRemove;

    private static final Logger log = LoggerFactory.getLogger(EditDetails.class);
    private Demand demand;
    private Double mdY, mdQ1, mdQ2, mdQ3, mdQ4;
    private TShirt tShirt;

    // *** Initialization functions
    @Subscribe
    public void onInit(InitEvent event) {
        filter.setExpanded(false);
        btnRemove.setEnabled(false);
        // style if the table
        detailsTable.getHeaderRow(0).getCell("mdY").setStyleName("h1r");
        detailsTable.getHeaderRow(0).getCell("mdQ1").setStyleName("h1r");
        detailsTable.getHeaderRow(0).getCell("mdQ2").setStyleName("h1r");
        detailsTable.getHeaderRow(0).getCell("mdQ3").setStyleName("h1r");
        detailsTable.getHeaderRow(0).getCell("mdQ4").setStyleName("h1r");
        for (int i=1; i<5; i++) {
            detailsTable.getColumn("mdQ"+ i).setStyleProvider(e -> "ce1r");
        }
        detailsTable.getColumn("mdY").setStyleProvider(e -> "crotr");
        detailsTable.getColumn("jira").setStyleProvider(e -> "ce1r");
    }

    public void setContext(Demand demand)  {
        title.setValue("Details for IPRB: " + demand.getIprb().getReference() + ", in budget " + demand.getBudget().getName());
        this.demand = demand;
        Budget budget = demand.getBudget();
        // loads the relevant set of data
        detailsDl.setQuery("select e from Detail e where e.demand = :demand");
        detailsDl.setParameter("demand", demand);
        detailsDl.load();
        // set the columns' editable mode, depending on the budget's lifecycle
        boolean oneQuarterClosed = false;
        for (int i = 1; i< 5; i++) {
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
    }

    // *** UI Customisation
    // TEAMS - restricts the list of teams to selectable teams only; display it with a combo
    @Install(to = "detailsTable.team", subject = "editFieldGenerator")
    private Field<Team> editingTeam(DataGrid.EditorFieldGenerationContext<Detail> editorContext) {
        ComboBox<Team> cb = uiComponents.create(ComboBox.NAME);
        cb.setValueSource((ValueSource<Team>) editorContext.getValueSourceProvider().getValueSource("team"));
        cb.setOptionsList(dataManager.load(Team.class).query("select e from Team e where e.selectable = true").list());
        return cb;
    }

    // ONEPAGER - OnePager renderer is a comboBox
    @Install(to = "detailsTable.onePager", subject = "editFieldGenerator")
    private Field<OnePager> editOnePager(DataGrid.EditorFieldGenerationContext efc) {
        ComboBox<OnePager> cb = uiComponents.create(ComboBox.NAME);
        cb.setValueSource((ValueSource<OnePager>) efc.getValueSourceProvider().getValueSource("onePager"));
        cb.setOptionsList(dataManager.load(OnePager.class).all().list());
        return cb;
    }

    // JIRA link management - Edit mode
    @Install(to = "detailsTable.jira", subject = "editFieldGenerator")
    private Field<String> editJIRA(DataGrid.EditorFieldGenerationContext efc) {
        TextField<String> txtJira  = uiComponents.create(TextField.NAME);
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
        int i = heap.length;
        if (i==0) {
            lnk.setCaption(cDetail.getItem().getJira());
        } else {
            lnk.setCaption(heap[i-1]);
        }
        lnk.setTarget("_blank");
        return lnk;
    }

    /*
    // makes the column mdY in readonly if an actual TShirt size is set
    @Install(to = "detailsTable.mdY", subject = "editFieldGenerator")
    private TextField<Double> editMDYear(DataGrid.EditorFieldGenerationContext<Detail> editorContext) {
        TextField<Double> edMDY = uiComponents.create(TextField.NAME);
        edMDY.setValueSource((ValueSource<Double>) editorContext.getValueSourceProvider().getValueSource("mdY"));
        Double dd = editorContext.getItem().getMdY();
        edMDY.setValue(dd);
        edMDY.setEditable(editorContext.getItem().getTShirt()==null || editorContext.getItem().getTShirt().equals(TShirt.FREE));
        return edMDY;
    }
   */

    // *** UI functions
    @Subscribe("btnAdd")
    public void onBtnAddClick(Button.ClickEvent event) {
        // creates a new Detail with the right context
        Detail detail = dataManager.create(Detail.class);
        detail.setDemand(demand);
        detail.setPriority(Priority.P1);
        detail.setTShirt(TShirt.FREE);
        dataManager.save(detail);
        detailsDl.load();
    }

    @Subscribe("detailsTable")
    public void onDetailsTableSelection(DataGrid.SelectionEvent event) {
        btnRemove.setEnabled(detailsTable.getSingleSelected()!=null);
    }

    @Subscribe("btnRemove")
    public void onBtnRemoveClick(Button.ClickEvent event) {
        dataManager.remove(detailsTable.getSelected());
        detailsDl.load();
    }

    @Subscribe("btnClose")
    public void onBtnCloseClick(Button.ClickEvent event) {
        closeWithDefaultAction();
    }

    // *** Data functions
    @Subscribe("detailsTable")
    public void onDetailsTableEditorOpen(DataGrid.EditorOpenEvent<Detail> event) {
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
        dataManager.save(event.getItem());
        detailsDl.load();
    }
}