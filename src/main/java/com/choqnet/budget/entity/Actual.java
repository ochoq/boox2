package com.choqnet.budget.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

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

    @Column(name = "EFFORT")
    private Double effort;

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
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
    }

    public String getFinMonth() {
        return finMonth;
    }

    public void setFinMonth(String finMonth) {
        this.finMonth = finMonth;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}