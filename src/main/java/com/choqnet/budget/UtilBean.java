package com.choqnet.budget;

import com.choqnet.budget.entity.Team;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;

@Service
public class UtilBean {
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
        String line, div, domain=null;
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
            switch(level) {
                case 1:
                    line = parent.getTLine();
                    div =  "";
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

                switch(i) {
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
            System.out.println("rank " + i  + " passed");
            i++;
            teams = dataManager.load(Team.class).query("select e from Team e where e.level = " + i).list();
        } while (teams.size()>0);
    }
    public String renamePlatform(String rootName) {
        if (rootName==null) {
            return "";
        }
        switch(rootName) {
            case "BO BB":
                return "Back-Office Bambora";
            case "BO MCH":
                return "Back-Office Multi Channel";
            case "BOL":
                return "Bambora OnLine";
            case "BB Samport":
                return "Bambora In-Store";
            case "Cnx":
                return "Conexflow";
            case "ARM":
                return "Armenia";
            case "CMA":
                return "Operations";
            case "BELCCSWL":
            case "CHECCSSPS":
                return "COO CS";
            default:
                return rootName;
        }
    }

}
