package com.choqnet.budget.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;

import javax.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "ACTUAL", indexes = {
        @Index(name = "IDX_ACTUAL_IPRB_ID", columnList = "IPRB_ID"),
        @Index(name = "IDX_ACTUAL_TEAM_ID", columnList = "TEAM_ID")
})
@Entity
public class Actual {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "FIN_MONTH")
    private String finMonth;

    @Column(name = "IPRB_REF")
    private String iprbRef;

    @JoinColumn(name = "IPRB_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private IPRB iprb;

    @Column(name = "JIRA_TEAM_REF")
    private Integer jiraTeamRef;

    @JoinColumn(name = "TEAM_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    @Column(name = "INIT_REF")
    private String initRef;

    @Column(name = "INIT_NAME")
    private String initName;

    @Column(name = "INIT_CATEGORY")
    private String initCategory;

    @Column(name = "JIRA_PROJECT")
    private String jiraProject;

    @Column(name = "JIRA_PROJECT_DOMAIN")
    private String jiraProjectDomain;

    @Column(name = "JIRA_PROJECT_PLATFORM")
    private String jiraProjectPlatform;

    @Column(name = "COST_CENTER")
    private String costCenter;

    @NumberFormat(pattern = "#,##0.00")
    @Column(name = "EFFORT")
    private Double effort;

    @NumberFormat(pattern = "#,##0.000")
    @Column(name = "BUDGET_COST")
    private Double budgetCost;

    public Double getBudgetCost() {
        if (team == null || team.getSetup()==null) {
            return 0.0;
        } else {
            return team.getSetup().getRateQx(finMonth) * effort / 1000;
        }
        //return budgetCost==null ? 0.0 : budgetCost;
    }

    public void setBudgetCost(Double budgetCost) {
        budgetCost = (team==null || team.getSetup()==null) ? 0.0 : team.getSetup().getRateQx(finMonth) * effort / 1000;
    }
    // version w/o value
    public void setBudgetCost() {
        setBudgetCost(0.0);
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
        setBudgetCost();
    }

    public String getJiraProjectPlatform() {
        return jiraProjectPlatform;
    }

    public void setJiraProjectPlatform(String jiraProjectPlatform) {
        this.jiraProjectPlatform = jiraProjectPlatform;
    }

    public String getJiraProjectDomain() {
        return jiraProjectDomain;
    }

    public void setJiraProjectDomain(String jiraProjectDomain) {
        this.jiraProjectDomain = jiraProjectDomain;
    }

    public String getJiraProject() {
        return jiraProject;
    }

    public void setJiraProject(String jiraProject) {
        this.jiraProject = jiraProject;
    }

    public Integer getJiraTeamRef() {
        return jiraTeamRef;
    }

    public void setJiraTeamRef(Integer jiraTeamRef) {
        this.jiraTeamRef = jiraTeamRef;
    }

    public String getInitCategory() {
        return initCategory;
    }

    public void setInitCategory(String initCategory) {
        this.initCategory = initCategory;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public String getInitName() {
        return initName;
    }

    public void setInitName(String initName) {
        this.initName = initName;
    }

    public String getInitRef() {
        return initRef;
    }

    public void setInitRef(String initRef) {
        this.initRef = initRef;
    }

    public IPRB getIprb() {
        return iprb;
    }

    public void setIprb(IPRB iprb) {
        this.iprb = iprb;
    }

    public String getIprbRef() {
        return iprbRef;
    }

    public void setIprbRef(String iprbRef) {
        this.iprbRef = iprbRef;
    }

    public Double getEffort() {
        return effort;
    }

    public void setEffort(Double effort) {
        this.effort = effort;
        setBudgetCost();
    }

    public String getFinMonth() {
        return finMonth;
    }

    public void setFinMonth(String finMonth) {
        this.finMonth = finMonth;
        setBudgetCost();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public double getValue(String year, String quarter) {
        if ("Q1".equals(quarter) && (
                finMonth.equals("fin" + year + "01") || finMonth.equals("fin" + year + "02") || finMonth.equals("fin" + year + "03")
                )) {
            return effort;
        }
        if ("Q2".equals(quarter) && (
                finMonth.equals("fin" + year + "04") || finMonth.equals("fin" + year + "05") || finMonth.equals("fin" + year + "06")
        )) {
            return effort;
        }
        if ("Q3".equals(quarter) && (
                finMonth.equals("fin" + year + "07") || finMonth.equals("fin" + year + "08") || finMonth.equals("fin" + year + "09")
        )) {
            return effort;
        }
        if ("Q4".equals(quarter) && (
                finMonth.equals("fin" + year + "10") || finMonth.equals("fin" + year + "11") || finMonth.equals("fin" + year + "12")
        )) {
            return effort;
        }
        return 0.0;
    }
}