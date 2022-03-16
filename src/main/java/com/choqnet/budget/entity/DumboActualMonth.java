package com.choqnet.budget.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

@JmixEntity
public class DumboActualMonth {

    private String finMonth;

    private Double effort;

    private DumboInit initiative;

    private DumboIPRB iprb;

    private DumboTeam squad;

    private String costCenterRef;

    private DumboJiraProject jiraProject;

    public DumboJiraProject getJiraProject() {
        return jiraProject;
    }

    public void setJiraProject(DumboJiraProject jiraProject) {
        this.jiraProject = jiraProject;
    }

    public String getCostCenterRef() {
        return costCenterRef;
    }

    public void setCostCenterRef(String costCenterRef) {
        this.costCenterRef = costCenterRef;
    }

    public DumboTeam getSquad() {
        return squad;
    }

    public void setSquad(DumboTeam squad) {
        this.squad = squad;
    }

    public DumboIPRB getIprb() {
        return iprb;
    }

    public void setIprb(DumboIPRB iprb) {
        this.iprb = iprb;
    }

    public DumboInit getInitiative() {
        return initiative;
    }

    public void setInitiative(DumboInit initiative) {
        this.initiative = initiative;
    }

    public Double getEffort() {
        return effort;
    }

    public void setEffort(Double effort) {
        this.effort = effort;
    }

    public String getFinMonth() {
        return finMonth;
    }

    public void setFinMonth(String finMonth) {
        this.finMonth = finMonth;
    }
}