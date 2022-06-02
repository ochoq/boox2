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

    @Column(name = "R2022Q1")
    private Integer r2022Q1 = 0;

    @Column(name = "R2022Q2")
    private Integer r2022Q2 = 0;

    @Column(name = "R2022Q3")
    private Integer r2022Q3 = 0;

    @Column(name = "R2022Q4")
    private Integer r2022Q4 = 0;

    @Column(name = "R2023")
    private Integer r2023 = 0;

    @Column(name = "R2023Q1")
    private Integer r2023Q1 = 0;

    @Column(name = "R2023Q2")
    private Integer r2023Q2 = 0;

    @Column(name = "R2023Q3")
    private Integer r2023Q3 = 0;

    @Column(name = "R2023Q4")
    private Integer r2023Q4 = 0;

    public void setR2022Q1(Integer r2022Q1) {
        this.r2022Q1 = r2022Q1;
    }

    public Integer getR2022Q1() {
        return r2022Q1 == null ? 0 : r2022Q1;
    }

    public void setR2022Q2(Integer r2022Q2) {
        this.r2022Q2 = r2022Q2;
    }

    public Integer getR2022Q2() {
        return r2022Q2 == null ? 0 : r2022Q2;
    }

    public void setR2023Q1(Integer r2023Q1) {
        this.r2023Q1 = r2023Q1;
    }

    public Integer getR2023Q1() {
        return r2023Q1 == null ? 0 : r2023Q1;
    }

    public Integer getR2023Q4() {
        return r2023Q4 == null ? 0 : r2023Q4;
    }

    public void setR2023Q4(Integer r2023Q4) {
        this.r2023Q4 = r2023Q4;
    }

    public Integer getR2023Q3() {
        return r2023Q3 == null ? 0 : r2023Q3;
    }

    public void setR2023Q3(Integer r2023Q3) {
        this.r2023Q3 = r2023Q3;
    }

    public Integer getR2023Q2() {
        return r2023Q2 == null ? 0 : r2023Q2;
    }

    public void setR2023Q2(Integer r2023Q2) {
        this.r2023Q2 = r2023Q2;
    }

    public Integer getR2022Q4() {
        return r2022Q4 == null ? 0 : r2022Q4;
    }

    public void setR2022Q4(Integer r2022Q4) {
        this.r2022Q4 = r2022Q4;
    }

    public Integer getR2022Q3() {
        return r2022Q3 == null ? 0 : r2022Q3;
    }

    public void setR2022Q3(Integer r2022Q3) {
        this.r2022Q3 = r2022Q3;
    }

    public Integer getR2023() {
        return r2023 == null ? 0 : r2023;
    }

    public void setR2023(Integer r2023) {
        this.r2023 = r2023;
    }

    public Integer getR2022() {
        return r2022 == null ? 0 : r2022;
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
        String year = month.substring(3, 7);
        switch (year) {
            case "2022":
                return wd2022;
            case "2023":
                return wd2023;
            default: {
                alert(year);
                return 0;
            }
        }
    }

    public int getRate(String month) {
        String year = month.substring(3, 7);
        switch (year) {
            case "2022":
                return r2022;
            case "2023":
                return r2023;
            default: {
                alert(year);
                return 0;
            }
        }
    }

    public int getRateQx(String month) {
        String quarter = month.substring(3);
        switch (quarter) {
            case "202201":
            case "202202":
            case "202203":
            case "2022Q1":
                return r2022Q1==null?0:r2022Q1;
            case "202204":
            case "202205":
            case "202206":
            case "2022Q2":
                return r2022Q2==null?0:r2022Q2;
            case "202207":
            case "202208":
            case "202209":
            case "2022Q3":
                return r2022Q3==null?0:r2022Q3;
            case "202210":
            case "202211":
            case "202212":
            case "2022Q4":
                return r2022Q4==null?0:r2022Q4;
            case "202301":
            case "202302":
            case "202303":
            case "2023Q1":
                return r2023Q1==null?0:r2023Q1;
            case "202304":
            case "202305":
            case "202306":
            case "2023Q2":
                return r2023Q2==null?0:r2023Q2;
            case "202307":
            case "202308":
            case "202309":
            case "2023Q3":
                return r2023Q3==null?0:r2023Q3;
            case "202310":
            case "202311":
            case "202312":
            case "2023Q4":
                return r2023Q4==null?0:r2023Q4;
            default:
                alert(quarter);
                return 0;
        }
    }

    private void alert(String year) {
        log.error("No Setup data for year " + year);
    }
}