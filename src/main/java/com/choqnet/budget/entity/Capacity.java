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
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull
    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "TEAM_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Team team;

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

    @Column(name = "NB_WORKING_DAYS")
    private Integer nbWorkingDays = 0;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "BUDGET_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Budget budget;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    //@NumberFormat(pattern = "0.00")
    @Transient
    @JmixProperty
    public Double getMdY() {
        return getMdQ1() + getMdQ2() + getMdQ3() + getMdQ4();
    }

    // *** MAN DAYS
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

    @NumberFormat(pattern = "#,##0.00", groupingSeparator = " ")
    @Transient
    @JmixProperty
    public Double getMdQ4() {
        return this.fteQ4 * nbWorkingDays / 4;
    }

    @NumberFormat(pattern = "#,##0.00", groupingSeparator = " ")
    @Transient
    @JmixProperty
    public Double getMdQ3() {
        return this.fteQ3 * nbWorkingDays / 4;
    }

    @NumberFormat(pattern = "#,##0.00", groupingSeparator = " ")
    @Transient
    @JmixProperty
    public Double getMdQ2() {
        return this.fteQ2 * nbWorkingDays / 4;
    }

    @NumberFormat(pattern = "#,##0.00", groupingSeparator = " ")
    @Transient
    @JmixProperty
    public Double getMdQ1() {
        return this.fteQ1 * nbWorkingDays / 4;
    }

    // *** FTE

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
        // if a quarterly rate is set, it will be applied to the other quarters to come
        this.fteQ3 = fteQ3;
    }

    public Double getFteQ2() {
        return fteQ2;
    }
    public void setFteQ2(Double fteQ2) {
        // if a quarterly rate is set, it will be applied to the other quarters to come
        this.fteQ2 = fteQ2;
    }

    public Double getFteQ1() {
        return fteQ1;
    }
    public void setFteQ1(Double fteQ1) {
        // if a quarterly rate is set, it will be applied to the other quarters to come
        this.fteQ1 = fteQ1;
    }

    // *** RATES
    public Double getRateQ4() {
        return rateQ4;
    }
    public void setRateQ4(Double rateQ4) {
        // if a quarterly rate is set the yearly rate is set to -1
        this.rateQ4 = rateQ4;
    }

    public Double getRateQ3() {
        return rateQ3;
    }
    public void setRateQ3(Double rateQ3) {
        // if a quarterly rate is set, it will be applied to the other quarters to come
        this.rateQ3 = rateQ3;
    }

    public Double getRateQ2() {
        return rateQ2;
    }
    public void setRateQ2(Double rateQ2) {
        // if a quarterly rate is set, it will be applied to the other quarters to come
        this.rateQ2 = rateQ2;
    }

    public Double getRateQ1() {
        return rateQ1;
    }
    public void setRateQ1(Double rateQ1) {
        // if a quarterly rate is set, it will be applied to the other quarters to come
        this.rateQ1 = rateQ1;
    }

    // *** OTHER FIELDS

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
}