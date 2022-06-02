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

    private String userName;

    private String resType;

    private String initRef;

    private String financeType;

    private String sapID;

    private String account;

    private String costCenterRef;

    public String getCostCenterRef() {
        return costCenterRef;
    }

    public void setCostCenterRef(String costCenterRef) {
        this.costCenterRef = costCenterRef;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSapID() {
        return sapID;
    }

    public void setSapID(String sapID) {
        this.sapID = sapID;
    }

    public String getFinanceType() {
        return financeType;
    }

    public void setFinanceType(String financeType) {
        this.financeType = financeType;
    }

    public String getInitRef() {
        return initRef;
    }

    public void setInitRef(String initRef) {
        this.initRef = initRef;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

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