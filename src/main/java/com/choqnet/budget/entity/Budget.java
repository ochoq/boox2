package com.choqnet.budget.entity;

import com.choqnet.budget.entity.datalists.Priority;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "BUDGET")
@Entity
public class Budget {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @Pattern(message = "This must be a year between 2020 and 2029", regexp = "(?:(?:202)[0-9]{1})")
    @Column(name = "YEAR_")
    private String year;

    @Composition
    @OneToMany(mappedBy = "budget")
    private List<Capacity> capacities;

    @Column(name = "CLOSE_Q1")
    private Boolean closeQ1 = false;

    @Column(name = "CLOSE_Q2")
    private Boolean closeQ2 = false;

    @Column(name = "CLOSE_Q3")
    private Boolean closeQ3 = false;

    @Column(name = "CLOSE_Q4")
    private Boolean closeQ4 = false;

    @Column(name = "FROZEN")
    private Boolean frozen = false;

    @Column(name = "PRIO_THRESHOLD")
    private String prioThreshold = "P5";

    @Column(name = "PREFERRED")
    private Boolean preferred = false;

    public Boolean getPreferred() {
        return preferred != null && preferred;
    }

    public void setPreferred(Boolean preferred) {
        this.preferred = preferred;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Priority getPrioThreshold() {
        return prioThreshold == null ? null : Priority.fromId(prioThreshold);
    }

    public void setPrioThreshold(Priority prioThreshold) {
        this.prioThreshold = prioThreshold == null ? null : prioThreshold.getId();
    }

    public Boolean getFrozen() {
        return frozen;
    }

    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }

    public Boolean getCloseQ4() {
        return closeQ4;
    }

    public void setCloseQ4(Boolean closeQ4) {
        this.closeQ4 = closeQ4;
    }

    public Boolean getCloseQ3() {
        return closeQ3;
    }

    public void setCloseQ3(Boolean closeQ3) {
        this.closeQ3 = closeQ3;
    }

    public Boolean getCloseQ2() {
        return closeQ2;
    }

    public void setCloseQ2(Boolean closeQ2) {
        this.closeQ2 = closeQ2;
    }

    public Boolean getCloseQ1() {
        return closeQ1;
    }

    public void setCloseQ1(Boolean closeQ1) {
        this.closeQ1 = closeQ1;
    }

    public List<Capacity> getCapacities() {
        return capacities;
    }

    public void setCapacities(List<Capacity> capacities) {
        this.capacities = capacities;
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

    public boolean getCloseQx(int i) {
        switch (i) {
            case 1:
                return closeQ1;
            case 2:
                return closeQ2;
            case 3:
                return closeQ3;
            case 4:
                return closeQ4;
            default:
                return false;
        }
    }
}