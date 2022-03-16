package com.choqnet.budget.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "WORKLOG", indexes = {
        @Index(name = "IDX_WORKLOG_TEAM_ID", columnList = "TEAM_ID"),
        @Index(name = "IDX_WORKLOG_IPRB_ID", columnList = "IPRB_ID")
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
    }

    public Double getEffort() {
        return effort;
    }

    public void setEffort(Double effort) {
        this.effort = effort;
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