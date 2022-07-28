package com.choqnet.budget.entity;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;

@JmixEntity
public class MyIPRB {

    private IPRB iprb;

    private String reference;

    private String fullName;

    @InstanceName
    private String name;

    private String team;

    private String teamShort;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actSAPMDQ1 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actSAPMDQ2 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actSAPMDQ3 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actSAPMDQ4 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actJIRAMDQ1 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actJIRAMDQ2 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actJIRAMDQ3 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actJIRAMDQ4 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demMDQ1 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demMDQ2 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demMDQ3 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demMDQ4 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actSAPKEQ1 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actSAPKEQ2 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actSAPKEQ3 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actSAPKEQ4 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actJIRAKEQ1 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actJIRAKEQ2 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actJIRAKEQ3 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double actJIRAKEQ4 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demKEQ1 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demKEQ2 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demKEQ3 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demKEQ4 = 0.0;

    public String getFullName() {
        return reference + " : " + name;
    }

    public IPRB getIprb() {
        return iprb;
    }

    public void setIprb(IPRB iprb) {
        this.iprb = iprb;
    }

    public Double getDemKEQ4() {
        return demKEQ4;
    }

    public void setDemKEQ4(Double demKEQ4) {
        this.demKEQ4 = demKEQ4;
    }

    public Double getDemKEQ3() {
        return demKEQ3;
    }

    public void setDemKEQ3(Double demKEQ3) {
        this.demKEQ3 = demKEQ3;
    }

    public Double getDemKEQ2() {
        return demKEQ2;
    }

    public void setDemKEQ2(Double demKEQ2) {
        this.demKEQ2 = demKEQ2;
    }

    public Double getDemKEQ1() {
        return demKEQ1;
    }

    public void setDemKEQ1(Double demKEQ1) {
        this.demKEQ1 = demKEQ1;
    }

    public Double getActJIRAKEQ4() {
        return actJIRAKEQ4;
    }

    public void setActJIRAKEQ4(Double actJIRAKEQ4) {
        this.actJIRAKEQ4 = actJIRAKEQ4;
    }

    public Double getActJIRAKEQ3() {
        return actJIRAKEQ3;
    }

    public void setActJIRAKEQ3(Double actJIRAKEQ3) {
        this.actJIRAKEQ3 = actJIRAKEQ3;
    }

    public Double getActJIRAKEQ2() {
        return actJIRAKEQ2;
    }

    public void setActJIRAKEQ2(Double actJIRAKEQ2) {
        this.actJIRAKEQ2 = actJIRAKEQ2;
    }

    public Double getActJIRAKEQ1() {
        return actJIRAKEQ1;
    }

    public void setActJIRAKEQ1(Double actJIRAKEQ1) {
        this.actJIRAKEQ1 = actJIRAKEQ1;
    }

    public Double getActSAPKEQ4() {
        return actSAPKEQ4;
    }

    public void setActSAPKEQ4(Double actSAPKEQ4) {
        this.actSAPKEQ4 = actSAPKEQ4;
    }

    public Double getActSAPKEQ3() {
        return actSAPKEQ3;
    }

    public void setActSAPKEQ3(Double actSAPKEQ3) {
        this.actSAPKEQ3 = actSAPKEQ3;
    }

    public Double getActSAPKEQ2() {
        return actSAPKEQ2;
    }

    public void setActSAPKEQ2(Double actSAPKEQ2) {
        this.actSAPKEQ2 = actSAPKEQ2;
    }

    public Double getActSAPKEQ1() {
        return actSAPKEQ1;
    }

    public void setActSAPKEQ1(Double actSAPKEQ1) {
        this.actSAPKEQ1 = actSAPKEQ1;
    }

    public Double getDemMDQ4() {
        return demMDQ4;
    }

    public void setDemMDQ4(Double demMDQ4) {
        this.demMDQ4 = demMDQ4;
    }

    public Double getDemMDQ3() {
        return demMDQ3;
    }

    public void setDemMDQ3(Double demMDQ3) {
        this.demMDQ3 = demMDQ3;
    }

    public Double getDemMDQ2() {
        return demMDQ2;
    }

    public void setDemMDQ2(Double demMDQ2) {
        this.demMDQ2 = demMDQ2;
    }

    public Double getDemMDQ1() {
        return demMDQ1;
    }

    public void setDemMDQ1(Double demMDQ1) {
        this.demMDQ1 = demMDQ1;
    }

    public Double getActJIRAMDQ4() {
        return actJIRAMDQ4;
    }

    public void setActJIRAMDQ4(Double actJIRAMDQ4) {
        this.actJIRAMDQ4 = actJIRAMDQ4;
    }

    public Double getActJIRAMDQ3() {
        return actJIRAMDQ3;
    }

    public void setActJIRAMDQ3(Double actJIRAMDQ3) {
        this.actJIRAMDQ3 = actJIRAMDQ3;
    }

    public Double getActJIRAMDQ2() {
        return actJIRAMDQ2;
    }

    public void setActJIRAMDQ2(Double actJIRAMDQ2) {
        this.actJIRAMDQ2 = actJIRAMDQ2;
    }

    public Double getActJIRAMDQ1() {
        return actJIRAMDQ1;
    }

    public void setActJIRAMDQ1(Double actJIRAMDQ1) {
        this.actJIRAMDQ1 = actJIRAMDQ1;
    }

    public Double getActSAPMDQ4() {
        return actSAPMDQ4;
    }

    public void setActSAPMDQ4(Double actSAPMDQ4) {
        this.actSAPMDQ4 = actSAPMDQ4;
    }

    public Double getActSAPMDQ3() {
        return actSAPMDQ3;
    }

    public void setActSAPMDQ3(Double actSAPMDQ3) {
        this.actSAPMDQ3 = actSAPMDQ3;
    }

    public Double getActSAPMDQ2() {
        return actSAPMDQ2;
    }

    public void setActSAPMDQ2(Double actSAPMDQ2) {
        this.actSAPMDQ2 = actSAPMDQ2;
    }

    public Double getActSAPMDQ1() {
        return actSAPMDQ1;
    }

    public void setActSAPMDQ1(Double actSAPMDQ1) {
        this.actSAPMDQ1 = actSAPMDQ1;
    }

    public String getTeamShort() {
        return teamShort;
    }

    public void setTeamShort(String teamShort) {
        this.teamShort = teamShort;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

}