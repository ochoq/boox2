package com.choqnet.budget.entity;

import com.choqnet.budget.entity.datalists.Priority;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;

import java.util.UUID;

@JmixEntity
public class CPTeam {
    @JmixGeneratedValue
    @JmixId
    private UUID id;

    private Capacity capacity;

    private String priority;

    @NumberFormat(pattern = "0.00")
    private Double demandQ1;

    @NumberFormat(pattern = "0.00")
    private Double demandQ2;

    @NumberFormat(pattern = "0.00")
    private Double demandQ3;

    @NumberFormat(pattern = "0.00")
    private Double demandQ4;

    private String labelQ1;

    private String labelQ2;

    private String labelQ3;

    private String labelQ4;

    public void setPriority(Priority priority) {
        this.priority = priority == null ? null : priority.getId();
    }

    public Priority getPriority() {
        return priority == null ? null : Priority.fromId(priority);
    }

    public String getLabelQ4() {
        return demandQ4 + " / " + capacity.getMdQ4();
    }

    public String getLabelQ3() {
        return demandQ3 + " / " + capacity.getMdQ3();
    }

    public String getLabelQ2() {
        return demandQ2 + " / " + capacity.getMdQ2();
    }

    public String getLabelQ1() {
        return demandQ1 + " / " + capacity.getMdQ1();
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

    public void setCapacity(Capacity capacity) {
        this.capacity = capacity;
    }

    public Capacity getCapacity() {
        return capacity;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}