package com.choqnet.budget.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.slf4j.Logger;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    @Composition
    @OrderBy("date DESC")
    @OneToMany(mappedBy = "setup")
    private List<IntRange> rates;

    @Composition
    @OrderBy("date DESC")
    @OneToMany(mappedBy = "setupNB")
    private List<NbRange> workDays;

    @Column(name = "RATE_NOW")
    private Integer rateNow;

    @Column(name = "WORK_DAYS_NOW")
    private Integer workDaysNow;

    public Integer getWorkDaysNow() {
        // value of today
        return getWorkDaysAtDate(LocalDate.now());
    }

    public Integer getRateNow() {
        // value of today
        return getRateAtDate(LocalDate.now());

    }

    public void setWorkDays(List<NbRange> workDays) {
        this.workDays = workDays;
    }

    public List<NbRange> getWorkDays() {
        return workDays;
    }

    public List<IntRange> getRates() {
        return rates;
    }

    public void setRates(List<IntRange> rates) {
        this.rates = rates;
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

    // *** NEW DATA FUNCTIONS
    public int getRateAtDate(LocalDate date) {
        // for Debug -> DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Optional<IntRange> target = rates.stream().filter(e -> date.compareTo(e.getDate())>=0).findFirst();
        if (target.isPresent()) {
            // for Debug -> log.info(dtf.format(date) + " is found in range starting @ " + dtf.format(target.get().getDate()));
            return target.get().getValue();
        } else {
            // for Debug -> log.info(dtf.format(date) + " is not found");
            return 0;
        }
    }

    public int getWorkDaysAtDate(LocalDate date) {
        // for Debug -> DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Optional<NbRange> target = workDays.stream().filter(e -> date.compareTo(e.getDate())>=0).findFirst();
        if (target.isPresent()) {
            // for Debug -> log.info(dtf.format(date) + " is found in range starting @ " + dtf.format(target.get().getDate()));
            return target.get().getValue();
        } else {
            // for Debug -> log.info(dtf.format(date) + " is not found");
            return 0;
        }
    }

    // *** DEPRECATED FUNCTIONS

    /*
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
    */


}