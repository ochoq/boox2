package com.choqnet.budget.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JmixEntity
@Table(name = "DEMAND")
@Entity
public class Demand {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "IPRB_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private IPRB iprb;

    @Composition
    @OneToMany(mappedBy = "demand")
    private List<Detail> details;

    @NumberFormat(pattern = "0.00")
    @Column(name = "MD_Y")
    private Double mdY = 0.0;

    @NumberFormat(pattern = "0.00")
    @Column(name = "MD_Q1")
    private Double mdQ1 = 0.0;

    @NumberFormat(pattern = "0.00")
    @Column(name = "MD_Q2")
    private Double mdQ2 = 0.0;

    @NumberFormat(pattern = "0.00")
    @Column(name = "MD_Q3")
    private Double mdQ3 = 0.0;

    @NumberFormat(pattern = "0.00")
    @Column(name = "MD_Q4")
    private Double mdQ4 = 0.0;

    @Composition
    @OneToMany(mappedBy = "demand")
    private List<Expense> expenses;

    @NumberFormat(pattern = "0.00")
    @Column(name = "EURO_AMOUNT")
    private Double euroAmount;

    @Column(name = "UNICITY", unique = true)
    private String unicity;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "BUDGET_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Budget budget;

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        if (budget!=null && iprb!=null) {
            unicity = iprb.getName() + budget.getName();
        }
        this.budget = budget;
    }

    public String getUnicity() {
        return unicity;
    }

    public Double getEuroAmount() {
        Optional<Double> temp = expenses.stream().filter(o -> o.getAccepted()).map(o -> o.getAmountKEuro()).reduce(Double::sum);
        return temp.orElse(0.0);
    }

    public List<Expense> getEuroDemands() {
        return expenses;
    }

    public void setEuroDemands(List<Expense> euroDemands) {
        this.expenses = euroDemands;
    }

    public Double getMdQ4() {
        Optional<Double> temp = details.stream()
                .filter(o -> o.getPriority().isLessOrEqual(budget.getPrioThreshold()))
                .map(Detail::getMdQ4)
                .reduce(Double::sum);
        return temp.orElse(0.0);
    }

    public Double getMdQ3() {
        Optional<Double> temp = details.stream()
                .filter(o -> o.getPriority().isLessOrEqual(budget.getPrioThreshold()))
                .map(Detail::getMdQ3)
                .reduce(Double::sum);
        return temp.orElse(0.0);
    }

    public Double getMdQ2() {
        Optional<Double> temp = details.stream()
                .filter(o -> o.getPriority().isLessOrEqual(budget.getPrioThreshold()))
                .map(Detail::getMdQ2)
                .reduce(Double::sum);
        return temp.orElse(0.0);
    }

    public Double getMdQ1() {
        Optional<Double> temp = details.stream()
                .filter(o -> o.getPriority().isLessOrEqual(budget.getPrioThreshold()))
                .map(Detail::getMdQ1)
                .reduce(Double::sum);
        return temp.orElse(0.0);
    }

    public Double getMdY() {
        return (mdQ1== null ? 0.0 : mdQ1) +
                (mdQ2== null ? 0.0 : mdQ2) +
                (mdQ3== null ? 0.0 : mdQ3) +
                (mdQ4== null ? 0.0 : mdQ4);
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }

    public IPRB getIprb() {
        return iprb;
    }

    public void setIprb(IPRB iprb) {
        if (budget!=null && iprb!=null) {
            unicity = iprb.getName() + budget.getName();
        }
        this.iprb = iprb;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}