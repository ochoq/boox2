package com.choqnet.budget.screen.myiprb;

import com.choqnet.budget.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UiController("MyIprb")
@UiDescriptor("my-IPRB.xml")
public class MyIprb extends Screen {

    private Budget _budget;
    private User _user;
    List<IPRB> myIPRBs;
    List<MyIPRB> myIPRBList;
    Myiprbselector mis;

    @Autowired
    private DataManager dataManager;
    @Autowired
    private ComboBox<Budget> cmbBudget;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private CollectionContainer<MyIPRB> myIPRBsDc;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private GroupTable<MyIPRB> myTable;

    @Subscribe
    public void onInit(InitEvent event) {
        initialiseBudgetCombo();
        loadMyIPRBs();
    }

    @Subscribe("cmbBudget")
    public void onCmbBudgetValueChange(HasValue.ValueChangeEvent<Budget> event) {
        if (event.isUserOriginated()) {
            _budget = event.getValue();
            loadMyIPRBs();
        }
    }

    @Subscribe("btnAdd")
    public void onBtnAddClick(Button.ClickEvent event) {
        mis = screenBuilders.screen(this)
                .withOpenMode(OpenMode.DIALOG)
                .withScreenClass(Myiprbselector.class)
                .withAfterCloseListener(e -> connect(mis))
                .build();
        mis.setContext(_user);
        mis.show();
    }

    @Subscribe("btnRemove")
    public void onBtnRemoveClick(Button.ClickEvent event) {
        if (myTable.getSingleSelected() != null) {
            IPRB iprb2Remove = myTable.getSingleSelected().getIprb();
            // deletes the registration
            List<IPRBRegistration> toDelete = dataManager.load(IPRBRegistration.class)
                    .query("select e from IPRBRegistration e where e.iprb = :iprb and e.user = :user")
                    .parameter("iprb", iprb2Remove)
                    .parameter("user", _user)
                    .list();
            dataManager.remove(toDelete);
            // removes from the display
            List<MyIPRB> myIPRB2Remove = myIPRBList.stream().filter(e -> iprb2Remove.equals(e.getIprb())).collect(Collectors.toList());
            myIPRBList.removeAll(myIPRB2Remove);
            myIPRBsDc.setItems(myIPRBList);
        }
    }

    private void connect(Myiprbselector mis) {
        List<IPRB> iprbs = mis.getList();
        // the list of connected IPRBs is in myIPRBs
        for (IPRB iprb : iprbs) {
            if (!myIPRBs.contains(iprb)) {
                IPRBRegistration newRegsitration = dataManager.create(IPRBRegistration.class);
                newRegsitration.setUser(_user);
                newRegsitration.setIprb(iprb);
                dataManager.save(newRegsitration);
                myIPRBs.add(iprb);
                createOneIPRBData(iprb);
            }
        }
        myIPRBsDc.setItems(myIPRBList);
    }

    // loads the list of IPRB linked to the current user and convert them in MyIPRB for the display.
    private void loadMyIPRBs() {
        _user = (User) currentAuthentication.getUser();
        myIPRBs = dataManager.load(IPRBRegistration.class)
                .query("select e from IPRBRegistration e where e.user = :user")
                .parameter("user", _user)
                .list()
                .stream()
                .map(IPRBRegistration::getIprb)
                .collect(Collectors.toList());
        myIPRBList = new ArrayList<>();
        for (IPRB iprb: myIPRBs) {
            createOneIPRBData(iprb);
        }
        myIPRBsDc.setItems(myIPRBList);
    }

    // fill the Budget combo with the current set of budgets.
    private void initialiseBudgetCombo() {
        List<Budget> budgets = dataManager.load(Budget.class)
                .query("select e from Budget e order by e.year desc, e.name asc")
                .list();
        cmbBudget.setOptionsList(budgets);
        Optional<Budget> prefBudget = budgets.stream().filter(Budget::getPreferred).findFirst();
        prefBudget.ifPresent(value -> cmbBudget.setValue(value));
        prefBudget.ifPresent(value -> _budget = value);
    }

    // create one MyIPRB object for each team having Actuals, XLActuals of Demand connected to the provided IPRB
    // if no teams are connected, one blank line is created for the IPRB
    private void createOneIPRBData(IPRB iprb) {
        List<Detail> dets;
        List<Actual> acts;
        List<XLActual> xlActs;
        List<Team> teamList;
        dets = dataManager.load(Detail.class)
                .query("select e from Detail e where e.budget = :budget and e.iprb = :iprb")
                .parameter("budget", _budget)
                .parameter("iprb", iprb)
                .list();
        dets = dets.stream().filter(o -> o.getPriority().isLessOrEqual(o.getBudget().getPrioThreshold())).collect(Collectors.toList());
        String year = _budget.getYear();
        acts = dataManager.load(Actual.class)
                .query("select e from Actual e where e.finMonth like 'fin" + year + "%' and e.iprb = :iprb")
                .parameter("iprb", iprb)
                .list();
        xlActs = dataManager.load(XLActual.class)
                .query("select e from XLActual e where e.year = :year and e.iprb = :iprb")
                .parameter("year", year)
                .parameter("iprb", iprb)
                .list();

        Stream<Team> strMain = dets.stream().map(Detail::getTeam).distinct();
        Stream<Team> strAdd = acts.stream().map(Actual::getTeam).distinct();
        strMain = Stream.concat(strMain, strAdd);
        strAdd = xlActs.stream().map(XLActual::getTeam).distinct();
        strMain = Stream.concat(strMain, strAdd).distinct();
        teamList = strMain.collect(Collectors.toList());
        // build the data
        // if there are no teams, we add a blank line
        // otherwise we create 1 line per team
        if (teamList.size()==0) {
            MyIPRB myIPRB = dataManager.create(MyIPRB.class);
            myIPRB.setIprb(iprb);
            myIPRB.setName(iprb.getName());
            myIPRB.setReference(iprb.getReference());
            myIPRBList.add(myIPRB);
        } else {
            for (Team team : teamList) {
                if (team != null) {
                    MyIPRB myIPRB = dataManager.create(MyIPRB.class);
                    myIPRB.setIprb(iprb);
                    myIPRB.setName(iprb.getName());
                    myIPRB.setReference(iprb.getReference());
                    myIPRB.setTeam(team.getFullName());
                    myIPRB.setTeamShort(team.getName());
                    myIPRB.setActJIRAMDQ1(acts.stream().filter(e -> e.isQx("01") && team.equals(e.getTeam())).map(Actual::getEffort).reduce(0.0, Double::sum));
                    myIPRB.setActJIRAMDQ2(acts.stream().filter(e -> e.isQx("02") && team.equals(e.getTeam())).map(Actual::getEffort).reduce(0.0, Double::sum));
                    myIPRB.setActJIRAMDQ3(acts.stream().filter(e -> e.isQx("03") && team.equals(e.getTeam())).map(Actual::getEffort).reduce(0.0, Double::sum));
                    myIPRB.setActJIRAMDQ4(acts.stream().filter(e -> e.isQx("04") && team.equals(e.getTeam())).map(Actual::getEffort).reduce(0.0, Double::sum));
                    myIPRB.setActSAPMDQ1(xlActs.stream().filter(e -> "Q1".equals(e.getQuarter()) && team.equals(e.getTeam())).map(XLActual::getEffort).reduce(0.0, Double::sum));
                    myIPRB.setActSAPMDQ2(xlActs.stream().filter(e -> "Q2".equals(e.getQuarter()) && team.equals(e.getTeam())).map(XLActual::getEffort).reduce(0.0, Double::sum));
                    myIPRB.setActSAPMDQ3(xlActs.stream().filter(e -> "Q3".equals(e.getQuarter()) && team.equals(e.getTeam())).map(XLActual::getEffort).reduce(0.0, Double::sum));
                    myIPRB.setActSAPMDQ4(xlActs.stream().filter(e -> "Q4".equals(e.getQuarter()) && team.equals(e.getTeam())).map(XLActual::getEffort).reduce(0.0, Double::sum));
                    myIPRB.setDemMDQ1(dets.stream().filter(e -> team.equals(e.getTeam())).map(Detail::getMdQ1).reduce(0.0, Double::sum));
                    myIPRB.setDemMDQ2(dets.stream().filter(e -> team.equals(e.getTeam())).map(Detail::getMdQ2).reduce(0.0, Double::sum));
                    myIPRB.setDemMDQ3(dets.stream().filter(e -> team.equals(e.getTeam())).map(Detail::getMdQ3).reduce(0.0, Double::sum));
                    myIPRB.setDemMDQ4(dets.stream().filter(e -> team.equals(e.getTeam())).map(Detail::getMdQ4).reduce(0.0, Double::sum));
                    myIPRBList.add(myIPRB);
                }
            }
        }

    }

}