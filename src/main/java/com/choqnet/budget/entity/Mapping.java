package com.choqnet.budget.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@JmixEntity
@Table(name = "MAPPING")
@Entity
public class Mapping {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "IS_MD")
    private Boolean isMD = true;

    @Column(name = "COL_NP")
    private Integer colNP = 0;

    @Column(name = "COL_OGP")
    private Integer colOGP= 0;

    @Column(name = "COL_RUN")
    private Integer colRUN = 0;

    @Column(name = "CODE")
    private String code;

    @InstanceName
    @Column(name = "DESCRIPTION")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getColRUN() {
        return colRUN;
    }

    public void setColRUN(Integer colRUN) {
        this.colRUN = colRUN;
    }

    public Integer getColOGP() {
        return colOGP;
    }

    public void setColOGP(Integer colOGP) {
        this.colOGP = colOGP;
    }

    public Integer getColNP() {
        return colNP;
    }

    public void setColNP(Integer colNP) {
        this.colNP = colNP;
    }

    public Boolean getIsMD() {
        return isMD;
    }

    public void setIsMD(Boolean isMD) {
        this.isMD = isMD;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}