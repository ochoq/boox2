package com.choqnet.budget.entity;

import com.choqnet.budget.entity.datalists.GTM;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@JmixEntity
@Table(name = "ONE_PAGER")
@Entity
public class OnePager {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "GTM")
    private String gtm;

    @Column(name = "VALUE_")
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public GTM getGtm() {
        return gtm == null ? null : GTM.fromId(gtm);
    }

    public void setGtm(GTM gtm) {
        this.gtm = gtm == null ? null : gtm.getId();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @InstanceName
    @DependsOnProperties({"gtm", "value"})
    public String getInstanceName() {
        return String.format("%s %s", gtm, value);
    }
}