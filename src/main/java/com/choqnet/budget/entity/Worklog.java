package com.choqnet.budget.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;

import javax.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "WORKLOG", indexes = {
        @Index(name = "IDX_WORKLOG_TEAM_ID", columnList = "TEAM_ID"),
        @Index(name = "IDX_WORKLOG_IPRB_ID", columnList = "IPRB_ID"),
        @Index(name = "IDX_WORKLOG", columnList = "FIN_MONTH")
})
@Entity
public class Worklog {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "INITIATIVE")
    private String initiative;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "IPRB_REF")
    private String iprbRef;

    @JoinColumn(name = "IPRB_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private IPRB iprb;

    @Column(name = "TEAM_REF")
    private Integer teamRef;

    @JoinColumn(name = "TEAM_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    @NumberFormat(pattern = "#,##0.00", groupingSeparator = " ")
    @Column(name = "EFFORT")
    private Double effort;

    @Column(name = "DATE_")
    private String date;

    @Column(name = "FIN_MONTH")
    private String finMonth;

    @Column(name = "USER_REF")
    private String userRef;

    @Column(name = "KEY_")
    private String key;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "RES_TYPE")
    private String resType;

    @Column(name = "INIT_REF")
    private String initRef;

    @Column(name = "FINANCE_TYPE")
    private String financeType;

    @Column(name = "SAP_ID")
    private String sapID;

    @Column(name = "ACCOUNT")
    private String account;

    @Column(name = "INIT_STATUS")
    private String initStatus;

    @Column(name = "ORIGIN_IPRB")
    private String originIPRB;

    // this number is in k€
    @NumberFormat(pattern = "#,##0.000", groupingSeparator = " ")
    @Column(name = "BUDGET_COST")
    private Double budgetCost;

    public Double getBudgetCost() {
        if (team == null || team.getSetup()==null) {
            return 0.0;
        } else {
            return team.getSetup().getRate(finMonth) * effort / 1000;
        }
        //return budgetCost==null ? 0.0 : budgetCost;
    }

    public void setBudgetCost(Double dummy) {
        budgetCost = (team==null || team.getSetup()==null) ? 0.0 : team.getSetup().getRate(finMonth) * effort / 1000;
    }
    // version w/o value
    public void setBudgetCost() {
        setBudgetCost(0.0);
    }

    public String getOriginIPRB() {
        return originIPRB;
    }



    public void setOriginIPRB(String originIPRB) {
        this.originIPRB = originIPRB;
    }

    public String getInitStatus() {
        return initStatus;
    }

    public void setInitStatus(String initStatus) {
        this.initStatus = initStatus;
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

    public void setIprb(IPRB iprb) {
        this.iprb = iprb;
    }

    public IPRB getIprb() {
        return iprb;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
        setBudgetCost();
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
        setBudgetCost();
    }

    public Double getEffort() {
        return effort;
    }

    public void setEffort(Double effort) {
        this.effort = effort;
        setBudgetCost();
    }

    public Integer getTeamRef() {
        return teamRef==null?0:teamRef;
    }

    public void setTeamRef(Integer teamRef) {
        this.teamRef = teamRef;
    }

    public String getIprbRef() {
        return iprbRef==null ? "" : iprbRef;
    }

    public void setIprbRef(String iprbRef) {
        this.iprbRef = iprbRef;
    }

    public String getInitiative() {
        return initiative;
    }

    public void setInitiative(String initiative) {
        this.initiative = initiative;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}