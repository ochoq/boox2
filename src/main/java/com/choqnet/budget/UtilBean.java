package com.choqnet.budget;

import com.choqnet.budget.entity.*;
import io.jmix.audit.entity.EntityLogItem;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UtilBean {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UtilBean.class);
    @Autowired
    private DataManager dataManager;

    public String giveProp(Object o, String property) {
        String methodName = "get" + property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
        Class clazz = o.getClass();
        Method method = null;
        try {
            method = clazz.getMethod(methodName, null);
            return  method.invoke(o, null).toString();
        } catch (Exception e) {
            return "???";
        }
    }

    // *** data management functions
    public void HierarchicalData(Team team) {
        int level;
        String fullName;
        String line, div, domain;
        if (team.getParent()==null) {
            level = 0;
            fullName = team.getName();
            line = fullName;
            div = "";
            domain = "";

        } else {
            level = team.getParent().getLevel() + 1;
            fullName = team.getParent().getFullName() + "-" + team.getName();
            Team parent = dataManager.load(Team.class).query("select e from Team e where e.name = :parent").parameter("parent", team.getParent().getName()).one();
            switch (level) {
                case 1:
                    line = parent.getTLine();
                    div = "";
                    domain = "";
                    break;
                case 2:
                    line = parent.getTLine();
                    div = team.getName();
                    domain = "";
                    break;
                case 3:
                    line = parent.getTLine();
                    div = parent.getTDiv();
                    domain = team.getName();
                    break;
                default:
                    line = parent.getTLine();
                    div = parent.getTDiv();
                    domain = parent.getTDomain();
                    break;
            }
        }
        team.setLevel(level);
        team.setFullName(fullName);
        team.setTLine(line);
        team.setTDiv(div);
        team.setTDomain(domain);
        dataManager.save(team);
        List<Team> children = dataManager.load(Team.class)
                .query("select e from Team e where e.parent = :parent")
                .parameter("parent", team)
                .fetchPlan("teams")
                .list();
        for (Team child: children) {
            HierarchicalData(child);
        }
    }
    public void setHierarchy() {
        List<Team> teams = dataManager.load(Team.class).query("select e from Team e where e.level = 0").list();
        int i = 0;
        SaveContext sc = new SaveContext();
        do {
            System.out.println(i + " starting with " + teams.size() + " items");
            for (Team team: teams) {

                switch (i) {
                    case 0:
                        team.setTLine(team.getName());
                        team.setTDiv("");
                        team.setTDomain("");
                        break;
                    case 1:
                        team.setTLine(team.getParent().getTLine());
                        team.setTDiv("");
                        team.setTDomain("");
                        break;
                    case 2:
                        team.setTLine(team.getParent().getTLine());
                        team.setTDiv(team.getName());
                        team.setTDomain("");
                        break;
                    case 3:
                        team.setTLine(team.getParent().getTLine());
                        team.setTDiv(team.getParent().getTDiv());
                        team.setTDomain(team.getName());
                        break;
                    default:
                        team.setTLine(team.getParent().getTLine());
                        team.setTDiv(team.getParent().getTDiv());
                        team.setTDomain(team.getParent().getTDomain());
                        break;
                }
                sc.saving(team);
            }
            dataManager.save(sc);
            i++;
            teams = dataManager.load(Team.class).query("select e from Team e where e.level = " + i).list();
        } while (teams.size()>0);
    }
    public String renamePlatform(String rootName) {
        if (rootName==null) {
            return "";
        }
        switch (rootName) {
            case "BO BB": return "Back-Office Bambora";
            case "BO MCH": return "Back-Office Multi Channel";
            case "BOL": return "Bambora OnLine";
            case "BB Samport": return "Bambora In-Store";
            case "Cnx": return "Conexflow";
            case "ARM": return "Armenia";
            case "CMA": return "Operations";
            case "BELCCSWL":
            case"CHECCSSPS": return "COO CS";
            default: return rootName;
        }
    }
    public void deleteProgress(Progress progress) {
        for (Detail detail : progress.getDetails()) {
            detail.setProgress(null);
            detail = dataManager.save(detail);
            dataManager.remove(detail);
        }
        for (Expense expense : progress.getExpenses()) {
            expense.setProgress(null);
            expense = dataManager.save(expense);
            dataManager.remove(expense);
        }
        progress.setDetails(null);
        progress.setExpenses(null);

        dataManager.remove(progress);

    }

    public void moveDetail(Progress source, Progress target, Detail detail) {
        // remove detail from the current parent Progress (source)
        List<Detail> dets = new ArrayList<>();
        for (Detail det: source.getDetails()) {
            if (!det.equals(detail)) {
                dets.add(det);
            }
        }
        source.setDetails(dets);
        source = setProgressData(source);
        dataManager.save(source);
        // register detail in target
        dets = new ArrayList<>(target.getDetails());
        dets.add(detail);
        target.setDetails(dets);
        target = setProgressData(target);
        dataManager.save(target);
        // move detail from source to target
        detail.setProgress(target);
        detail.setIprb(target.getIprb());
        dataManager.save(detail);
    }

    public void connectDetailProgress(Detail detail, Progress progress) {
        detail.setProgress(progress);
        dataManager.save(detail);
        List<Detail> dets = new ArrayList<>(progress.getDetails());
        dets.add(detail);
        progress.setDetails(dets);
        dataManager.save(progress);
    }

    public void connectExpenseProgress(Expense expense, Progress progress) {
        expense.setProgress(progress);
        dataManager.save(expense);
        List<Expense> exps = new ArrayList<>(progress.getExpenses());
        exps.add(expense);
        progress.setExpenses(exps);
        dataManager.save(progress);
    }

    // ***** DATE MANAGEMENT METHODS
    final int START_FINANCE_MONTH = 24;
    public SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");

    public Date getFinanceMonthStart(Date date) {
        // last previous 24th of a month
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (cal.get(Calendar.DATE) < START_FINANCE_MONTH) {
            cal.add(Calendar.MONTH, -1);
        }
        cal.set(Calendar.DATE, START_FINANCE_MONTH);
        return cal.getTime();
    }

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


    // *** functions of attribute dependencies
    public Progress setProgressData(Progress progress) {
        if (progress.getDetails()==null || progress.getBudget()==null) {
            progress.setDemandQ1(0.0);
            progress.setDemandQ2(0.0);
            progress.setDemandQ3(0.0);
            progress.setDemandQ4(0.0);
            progress.setBudgetCost(0.0);
        } else {
            progress.setDemandQ1(progress.getDetails().stream()
                    .filter(o -> o.getPriority() != null && o.getPriority().isLessOrEqual(progress.getBudget().getPrioThreshold()))
                    .map(Detail::getMdQ1)
                    .reduce(0.0, Double::sum));
            progress.setDemandQ2(progress.getDetails().stream()
                    .filter(o -> o.getPriority() != null && o.getPriority().isLessOrEqual(progress.getBudget().getPrioThreshold()))
                    .map(Detail::getMdQ2)
                    .reduce(0.0, Double::sum));
            progress.setDemandQ3(progress.getDetails().stream()
                    .filter(o -> o.getPriority() != null && o.getPriority().isLessOrEqual(progress.getBudget().getPrioThreshold()))
                    .map(Detail::getMdQ3)
                    .reduce(0.0, Double::sum));
            progress.setDemandQ4(progress.getDetails().stream()
                    .filter(o -> o.getPriority() != null && o.getPriority().isLessOrEqual(progress.getBudget().getPrioThreshold()))
                    .map(Detail::getMdQ4)
                    .reduce(0.0, Double::sum));
            progress.setDemandNY(progress.getDetails().stream()
                    .filter(o -> o.getPriority() != null && o.getPriority().isLessOrEqual(progress.getBudget().getPrioThreshold()))
                    .map(Detail::getMdNY)
                    .reduce(0.0, Double::sum));
            progress.setBudgetCost(progress.getDetails().stream().map(Detail::getBudgetCost).filter(Objects::nonNull).reduce(0.0, Double::sum));
            progress.setBudgetCostQ1(progress.getDetails().stream().map(Detail::getBudgetCostQ1).filter(Objects::nonNull).reduce(0.0, Double::sum));
            progress.setBudgetCostQ2(progress.getDetails().stream().map(Detail::getBudgetCostQ2).filter(Objects::nonNull).reduce(0.0, Double::sum));
            progress.setBudgetCostQ3(progress.getDetails().stream().map(Detail::getBudgetCostQ3).filter(Objects::nonNull).reduce(0.0, Double::sum));
            progress.setBudgetCostQ4(progress.getDetails().stream().map(Detail::getBudgetCostQ4).filter(Objects::nonNull).reduce(0.0, Double::sum));
        }
        if (progress.getExpenses()==null) {
            progress.setExpense(0.0);
        } else {
            progress.setExpense(progress.getExpenses().stream().filter(Expense::getAccepted).map(Expense::getAmountKEuro).reduce(0.0, Double::sum));
        }
        return progress;
    }

    public Capacity setCapacityData(Capacity capacity) {
        // useless, as the quarterly values of Rate and FTE are directly calculated in the entity Capacity
        /*
        if (capacity.getBudget()==null || capacity.getTeam()==null) {
            capacity.setNbWorkingDays(0);
            //capacity.setRateY(0.0);
            capacity.setRateQ1(0.0);
            capacity.setRateQ2(0.0);
            capacity.setRateQ3(0.0);
            capacity.setRateQ4(0.0);
            capacity.setFteQ1(0.0);
            capacity.setFteQ2(0.0);
            capacity.setFteQ3(0.0);
            capacity.setFteQ4(0.0);
        } else {
            capacity.setNbWorkingDays(capacity.getTeam().getWorkDays(capacity.getBudget().getYear()));
            //capacity.setRateY(1.0 * capacity.getTeam().getRate("xxx" + capacity.getBudget().getYear()));
            capacity.setRateQ1(1.0 * capacity.getTeam().getRateQx("xxx" + capacity.getBudget().getYear()+"01"));
            capacity.setRateQ2(1.0 * capacity.getTeam().getRateQx("xxx" + capacity.getBudget().getYear()+"04"));
            capacity.setRateQ3(1.0 * capacity.getTeam().getRateQx("xxx" + capacity.getBudget().getYear()+"07"));
            capacity.setRateQ4(1.0 * capacity.getTeam().getRateQx("xxx" + capacity.getBudget().getYear()+"10"));
            capacity.setFteQ1(capacity.getMdQ1() * 4 / capacity.getTeam().getWorkDays(capacity.getBudget().getYear()));
            capacity.setFteQ2(capacity.getMdQ2() * 4 / capacity.getTeam().getWorkDays(capacity.getBudget().getYear()));
            capacity.setFteQ3(capacity.getMdQ3() * 4 / capacity.getTeam().getWorkDays(capacity.getBudget().getYear()));
            capacity.setFteQ4(capacity.getMdQ4() * 4 / capacity.getTeam().getWorkDays(capacity.getBudget().getYear()));
        }
        */
        return capacity;
    }

    public Detail setDetailData(Detail detail) {
        if (detail.getBudget()== null || detail.getTeam() == null || detail.getTeam().getSetup()==null ) {
            detail.setBudgetCostQ1(0.0);
            detail.setBudgetCostQ2(0.0);
            detail.setBudgetCostQ3(0.0);
            detail.setBudgetCostQ4(0.0);
            detail.setBudgetCost(0.0);
        } else {
            //detail.setBudgetCost(detail.getTeam().getSetup().getRate("xxx" + detail.getBudget().getYear()) * detail.getMdY() / 1000);
            detail.setBudgetCostQ1(detail.getTeam().getRateQx("xxx" + detail.getBudget().getYear() + "01") * detail.getMdQ1() / 1000);
            detail.setBudgetCostQ2(detail.getTeam().getRateQx("xxx" + detail.getBudget().getYear() + "04") * detail.getMdQ2() / 1000);
            detail.setBudgetCostQ3(detail.getTeam().getRateQx("xxx" + detail.getBudget().getYear() + "07") * detail.getMdQ3() / 1000);
            detail.setBudgetCostQ4(detail.getTeam().getRateQx("xxx" + detail.getBudget().getYear() + "10") * detail.getMdQ4() / 1000);
            detail.setBudgetCost(detail.getBudgetCostQ1()+detail.getBudgetCostQ2()+ detail.getBudgetCostQ3()+detail.getBudgetCostQ4());
        }
        return detail;
    }

    // *** HISTORY MANAGEMENT
    public List<ChangeHistory> giveChangeHistory(UUID uuid) {
        List<ChangeHistory> changeHistoryList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<EntityLogItem> entityLogItemList = dataManager
                .load(EntityLogItem.class)
                .query("select e from audit_EntityLog e where e.entityRef.entityId = :id order by e.eventTs")
                .parameter("id", uuid)
                .list();
        for (EntityLogItem entityLogItem: entityLogItemList) {
            //System.out.println(entityLogItem.getUsername() + " - " + entityLogItem.getCreateTs() + " --> " + entityLogItem.getType());
            //ChangeHistory changeHistory = dataManager.create(ChangeHistory.class);
            //changeHistory.setWho(entityLogItem.getUsername());
            //changeHistory.setWhen(dateFormat.format(entityLogItem.getCreateTs().getTime()));
            if (entityLogItem.getType()== EntityLogItem.Type.CREATE) {
                ChangeHistory changeHistory = dataManager.create(ChangeHistory.class);
                changeHistory.setWho(entityLogItem.getUsername());
                changeHistory.setWhen(dateFormat.format(entityLogItem.getCreateTs().getTime()));
                changeHistory.setAttribute("CREATION");
                changeHistoryList.add(changeHistory);

            } else {
                // process the changes one by one
                String[] changes = getReadableChanges(entityLogItem.getChanges());
                // fetch the changes to process all new values
                for (String change: changes) {
                    if (!change.contains("-oldVl")) {
                        ChangeHistory changeHistory = dataManager.create(ChangeHistory.class);
                        changeHistory.setWho(entityLogItem.getUsername());
                        changeHistory.setWhen(dateFormat.format(entityLogItem.getCreateTs().getTime()));
                        String changed = getOldInstance(getName(change), changes);
                        changeHistory.setAttribute(getName(change));
                        changeHistory.setOldValue(getValue(changed));
                        changeHistory.setNewValue(getValue(change));
                        changeHistoryList.add(changeHistory);
                    }
                }
            }

            //System.out.println("");
        }
        changeHistoryList = changeHistoryList.stream().sorted((ChangeHistory c1, ChangeHistory c2) -> c1.getWhen().compareTo(c2.getWhen())).collect(Collectors.toList());
        return changeHistoryList;
    }

    private String[] getReadableChanges(String changes) {
        /**
         * returns the String changes under a more convenient format: a list of single changes
         */
        return changes.split("\r\n");
    }

    private String getValue(String attribute) {
        if (attribute==null || !attribute.contains("=")) return attribute;
        return attribute.substring(attribute.indexOf("=")+1);
    }
    private String getName(String attribute) {
        if (!attribute.contains("=")) return attribute;
        return attribute.substring(0,attribute.indexOf("="));
    }

    private String getOldInstance(String attribute, String[] changes) {
        for (String change: changes) {
            if (change.contains(attribute + "-oldVl")) {
                return change;
            }
        }
        return null;
    }

    // *** MAINTENANCE TASK
    public void updateDetailProgressAndCapacity() {
        SaveContext sc;
        log.info("Maintenance: update Detail, Progress and Capacity items");
        // programme de maintenance
        List<Detail> details = dataManager.load(Detail.class).all().fetchPlan("details").list();
        log.info(details.size() + " details to process");
        sc = new SaveContext();
        for (Detail detail: details) {
            sc.saving(setDetailData(detail));
        }
        dataManager.save(sc);
        List<Capacity> capacities = dataManager.load(Capacity.class).all().fetchPlan("capacities").list();
        log.info(capacities.size() + " capacities to process");
        sc = new SaveContext();
        for(Capacity capacity: capacities) {
            sc.saving(setCapacityData(capacity));
        }
        dataManager.save(sc);
        List<Progress> progresses = dataManager.load(Progress.class).all().fetchPlan("progAll").list();
        log.info(progresses.size() + " progress items to process");
        sc = new SaveContext();
        for(Progress progress: progresses) {
            sc.saving(setProgressData(progress));
        }
        dataManager.save(sc);
        log.info("Maintenance task done");
    }

    // *** AUTO NUMBERING for IPRB
    public String giveIPRBID() {
        Token token = dataManager.load(Token.class).all().one();

        String newId ="0000" + token.getCounter().toString();
        newId = newId.substring(newId.length()-4);
        token.setCounter(token.getCounter() + 1);
        dataManager.save(token);
        return "ACC" + newId;
    }


}
