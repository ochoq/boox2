package com.choqnet.budget.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;

import javax.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "EXPENSE", indexes = {
        @Index(name = "IDX_EXPENSE_BUDGET_ID", columnList = "BUDGET_ID"),
        @Index(name = "IDX_EXPENSE_IPRB_ID", columnList = "IPRB_ID"),
        @Index(name = "IDX_EXPENSE_PROGRESS_ID", columnList = "PROGRESS_ID")
})
@Entity
public class Expense {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @NumberFormat(pattern = "0.00")
    @Column(name = "AMOUNT")
    private Double amountKEuro = 0.0;

    @Column(name = "CAPEX")
    private Boolean capex = false;

    @Column(name = "ACCEPTED")
    private Boolean accepted = true;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "BUDGET_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Budget budget;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "IPRB_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private IPRB iprb;

    @JoinColumn(name = "PROGRESS_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Progress progress;

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public IPRB getIprb() {
        return iprb;
    }

    public void setIprb(IPRB iprb) {
        this.iprb = iprb;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Boolean getCapex() {
        return capex;
    }

    public void setCapex(Boolean capex) {
        this.capex = capex;
    }

    public Double getAmountKEuro() {
        return amountKEuro;
    }

    public void setAmountKEuro(Double amountKEuro) {
        this.amountKEuro = amountKEuro;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}