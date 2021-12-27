package com.choqnet.budget;

import com.choqnet.budget.entity.Team;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
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

    public void propagateFullName(Team team) {
        team.setFullName(team.getParent()==null ? team.getName() : team.getParent().getFullName() + "-" + team.getName());
        dataManager.save(team);
        List<Team> children = dataManager.load(Team.class)
                .query("select e from Team e where e.parent = :parent")
                .parameter("parent", team)
                .list();
        for (Team child: children) {
            propagateFullName(child);
        }
    }

}
