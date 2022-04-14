package com.choqnet.budget.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "PROGRESS", indexes = {
        @Index(name = "IDX_PROGRESS_IPRB_ID", columnList = "IPRB_ID"),
        @Index(name = "IDX_PROGRESS_BUDGET_ID", columnList = "BUDGET_ID")
})
@Entity
public class Progress {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "IPRB_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private IPRB iprb;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "BUDGET_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Budget budget;

    // @NumberFormat(pattern = "#,##0", groupingSeparator = " ")

    @NumberFormat(pattern = "#,##0")
    @Column(name = "EXPECTED_LANDING")
    private Double expectedLanding;

    @NumberFormat(pattern = "#,##0")
    @Column(name = "ACTUAL_Q1")
    private Double actualQ1 = 0.0;

    @NumberFormat(pattern = "#,##0")
    @Column(name = "ACTUAL_Q2")
    private Double actualQ2 = 0.0;

    @NumberFormat(pattern = "#,##0")
    @Column(name = "ACTUAL_Q3")
    private Double actualQ3 = 0.0;

    @NumberFormat(pattern = "#,##0")
    @Column(name = "ACTUAL_Q4")
    private Double actualQ4 = 0.0;

    @NumberFormat(pattern = "#,##0")
    @Column(name = "DEMAND_Q1")
    private Double demandQ1 = 0.0;

    @NumberFormat(pattern = "#,##0")
    @Column(name = "DEMAND_Q2")
    private Double demandQ2 = 0.0;

    @NumberFormat(pattern = "#,##0")
    @Column(name = "DEMAND_Q3")
    private Double demandQ3 = 0.0;

    @NumberFormat(pattern = "#,##0")
    @Column(name = "DEMAND_Q4")
    private Double demandQ4 = 0.0;

    @Column(name = "DEMAND_NY")
    private Double demandNY;

    @OneToMany(mappedBy = "progress")
    private List<Detail> details;

    @OneToMany(mappedBy = "progress")
    private List<Expense> expenses;

    @NumberFormat(pattern = "#,##0.0")
    @Column(name = "EXPENSE")
    private Double expense = 0.0;

    @NumberFormat(pattern = "#,##0.00")
    @Column(name = "BUDGET_COST")
    private Double budgetCost;

    @Column(name = "BUDGET_COST_Q1")
    private Double budgetCostQ1;

    @Column(name = "BUDGET_COST_Q2")
    private Double budgetCostQ2;

    @Column(name = "BUDGET_COST_Q3")
    private Double budgetCostQ3;

    @Column(name = "BUDGET_COST_Q4")
    private Double budgetCostQ4;

    public Double getBudgetCostQ4() {
        return budgetCostQ4 == null ? 0.0 : budgetCostQ4;
    }

    public void setBudgetCostQ4(Double budgetCostQ4) {
        this.budgetCostQ4 = budgetCostQ4;
    }

    public Double getBudgetCostQ3() {
        return budgetCostQ3 == null ? 0.0 : budgetCostQ3;
    }

    public void setBudgetCostQ3(Double budgetCostQ3) {
        this.budgetCostQ3 = budgetCostQ3;
    }

    public Double getBudgetCostQ2() {
        return budgetCostQ2 == null ? 0.0 : budgetCostQ2;
    }

    public void setBudgetCostQ2(Double budgetCostQ2) {
        this.budgetCostQ2 = budgetCostQ2;
    }

    public Double getBudgetCostQ1() {
        return budgetCostQ1 == null ? 0.0 : budgetCostQ1;
    }

    public void setBudgetCostQ1(Double budgetCostQ1) {
        this.budgetCostQ1 = budgetCostQ1;
    }

    public Double getDemandNY() {
        return demandNY == null ? 0.0 : demandNY;
    }

    public void setDemandNY(Double demandNY) {
        this.demandNY = demandNY;
    }


    // todo : better implementation of the costs for Progress

    public Double getBudgetCost() {
        return budgetCost == null ? 0.0 : budgetCost;
    }

    public void setBudgetCost(Double budgetCost) {
        this.budgetCost= budgetCost;
    }

    public Double getExpectedLanding() {
        return (budget==null ? 0.0 : (budget.getCloseQ1() ? actualQ1 : demandQ1) +
                (budget.getCloseQ2() ? actualQ2 : demandQ2) +
                (budget.getCloseQ3() ? actualQ3 : demandQ3) +
                (budget.getCloseQ4() ? actualQ4 : demandQ4));
    }


    public Double getExpense() {
        return expense;
    }

    public void setExpense(Double expense) {
        // for initialisation purpose only
        this.expense = expense;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }

    public Double getDemandQ4() {
        return demandQ4;
    }

    public void setDemandQ4(Double demandQ4) {
        // for initialisation purpose only
        this.demandQ4 = demandQ4;
    }

    public Double getDemandQ3() {
        return demandQ3;
    }

    public void setDemandQ3(Double demandQ3) {
        // for initialisation purpose only
        this.demandQ3 = demandQ3;
    }

    public Double getDemandQ2() {
        return demandQ2;
    }

    public void setDemandQ2(Double demandQ2) {
        // for initialisation purpose only
        this.demandQ2 = demandQ2;
    }

    public Double getDemandQ1() {
        return demandQ1;
    }

    public void setDemandQ1(Double demandQ1) {
        // for initialisation purpose only
        this.demandQ1 = demandQ1;
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

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public IPRB getIprb() {
        return iprb;
    }

    public void setIprb(IPRB iprb) {
        this.iprb = iprb;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }



}