package com.choqnet.budget.screen.myiprbs;

import com.choqnet.budget.entity.*;
import com.choqnet.budget.screen.myiprbselector.MyIprbSelector;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UiController("MyIprbs")
@UiDescriptor("my-Iprbs.xml")
public class MyIprbs extends Screen {
    private static final Logger log = LoggerFactory.getLogger(MyIprbs.class);
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ComboBox<Budget> cmbBudget;

    private Budget _budget;
    private User _user;
    List<MyIPRB> myIPRBList;
    List<Detail> details;
    List<Actual> actuals;
    List<XLActual> xlActuals;
    String userName;
    MyIprbSelector mis;
    boolean showFullName = false;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private CollectionContainer<MyIPRB> myIPRBsDc;
    @Autowired
    private GroupTable<MyIPRB> myTable;
    @Autowired
    private ScreenBuilders screenBuilders;



    @Subscribe
    public void onInit(InitEvent event) {
        initialiseBudgetCombo();
       //loadBudgetData();
        loadMyIPRBs();

    }

    private void loadMyIPRBs() {
        _user = (User) currentAuthentication.getUser();
        userName = _user.getUsername();
        myIPRBList = new ArrayList<>();
        for (IPRB iprb: _user.getMyIprbs()) {
            //createMyIPRBData(iprb);
            createOneIPRBData(iprb);
        }
        myIPRBsDc.setItems(myIPRBList);
    }

    private void createMyIPRBData(IPRB iprb) {
        List<Detail> dets;
        List<Actual> acts;
        List<XLActual> xlActs;
        List<Team> teamList;
        // refine the set if data
        dets = details.stream().filter(e -> iprb.equals(e.getIprb())).collect(Collectors.toList());
        acts = actuals.stream().filter(e -> iprb.equals(e.getIprb())).collect(Collectors.toList());
        xlActs = xlActuals.stream().filter(e -> iprb.equals(e.getIprb())).collect(Collectors.toList());
        Stream<Team> strMain = dets.stream().map(e -> e.getTeam()).distinct();
        Stream<Team> strAdd = acts.stream().map(e -> e.getTeam()).distinct();
        strMain = Stream.concat(strMain, strAdd);
        strAdd = xlActs.stream().map(e -> e.getTeam()).distinct();
        strMain = Stream.concat(strMain, strAdd).distinct();
        teamList = strMain.collect(Collectors.toList());
        // build the data
        for (Team team: teamList) {
            if (team!=null) {
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

    private void initialiseBudgetCombo() {
        List<Budget> budgets = dataManager.load(Budget.class)
                .query("select e from Budget e order by e.year desc, e.name asc")
                .list();
        cmbBudget.setOptionsList(budgets);
        Optional<Budget> prefBudget = budgets.stream().filter(Budget::getPreferred).findFirst();
        prefBudget.ifPresent(value -> cmbBudget.setValue(value));
        prefBudget.ifPresent(value -> _budget = value);
    }

    @Subscribe("cmbBudget")
    public void onCmbBudgetValueChange(HasValue.ValueChangeEvent<Budget> event) {
        if (event.isUserOriginated()) {
            _budget = event.getValue();
            //loadBudgetData();
            loadMyIPRBs();
        }
    }

    private void loadBudgetData() {
        details = dataManager.load(Detail.class)
                .query("select e from Detail e where e.budget = :budget")
                .parameter("budget", _budget)
                .list();
        String year = _budget.getYear();
        actuals = dataManager.load(Actual.class)
                .query("select e from Actual e where e.finMonth like 'fin" + year + "%'")
                .list();
        xlActuals = dataManager.load(XLActual.class)
                .query("select e from XLActual e where e.year = :year")
                .parameter("year", year)
                .list();
        
    }

    @Subscribe("btnRemove")
    public void onBtnRemoveClick(Button.ClickEvent event) {
        if (myTable.getSingleSelected()!=null) {
            IPRB iprb2Remove = myTable.getSingleSelected().getIprb();
            List<IPRB> iprbs = _user.getMyIprbs();
            iprbs.remove(iprb2Remove);
            _user = dataManager.load(User.class).query("select e from User e where e.username = :userName").parameter("userName", userName).one();
            _user.setMyIprbs(iprbs);
            _user = dataManager.save(_user);
            List<MyIPRB> myIPRB2Remove = myIPRBList.stream().filter(e -> iprb2Remove.equals(e.getIprb())).collect(Collectors.toList());
            myIPRBList.removeAll(myIPRB2Remove);
            myIPRBsDc.setItems(myIPRBList);
        }
    }

    @Subscribe("btnAdd")
    public void onBtnAddClick(Button.ClickEvent event) {
        mis = screenBuilders.screen(this)
                .withOpenMode(OpenMode.DIALOG)
                .withScreenClass(MyIprbSelector.class)
                .withAfterCloseListener(e -> connect(mis))
                .build();
        mis.setContext(_user);
        mis.show();
    }

    private void connect(MyIprbSelector mis) {
        List<IPRB> iprbs = mis.getList();
        List<IPRB> initials = _user.getMyIprbs();
        if (iprbs!=null) {
            for (IPRB iprb: iprbs) {
                if (!initials.contains(iprb)) {
                    initials.add(iprb);
                    //createMyIPRBData(iprb);
                    log.info("start connecting " + iprb.getReference());
                    createOneIPRBData(iprb);
                    log.info("finish connecting " + iprb.getReference());
                }
            }
            _user = dataManager.load(User.class).query("select e from User e where e.username = :userName").parameter("userName", userName).one();
            _user.setMyIprbs(initials);
            _user = dataManager.save(_user);
            myIPRBsDc.setItems(myIPRBList);
        }
    }

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
        dets = dets.stream().filter(o->o.getPriority().isLessOrEqual(o.getBudget().getPrioThreshold())).collect(Collectors.toList());
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

        Stream<Team> strMain = dets.stream().map(e -> e.getTeam()).distinct();
        Stream<Team> strAdd = acts.stream().map(e -> e.getTeam()).distinct();
        strMain = Stream.concat(strMain, strAdd);
        strAdd = xlActs.stream().map(e -> e.getTeam()).distinct();
        strMain = Stream.concat(strMain, strAdd).distinct();
        teamList = strMain.collect(Collectors.toList());
        // build the data
        for (Team team: teamList) {
            if (team!=null) {
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