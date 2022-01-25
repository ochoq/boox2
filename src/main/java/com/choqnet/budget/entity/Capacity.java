package com.choqnet.budget.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.annotation.NumberFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
@Table(name = "CAPACITY")
@Entity
public class Capacity {

    // *** GENERAL FIELDS

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull
    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "TEAM_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Team team;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "BUDGET_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Budget budget;

    @Column(name = "NB_WORKING_DAYS")
    private Integer nbWorkingDays = 0;

    public Team getTeam() {
        return team;
    }
    public void setTeam(Team team) {
        this.team = team;
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getNbWorkingDays() {
        return nbWorkingDays;
    }
    public void setNbWorkingDays(Integer nbWorkingDays) {
        this.nbWorkingDays = nbWorkingDays;
    }

    public Budget getBudget() {
        return budget;
    }
    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    // *** FTE, renamed HEADCOUNT DATA

    @NumberFormat(pattern = "0.00")
    @Column(name = "FTE_Q1")
    private Double fteQ1 = 0.0;

    @NumberFormat(pattern = "0.00")
    @Column(name = "FTE_Q2")
    private Double fteQ2 = 0.0;

    @NumberFormat(pattern = "0.00")
    @Column(name = "FTE_Q3")
    private Double fteQ3 = 0.0;

    @NumberFormat(pattern = "0.00")
    @Column(name = "FTE_Q4")
    private Double fteQ4 = 0.0;

    public Double getFteQ4() {
        return fteQ4;
    }
    public void setFteQ4(Double fteQ4) {
        this.fteQ4 = fteQ4;
    }

    public Double getFteQ3() {
        return fteQ3;
    }
    public void setFteQ3(Double fteQ3) {
        this.fteQ3 = fteQ3;
    }

    public Double getFteQ2() {
        return fteQ2;
    }
    public void setFteQ2(Double fteQ2) {
        this.fteQ2 = fteQ2;
    }

    public Double getFteQ1() {
        return fteQ1;
    }
    public void setFteQ1(Double fteQ1) {
        this.fteQ1 = fteQ1;
    }

    // *** RATES DATA

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    @Column(name = "RATE_Y")
    private Double rateY;

    @NumberFormat(pattern = "0")
    @Column(name = "RATE_Q1")
    private Double rateQ1 = 0.0;

    @NumberFormat(pattern = "0")
    @Column(name = "RATE_Q2")
    private Double rateQ2 = 0.0;

    @NumberFormat(pattern = "0")
    @Column(name = "RATE_Q3")
    private Double rateQ3 = 0.0;

    @NumberFormat(pattern = "0")
    @Column(name = "RATE_Q4")
    private Double rateQ4 = 0.0;

    public Double getRateY() {
        return rateY==null ? 0.0 : rateY;
    }
    public void setRateY(Double rateY) {
        this.rateY = rateY;
        // currently, the captured rateY applies for each quarter
        // however, calculations will be done with rateQx for each quarter
        this.rateQ1 = rateY;
        this.rateQ2 = rateY;
        this.rateQ3 = rateY;
        this.rateQ4 = rateY;
    }

    public Double getRateQ4() {
        return rateQ4==null ? 0.0 : rateQ4;
    }
    public void setRateQ4(Double rateQ4) {
        this.rateQ4 = rateQ4;
    }

    public Double getRateQ3() {
        return rateQ3==null ?0.0 : rateQ3;
    }
    public void setRateQ3(Double rateQ3) {
        this.rateQ3 = rateQ3;
    }

    public Double getRateQ2() {
        return rateQ2 == null ? 0.0 : rateQ2;
    }
    public void setRateQ2(Double rateQ2) {
        this.rateQ2 = rateQ2;
    }

    public Double getRateQ1() {
        return rateQ1 == null ? 0.0 : rateQ1;
    }
    public void setRateQ1(Double rateQ1) {
        this.rateQ1 = rateQ1;
    }

    // *** MANDAYS DATA

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    @Column(name = "MD_Q1")
    private Double mdQ1;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    @Column(name = "MD_Q2")
    private Double mdQ2;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    @Column(name = "MD_Q3")
    private Double mdQ3;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    @Column(name = "MD_Q4")
    private Double mdQ4;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    @Transient
    @JmixProperty
    public Double getMdY() {
        return getMdQ1() + getMdQ2() + getMdQ3() + getMdQ4();
    }

    public void setMdQ1(Double mdQ1) {
        this.mdQ1 = mdQ1;
    }
    public Double getMdQ1() {
        return mdQ1==null ? 0.0 : mdQ1;
    }

    public void setMdQ2(Double mdQ2) {
        this.mdQ2 = mdQ2;
    }
    public Double getMdQ2() {
        return mdQ2==null ? 0.0 : mdQ2;
    }

    public void setMdQ3(Double mdQ3) {
        this.mdQ3 = mdQ3;
    }
    public Double getMdQ3() {
        return mdQ3==null ? 0.0 : mdQ3;
    }

    public void setMdQ4(Double mdQ4) {
        this.mdQ4 = mdQ4;
    }
    public Double getMdQ4() {
        return mdQ4==null ? 0.0 : mdQ4;
    }

    public Double getMdQx(int i) {
        switch(i) {
            case 1:
                return getMdQ1();
            case 2:
                return getMdQ2();
            case 3:
                return getMdQ3();
            case 4:
                return getMdQ4();
            default:
                return 0.0;
        }
    }




}