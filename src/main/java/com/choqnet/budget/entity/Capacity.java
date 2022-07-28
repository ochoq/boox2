package com.choqnet.budget.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.annotation.NumberFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
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

    @NumberFormat(pattern = "0.00", groupingSeparator = " ")
    @Column(name = "FTE_Q1")
    private Double fteQ1 = 0.0;

    @NumberFormat(pattern = "0.00", groupingSeparator = " ")
    @Column(name = "FTE_Q2")
    private Double fteQ2 = 0.0;

    @NumberFormat(pattern = "0.00", groupingSeparator = " ")
    @Column(name = "FTE_Q3")
    private Double fteQ3 = 0.0;

    @NumberFormat(pattern = "0.00", groupingSeparator = " ")
    @Column(name = "FTE_Q4")
    private Double fteQ4 = 0.0;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    @Column(name = "RATE_Y")
    private Double rateY;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    @Column(name = "RATE_Q1")
    private Double rateQ1;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    @Column(name = "RATE_Q2")
    private Double rateQ2;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    @Column(name = "RATE_Q3")
    private Double rateQ3;

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
    @Column(name = "RATE_Q4")
    private Double rateQ4;
    @NumberFormat(pattern = "#,##0")
    @Column(name = "MD_Q1")
    private Double mdQ1;
    @NumberFormat(pattern = "#,##0")
    @Column(name = "MD_Q2")
    private Double mdQ2;
    @NumberFormat(pattern = "#,##0")
    @Column(name = "MD_Q3")
    private Double mdQ3;
    @NumberFormat(pattern = "#,##0")
    @Column(name = "MD_Q4")
    private Double mdQ4;

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

    // *** FTE, renamed HEADCOUNT DATA

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

    public Double getFteQ4() {
        if (budget == null || team == null || team.getSetup() == null) {
            return 0.0;
        }
        int intYear = Integer.parseInt(budget.getYear());
        double nbDays = team.getSetup().getWorkDaysAtDate(LocalDate.of(intYear, 10, 2));
        if (nbDays == 0) {
            return 0.0;
        } else {
            return ((mdQ4 == null ? 0.0 : mdQ4) * 4 / nbDays);
        }
        //return fteQ4;
    }

//    public void setFteQ4(Double fte) {
//        this.fteQ4 = fte;
//    }

    public Double getFteQ3() {
        if (budget == null || team == null || team.getSetup() == null) {
            return 0.0;
        }
        int intYear = Integer.parseInt(budget.getYear());
        double nbDays = team.getSetup().getWorkDaysAtDate(LocalDate.of(intYear, 7, 2));
        if (nbDays == 0) {
            return 0.0;
        } else {
            return ((mdQ3 == null ? 0.0 : mdQ3) * 4 / nbDays);
        }
        //return fteQ3;
    }

//    public void setFteQ3(Double fte) {
//        this.fteQ3 = fte;
//    }



    public Double getFteQ2() {
        if (budget == null || team == null || team.getSetup() == null) {
            return 0.0;
        }
        int intYear = Integer.parseInt(budget.getYear());
        double nbDays = team.getSetup().getWorkDaysAtDate(LocalDate.of(intYear, 4, 2));
        if (nbDays == 0) {
            return 0.0;
        } else {
            return ((mdQ2 == null ? 0.0 : mdQ2) * 4 / nbDays);
        }
        //return fteQ2;
    }

//    public void setFteQ2(Double fte) {
//        this.fteQ2 = fte;
//    }

    public Double getFteQ1() {
        if (budget == null || team == null || team.getSetup() == null) {
            return 0.0;
        }
        int intYear = Integer.parseInt(budget.getYear());
        double nbDays = team.getSetup().getWorkDaysAtDate(LocalDate.of(intYear, 1, 2));
        if (nbDays == 0) {
            return 0.0;
        } else {
            return ((mdQ1 == null ? 0.0 : mdQ1) * 4 / nbDays);
        }
        //  return fteQ1;
    }

//    public void setFteQ1(Double fte) {
//        this.fteQ1 = fte;
//    }


    // *** RATES DATA

    public Double getRateY() {
        return rateY == null ? 0.0 : rateY;
    }

    public void setRateY(Double rate) {
        this.rateY = rate;
    }

    public Double getRateQ4() {
        if (budget == null || team == null || team.getSetup() == null) {
            return 0.0;
        }
        int intYear = Integer.parseInt(budget.getYear());
        return (team.getSetup().getRateAtDate(LocalDate.of(intYear, 10, 2)) * 1.0);
        //return rateQ4 == null ? 0.0 : rateQ4;
    }

//    public void setRateQ4(Double rateQ4) {
//        this.rateQ4 = rateQ4;
//    }

    public Double getRateQ3() {
        if (budget == null || team == null || team.getSetup() == null) {
            return 0.0;
        }
        int intYear = Integer.parseInt(budget.getYear());
        return (team.getSetup().getRateAtDate(LocalDate.of(intYear, 7, 2)) * 1.0);
        //return rateQ3 == null ? 0.0 : rateQ3;
    }

//    public void setRateQ3(Double rateQ3) {
//        this.rateQ3 = rateQ3;
//    }

    public Double getRateQ2() {
        if (budget == null || team == null || team.getSetup() == null) {
            return 0.0;
        }
        int intYear = Integer.parseInt(budget.getYear());
        return (team.getSetup().getRateAtDate(LocalDate.of(intYear, 4, 2)) * 1.0);
        //return rateQ2 == null ? 0.0 : rateQ2;
    }

//    public void setRateQ2(Double rateQ2) {
//        this.rateQ2 = rateQ2;
//    }


    public Double getRateQ1() {
        if (budget == null || team == null || team.getSetup() == null) {
            return 0.0;
        }
        int intYear = Integer.parseInt(budget.getYear());
        return (team.getSetup().getRateAtDate(LocalDate.of(intYear, 1, 2)) * 1.0);

        //return rateQ1 == null ? 0.0 : rateQ1;
    }

//    public void setRateQ1(Double rateQ1) {
//        this.rateQ1 = rateQ1;
//    }

    public Double getRateQx(int i) {
        switch (i) {
            case 1:
                return rateQ1;
            case 2:
                return rateQ2;
            case 3:
                return rateQ3;
            case 4:
                return rateQ4;
        }
        return 0.0;
    }

//    public void setQx(int i, Double value) {
//        switch (i) {
//            case 1:
//                rateQ1 = value;
//                break;
//            case 2:
//                rateQ2 = value;
//                break;
//            case 3:
//                rateQ3 = value;
//                break;
//            case 4:
//                rateQ4 = value;
//                break;
//        }
//    }


    @NumberFormat(pattern = "#,##0")
    @JmixProperty
    public Double getMdY() {
        return getMdQ1() + getMdQ2() + getMdQ3() + getMdQ4();
    }

    public Double getMdQ1() {
        return mdQ1 == null ? 0.0 : mdQ1;
    }

    public void setMdQ1(Double mdQ1) {
        this.mdQ1 = mdQ1;
    }

    public Double getMdQ2() {
        return mdQ2 == null ? 0.0 : mdQ2;
    }

    public void setMdQ2(Double mdQ2) {
        this.mdQ2 = mdQ2;
    }

    public Double getMdQ3() {
        return mdQ3 == null ? 0.0 : mdQ3;
    }

    public void setMdQ3(Double mdQ3) {
        this.mdQ3 = mdQ3;
    }

    public Double getMdQ4() {
        return mdQ4 == null ? 0.0 : mdQ4;
    }

    public void setMdQ4(Double mdQ4) {
        this.mdQ4 = mdQ4;
    }

    public Double getMdQx(int i) {
        switch (i) {
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