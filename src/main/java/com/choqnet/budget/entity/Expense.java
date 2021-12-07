package com.choqnet.budget.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;

import javax.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "EXPENSE")
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

    @JoinColumn(name = "DEMAND_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Demand demand;

    public Demand getDemand() {
        return demand;
    }

    public void setDemand(Demand demand) {
        this.demand = demand;
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