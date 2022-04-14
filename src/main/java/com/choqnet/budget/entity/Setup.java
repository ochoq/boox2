package com.choqnet.budget.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.slf4j.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@JmixEntity
@Table(name = "SETUP")
@Entity
public class Setup {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Setup.class);
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "WD2022")
    private Integer wd2022 = 0;

    @Column(name = "WD2023")
    private Integer wd2023 = 0;

    @Column(name = "R2022")
    private Integer r2022 = 0;

    @Column(name = "R2023")
    private Integer r2023 = 0;

    public Integer getR2023() {
        return r2023;
    }

    public void setR2023(Integer r2023) {
        this.r2023 = r2023;
    }

    public Integer getR2022() {
        return r2022;
    }

    public void setR2022(Integer r2022) {
        this.r2022 = r2022;
    }

    public Integer getWd2023() {
        return wd2023;
    }

    public void setWd2023(Integer wd2023) {
        this.wd2023 = wd2023;
    }

    public Integer getWd2022() {
        return wd2022;
    }

    public void setWd2022(Integer wd2022) {
        this.wd2022 = wd2022;
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

    public int getWorkDays(String month) {
        String year = month.substring(3,7);
        switch(year) {
            case "2022": return wd2022;
            case "2023": return wd2023;
            default: {
                alert(year);
                return 0;
            }
        }
    }

    public int getRate(String month) {
        String year = month.substring(3,7);
        switch(year) {
            case "2022": return r2022;
            case "2023": return r2023;
            default: {
                alert(year);
                return 0;
            }
        }
    }

    private void alert(String year) {
        log.error("No Setup data for year " + year);
    }
}