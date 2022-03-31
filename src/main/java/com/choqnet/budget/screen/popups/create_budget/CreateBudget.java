package com.choqnet.budget.screen.popups.create_budget;

import com.choqnet.budget.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@UiController("CreateBudget")
@UiDescriptor("create-budget.xml")
public class CreateBudget extends Screen {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CreateBudget.class);
    @Autowired
    private TextField<String> txtYear;
    @Autowired
    private TextField<String> txtName;
    @Autowired
    private CheckBox chkCapacity;
    @Autowired
    private CheckBox chkDemand;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DataManager dataManager;

    private List<Budget> budgets;

    @Subscribe
    public void onInit(InitEvent event) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy");
        txtYear.setValue(LocalDate.now().format(dtf));
        chkCapacity.setValue(true);
        chkDemand.setValue(true);
        budgets = dataManager.load(Budget.class).all().list();
    }


    @Subscribe("txtYear")
    public void onTxtYearValueChange(HasValue.ValueChangeEvent event) {
        try {
            txtYear.validate();
        } catch (ValidationException ignored) {
        }
    }



    @Subscribe("txtName")
    public void onTxtNameValueChange(HasValue.ValueChangeEvent event) {
        validateName();
    }

    private boolean validateName() {
        Optional<Budget> targetBudget = budgets.stream().filter(o->o.getName().equals(txtName.getValue())).findFirst();
        if (targetBudget.isPresent()) {
            txtName.setStyleName("errorCapture");
            txtName.setDescription(txtName.getValue() + " exists already.");
            return false;
        } else {
            if (txtName.getValue()==null) {
                txtName.setStyleName("errorCapture");
                txtName.setDescription("The name can't be blank.");
                return false;
            } else  {
                txtName.setStyleName("");
                txtName.setDescription("");
                return true;
            }
        }
    }

    @Subscribe("btnCreate")
    public void onBtnCreateClick(Button.ClickEvent event) {
        SaveContext sc = new SaveContext();
        // check on year
        try {
            txtYear.validate();
        } catch (ValidationException e) {
            return;
        }
        // check on name
        if (!validateName()) {
            return;
        }
        // actual creation of the budget
        Budget budget = dataManager.create(Budget.class);
        budget.setYear(txtYear.getValue());
        budget.setName(txtName.getValue());
        dataManager.save(budget);
        // optional - creation of the associated capacities
        if (chkCapacity.isChecked()) {
            List<Team> teams = dataManager.load(Team.class)
                    .condition(PropertyCondition.equal("enabled",true))
                    .list();
            for (Team team: teams) {
                Capacity capacity = dataManager.create(Capacity.class);
                capacity.setBudget(budget);
                capacity.setTeam(team);
                sc.saving(capacity);
            }
        }
        // optional - creation of the demands
        LocalDate budgetDate = LocalDate.parse(txtYear.getValue()+"-01-01");
        if (chkDemand.isChecked()) {
            List<IPRB> iprbs = dataManager.load(IPRB.class)
                    .query("select e from IPRB e where e.endDate IS NULL or e.endDate>= :date")
                    .parameter("date", budgetDate)
                    .list();
            for (IPRB iprb: iprbs)  {
                Progress progress = dataManager.create(Progress.class);
                progress.setIprb(iprb);
                progress.setBudget(budget);
                sc.saving(progress);
//                Demand demand = dataManager.create(Demand.class);
//                demand.setIprb(iprb);
//                demand.setBudget(budget);
//                sc.saving(demand);
            }
        }
        dataManager.save(sc);
        notifications.create()
                .withCaption("Budget created")
                .withDescription("The budget " + txtName.getValue() + " (" + txtYear.getValue() +") is created.")
                .withType(Notifications.NotificationType.TRAY)
                .show();
        closeWithDefaultAction();
    }

    @Subscribe("btnCancel")
    public void onBtnCancelClick(Button.ClickEvent event) {
        closeWithDefaultAction();
    }




}