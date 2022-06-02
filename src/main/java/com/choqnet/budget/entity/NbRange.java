package com.choqnet.budget.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "NB_RANGE", indexes = {
        @Index(name = "IDX_NBRANGE_SETUP_NB_ID", columnList = "SETUP_NB_ID")
})
@Entity
public class NbRange {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "DATE_", nullable = false)
    @NotNull
    private LocalDate date;

    @Column(name = "VALUE_")
    private Integer value=0;
    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "SETUP_NB_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Setup setupNB;

    public Setup getSetupNB() {
        return setupNB;
    }

    public void setSetupNB(Setup setupNB) {
        this.setupNB = setupNB;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}