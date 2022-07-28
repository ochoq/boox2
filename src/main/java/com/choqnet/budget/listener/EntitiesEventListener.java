package com.choqnet.budget.listener;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.event.EntityChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntitiesEventListener {

    private static final Logger log = LoggerFactory.getLogger(EntitiesEventListener.class);
    @Autowired
    private DataManager dataManager;
    @Autowired
    private UtilBean utilBean;

    @TransactionalEventListener
    public void onBudgetChangedAfterCommit(EntityChangedEvent<Budget> event) {
        if (event.getType()!=EntityChangedEvent.Type.DELETED) {
            if (event.getChanges().isChanged("year") || event.getChanges().isChanged("prioThreshold")) {
                // propagate the change of budget.year
                log.info("Budget Year/prio Changed");
                Budget budget = dataManager.load(event.getEntityId()).joinTransaction(false).one();

                List<Capacity> capacities = dataManager.load(Capacity.class)
                        .query("select e from Capacity e where e.budget = :budget")
                        .parameter("budget", budget)
                        .fetchPlan("capacities")
                        .joinTransaction(false)
                        .list();
                log.info(capacities.size() + " capacities impacted because of the change for " + budget.getName());
                for (Capacity capacity: capacities) {
                    capacity = utilBean.setCapacityData(capacity);
                    dataManager.save(new SaveContext().saving(capacity).setJoinTransaction(false));
                }

                List<Detail> details = dataManager.load(Detail.class)
                        .query("select e from Detail e where e.budget = :budget")
                        .parameter("budget", budget)
                        .fetchPlan("details")
                        .joinTransaction(false)
                        .list();
                log.info(details.size() + " details impacted because of the change for " + budget.getName());
                for (Detail detail: details) {
                    detail = utilBean.setDetailData(detail);
                    dataManager.save(new SaveContext().saving(detail).setJoinTransaction(false));
                }

                List<Progress> progresses = dataManager.load(Progress.class)
                        .query("select e from Progress e where e.budget = :budget")
                        .parameter("budget", budget)
                        .fetchPlan("progAll")
                        .joinTransaction(false)
                        .list();
                log.info(progresses.size() + " progress records impacted because of the change for " + budget.getName());
                for (Progress progress: progresses) {
                    progress = utilBean.setProgressData(progress);
                    dataManager.save(new SaveContext().saving(progress).setJoinTransaction(false));
                }
            }
        }
    }

    @TransactionalEventListener
    public void onSetupChangedAfterCommit(EntityChangedEvent<Setup> event) {
        if (event.getType()!= EntityChangedEvent.Type.DELETED) {

            if (event.getChanges().isChanged("r2022Q1") ||
                    event.getChanges().isChanged("r2022Q2") ||
                    event.getChanges().isChanged("r2022Q3") ||
                    event.getChanges().isChanged("r2022Q4") ||
                    event.getChanges().isChanged("r2023Q1") ||
                    event.getChanges().isChanged("r2023Q2") ||
                    event.getChanges().isChanged("r2023Q3") ||
                    event.getChanges().isChanged("r2023Q4") ||
                    event.getChanges().isChanged("wd2022") ||
                    event.getChanges().isChanged("wd2023")) {
                // propagate the change of Setup.rxxxx
                log.info("Setup Rate/nbWorkingDays changed for 2022 or 2023");
                Setup setup = dataManager.load(event.getEntityId()).joinTransaction(false).one();
                List<Team> teams = dataManager.load(Team.class)
                        .query("select e from Team e where e.setup = :setup")
                        .parameter("setup", setup)
                        .fetchPlan("teams")
                        .joinTransaction(false)
                        .list();
                List<Capacity> capacities = dataManager.load(Capacity.class)
                        .query("select e from Capacity e where e.team in :teams")
                        .parameter("teams", teams)
                        .fetchPlan("capacities")
                        .joinTransaction(false)
                        .list();
                log.info(capacities.size() + " capacities impacted ");
                for (Capacity capacity: capacities) {
                    capacity = utilBean.setCapacityData(capacity);
                    dataManager.save(new SaveContext().saving(capacity).setJoinTransaction(false));
                }
                List<Detail> details = dataManager.load(Detail.class)
                        .query("select e from Detail e where e.team in :teams")
                        .parameter("teams", teams)
                        .fetchPlan("details")
                        .joinTransaction(false)
                        .list();
                log.info(details.size() + " details impacted ");
                for (Detail detail: details) {
                    detail = utilBean.setDetailData(detail);
                    dataManager.save(new SaveContext().saving(detail).setJoinTransaction(false));
                }
                List<Progress> progresses = details.stream().map(Detail::getProgress).distinct().collect(Collectors.toList());
                for (Progress progress: progresses) {
                    progress = utilBean.setProgressData(progress);
                    dataManager.save(new SaveContext().saving(progress).setJoinTransaction(false));
                }
            }
        }
    }

    @TransactionalEventListener
    public void onTeamChangedAfterCommit(EntityChangedEvent<Team> event) {
        if (event.getType()!= EntityChangedEvent.Type.DELETED) {
            if (event.getChanges().isChanged("setup")) {
                Team team = dataManager.load(event.getEntityId()).fetchPlan("teams").joinTransaction(false).one();
                // propagate the change of capacity.team
                log.info("Team setup changed");
                List<Capacity> capacities = dataManager.load(Capacity.class)
                        .query("select e from Capacity e where e.team = :team")
                        .parameter("team", team)
                        .fetchPlan("capacities")
                        .joinTransaction(false)
                        .list();
                log.info(capacities.size() + " capacities impacted because of the setup change for " + team.getFullName());
                for (Capacity capacity: capacities) {
                    capacity = utilBean.setCapacityData(capacity);
                    dataManager.save(new SaveContext().saving(capacity).setJoinTransaction(false));
                }
                List<Detail> details = dataManager.load(Detail.class)
                        .query("select e from Detail e where e.team = :team")
                        .parameter("team", team)
                        .fetchPlan("details")
                        .joinTransaction(false)
                        .list();
                log.info(details.size() + " details impacted ");
                for (Detail detail: details) {
                    detail = utilBean.setDetailData(detail);
                    dataManager.save(new SaveContext().saving(detail).setJoinTransaction(false));
                }
                List<Progress> progresses = details.stream().map(e -> e.getProgress()).distinct().collect(Collectors.toList());
                for (Progress progress: progresses) {
                    progress = utilBean.setProgressData(progress);
                    dataManager.save(new SaveContext().saving(progress).setJoinTransaction(false));
                }
            }
        }
    }

    @TransactionalEventListener
    public void onCapacityChangedAfterCommit(EntityChangedEvent<Capacity> event) {
        if (event.getType()!= EntityChangedEvent.Type.DELETED) {
            Capacity capacity = dataManager.load(event.getEntityId()).fetchPlan("capacities").joinTransaction(false).one();

            if (event.getChanges().isChanged("team") ||
                    event.getChanges().isChanged("mdQ1") ||
                    event.getChanges().isChanged("mdQ2") ||
                    event.getChanges().isChanged("mdQ3") ||
                    event.getChanges().isChanged("mdQ4")) {
                // propagate the change of capacity.team
                capacity = utilBean.setCapacityData(capacity);
            }
            dataManager.save(new SaveContext().saving(capacity).setJoinTransaction(false));
        }

    }

    @TransactionalEventListener
    public void onDetailChangedAfterCommit(EntityChangedEvent<Detail> event) {
        if (event.getType()!= EntityChangedEvent.Type.DELETED) {
            if (event.getChanges().isChanged("team") ||
                    event.getChanges().isChanged("priority") ||
                    event.getChanges().isChanged("mdY") ||
                    event.getChanges().isChanged("mdQ1") ||
                    event.getChanges().isChanged("mdQ2") ||
                    event.getChanges().isChanged("mdQ3") ||
                    event.getChanges().isChanged("mdQ4")) {
                Detail detail = dataManager.load(event.getEntityId()).fetchPlan("details").joinTransaction(false).one();
                log.info("Detail updated");
                detail = utilBean.setDetailData(detail);
                dataManager.save(new SaveContext().saving(detail).setJoinTransaction(false));
            }
        }
    }

    @TransactionalEventListener
    public void onExpenseChangedAfterCommit(EntityChangedEvent<Expense> event) {
        if (event.getType()!= EntityChangedEvent.Type.DELETED) {
            if (event.getChanges().isChanged("accepted") ||
                    event.getChanges().isChanged("amountKEuro")) {
                Expense expense = dataManager.load(event.getEntityId()).fetchPlan("exList").joinTransaction(false).one();
            }
        }
    }

}