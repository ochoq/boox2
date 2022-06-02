package com.choqnet.budget.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;

import java.util.UUID;

@JmixEntity
public class FollowUp {
    @JmixGeneratedValue
    @JmixId
    private UUID id;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demandQ1;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demandQ2;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demandQ3;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demandQ4;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actualQ1;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actualQ2;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actualQ3;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actualQ4;

    private IPRB iprb;

    private Team team;

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public IPRB getIprb() {
        return iprb;
    }

    public void setIprb(IPRB iprb) {
        this.iprb = iprb;
    }

    public Double getActualQ4() {
        return actualQ4;
    }

    public void setActualQ4(Double actualQ4) {
        this.actualQ4 = actualQ4;
    }

    public Double getActualQ3() {
        return actualQ3;
    }

    public void setActualQ3(Double actualQ3) {
        this.actualQ3 = actualQ3;
    }

    public Double getActualQ2() {
        return actualQ2;
    }

    public void setActualQ2(Double actualQ2) {
        this.actualQ2 = actualQ2;
    }

    public Double getActualQ1() {
        return actualQ1;
    }

    public void setActualQ1(Double actualQ1) {
        this.actualQ1 = actualQ1;
    }

    public Double getDemandQ4() {
        return demandQ4;
    }

    public void setDemandQ4(Double demandQ4) {
        this.demandQ4 = demandQ4;
    }

    public Double getDemandQ3() {
        return demandQ3;
    }

    public void setDemandQ3(Double demandQ3) {
        this.demandQ3 = demandQ3;
    }

    public Double getDemandQ2() {
        return demandQ2;
    }

    public void setDemandQ2(Double demandQ2) {
        this.demandQ2 = demandQ2;
    }

    public Double getDemandQ1() {
        return demandQ1;
    }

    public void setDemandQ1(Double demandQ1) {
        this.demandQ1 = demandQ1;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Double giveValue(String type, int quarter) {
        switch (type.toUpperCase()+quarter) {
            case "D1": return demandQ1;
            case "D2": return demandQ2;
            case "D3": return demandQ3;
            case "D4": return demandQ4;
            case "A1": return actualQ1;
            case "A2": return actualQ2;
            case "A3": return actualQ3;
            case "A4": return actualQ4;
            default: return 0.0;
        }
    }
}