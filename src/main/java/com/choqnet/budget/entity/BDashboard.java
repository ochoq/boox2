package com.choqnet.budget.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;

import java.util.UUID;

@JmixEntity
public class BDashboard {
    @JmixGeneratedValue
    @JmixId
    private UUID id;

    private String domain;

    private String domain2Display;

    private String platform;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demandUMP = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demandBAU = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demandRUN = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demandQ1 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demandQ2 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demandQ3 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demandQ4 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double demand = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double budgetUMP = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double budgetBAU = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double budgetRUN = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double budgetQ1 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double budgetQ2 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double budgetQ3 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double budgetQ4 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double budget = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double kEurUMP = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double kEurBAU = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double kEurRUN = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double kEurQ1 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double kEurQ2 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double kEurQ3 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double kEurQ4 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double kEurY = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double capacity = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    private Double kEurCapacity;

    private Double bookingRatio;

    public String getDomain2Display() {
        return domain2Display;
    }

    public void setDomain2Display(String domain2Display) {
        this.domain2Display = domain2Display;
    }

    public Double getKEurCapacity() {
        return kEurCapacity;
    }

    public void setKEurCapacity(Double kEurCapacity) {
        this.kEurCapacity = kEurCapacity;
    }

    public void setKEurQ1(Double kEurQ1) {
        this.kEurQ1 = kEurQ1;
    }

    public Double getKEurQ1() {
        return kEurQ1;
    }

    public Double getKEurY() {
        return kEurY;
    }

    public void setKEurY(Double kEurY) {
        this.kEurY = kEurY;
    }

    public Double getKEurQ4() {
        return kEurQ4;
    }

    public void setKEurQ4(Double kEurQ4) {
        this.kEurQ4 = kEurQ4;
    }

    public Double getKEurQ3() {
        return kEurQ3;
    }

    public void setKEurQ3(Double kEurQ3) {
        this.kEurQ3 = kEurQ3;
    }

    public Double getKEurQ2() {
        return kEurQ2;
    }

    public void setKEurQ2(Double kEurQ2) {
        this.kEurQ2 = kEurQ2;
    }

    public Double getKEurRUN() {
        return kEurRUN;
    }

    public void setKEurRUN(Double kEurRUN) {
        this.kEurRUN = kEurRUN;
    }

    public Double getKEurBAU() {
        return kEurBAU;
    }

    public void setKEurBAU(Double kEurBAU) {
        this.kEurBAU = kEurBAU;
    }

    public Double getKEurUMP() {
        return kEurUMP;
    }

    public void setKEurUMP(Double kEurUMP) {
        this.kEurUMP = kEurUMP;
    }

    public Double getBudgetQ4() {
        return budgetQ4;
    }

    public void setBudgetQ4(Double budgetQ4) {
        this.budgetQ4 = budgetQ4;
    }

    public Double getBudgetQ3() {
        return budgetQ3;
    }

    public void setBudgetQ3(Double budgetQ3) {
        this.budgetQ3 = budgetQ3;
    }

    public Double getBudgetQ2() {
        return budgetQ2;
    }

    public void setBudgetQ2(Double budgetQ2) {
        this.budgetQ2 = budgetQ2;
    }

    public Double getBudgetQ1() {
        return budgetQ1;
    }

    public void setBudgetQ1(Double budgetQ1) {
        this.budgetQ1 = budgetQ1;
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


    public Double getDemand() {
        return demand;
    }

    public void setDemand(Double demand) {
        this.demand = demand;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Double getBookingRatio() {
        return bookingRatio;
    }

    public void setBookingRatio(Double bookingRatio) {
        this.bookingRatio = bookingRatio;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public Double getBudgetRUN() {
        return budgetRUN;
    }

    public void setBudgetRUN(Double budgetRUN) {
        this.budgetRUN = budgetRUN;
    }

    public Double getBudgetBAU() {
        return budgetBAU;
    }

    public void setBudgetBAU(Double budgetBAU) {
        this.budgetBAU = budgetBAU;
    }

    public Double getBudgetUMP() {
        return budgetUMP;
    }

    public void setBudgetUMP(Double budgetUMP) {
        this.budgetUMP = budgetUMP;
    }

    public Double getDemandRUN() {
        return demandRUN;
    }

    public void setDemandRUN(Double demandRUN) {
        this.demandRUN = demandRUN;
    }

    public Double getDemandBAU() {
        return demandBAU;
    }

    public void setDemandBAU(Double demandBAU) {
        this.demandBAU = demandBAU;
    }

    public Double getDemandUMP() {
        return demandUMP;
    }

    public void setDemandUMP(Double demandUMP) {
        this.demandUMP = demandUMP;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}