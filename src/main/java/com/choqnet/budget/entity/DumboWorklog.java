package com.choqnet.budget.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

@JmixEntity
public class DumboWorklog {

    private Double effort;

    private Integer teamID;

    private DumboIPRB iprb;

    private DumboInit init;

    private String finMonth;

    private String userRef;

    private String date;

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserRef() {
        return userRef;
    }

    public void setUserRef(String userRef) {
        this.userRef = userRef;
    }

    public String getFinMonth() {
        return finMonth;
    }

    public void setFinMonth(String finMonth) {
        this.finMonth = finMonth;
    }

    public DumboInit getInit() {
        return init;
    }

    public void setInit(DumboInit init) {
        this.init = init;
    }

    public DumboIPRB getIprb() {
        return iprb;
    }

    public void setIprb(DumboIPRB iprb) {
        this.iprb = iprb;
    }

    public Integer getTeamID() {
        return teamID;
    }

    public void setTeamID(Integer teamID) {
        this.teamID = teamID;
    }

    public Double getEffort() {
        return effort;
    }

    public void setEffort(Double effort) {
        this.effort = effort;
    }
}