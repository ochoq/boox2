package com.choqnet.budget.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.JmixEntity;

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

    @Column(name = "ACTUAL_Q1")
    private Double actualQ1 = 0.0;

    @Column(name = "ACTUAL_Q2")
    private Double actualQ2 = 0.0;

    @Column(name = "ACTUAL_Q3")
    private Double actualQ3 = 0.0;

    @Column(name = "ACTUAL_Q4")
    private Double actualQ4 = 0.0;

    @Column(name = "DEMAND_Q1")
    private Double demandQ1 = 0.0;

    @Column(name = "DEMAND_Q2")
    private Double demandQ2 = 0.0;

    @Column(name = "DEMAND_Q3")
    private Double demandQ3 = 0.0;

    @Column(name = "DEMAND_Q4")
    private Double demandQ4 = 0.0;

    @OneToMany(mappedBy = "progress")
    private List<Detail> details;

    @OneToMany(mappedBy = "progress")
    private List<Expense> expenses;

    @Column(name = "EXPENSE")
    private Double expense = 0.0;

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
        if (expenses!=null) {
            expense = expenses.stream().filter(o -> o.getAccepted()).map(o -> o.getAmountKEuro()).reduce(0.0, Double::sum);
        } else {
            expense = 0.0;
        }
        this.expenses = expenses;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        if (details!=null && budget!=null) {
            demandQ1 = details.stream()
                    .filter(o -> o.getPriority() != null && o.getPriority().isLessOrEqual(budget.getPrioThreshold()))
                    .map(Detail::getMdQ1)
                    .reduce(0.0, Double::sum);
            demandQ2 = details.stream()
                    .filter(o -> o.getPriority() != null && o.getPriority().isLessOrEqual(budget.getPrioThreshold()))
                    .map(Detail::getMdQ2)
                    .reduce(0.0, Double::sum);
            demandQ3 = details.stream()
                    .filter(o -> o.getPriority() != null && o.getPriority().isLessOrEqual(budget.getPrioThreshold()))
                    .map(Detail::getMdQ3)
                    .reduce(0.0, Double::sum);
            demandQ4 = details.stream()
                    .filter(o -> o.getPriority() != null && o.getPriority().isLessOrEqual(budget.getPrioThreshold()))
                    .map(Detail::getMdQ4)
                    .reduce(0.0, Double::sum);
        } else {
            demandQ1 = 0.0;
            demandQ2 = 0.0;
            demandQ3 = 0.0;
            demandQ4 = 0.0;
        }
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