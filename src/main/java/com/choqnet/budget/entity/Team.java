package com.choqnet.budget.entity;

import com.choqnet.budget.entity.datalists.GTM;
import com.choqnet.budget.entity.datalists.Source;
import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "TEAM", indexes = {
        @Index(name = "IDX_TEAM_SETUP_ID", columnList = "SETUP_ID")
})
@Entity
public class Team {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "NAME")
    private String name = "";

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @JoinColumn(name = "PARENT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Team parent;

    @Column(name = "SOURCE")
    private String source;

    @Column(name = "SOURCE_NAME")
    private String sourceName;

    @Column(name = "SOURCE_ID")
    private String sourceID;

    @Column(name = "S_ID")
    private Integer sID;

    @Column(name = "MAIN_GTM")
    private String mainGTM;

    @Column(name = "IC_TARGET")
    private String icTarget;

    @Column(name = "ENABLED")
    private Boolean enabled = true;

    @Column(name = "SELECTABLE")
    private Boolean selectable = true;

    @Column(name = "UPDATED")
    private LocalDateTime updated = LocalDateTime.now();

    @Column(name = "FULL_NAME")
    @InstanceName
    private String fullName;

    @Column(name = "LEVEL_")
    private Integer level = 0;

    @Column(name = "T_LINE")
    private String tLine;

    @Column(name = "T_DIV")
    private String tDiv;

    @Column(name = "T_DOMAIN")
    private String tDomain;

    @Column(name = "SIMPLE_DOMAIN")
    private String simpleDomain;

    @Column(name = "SIMPLE_PLATFORM")
    private String simplePlatform;

    @Column(name = "IN_BUDGET")
    private Boolean inBudget = false;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @JoinColumn(name = "SETUP_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Setup setup;

    public Integer getSID() {
        try {
            return Integer.valueOf(sourceID);
        } catch (Exception e) {
            return 0;
        }
    }

    public Setup getSetup() {
        return setup;
    }

    public void setSetup(Setup setup) {
        this.setup = setup;
    }

    public Boolean getInBudget() {
        return inBudget==null ? false : inBudget;
    }

    public void setInBudget(Boolean inBudget) {
        this.inBudget = inBudget;
    }

    public String getSimplePlatform() {
        if (icTarget==null) {
            return "Not assigned";
        }
        return icTarget;
    }

    public String getSimpleDomain() {
        if (tDomain==null) {
            return "OTHER";
        }
        switch(tDomain.toUpperCase()) {
            case "IN-STORE":
                return "IN-STORE";
            case "OMNICHANNEL":
                return "OCH";
            case "ONLINE":
                return "ONLINE";
            case "ECOMMERCE":
                return "ECOM";
            default:
                return "OTHER";
        }
    }

    public String getTDomain() {
        return tDomain;
    }

    public void setTDomain(String tDomain) {
        this.tDomain = tDomain;
    }

    public String getTDiv() {
        return tDiv;
    }

    public void setTDiv(String tDiv) {
        this.tDiv = tDiv;
    }

    public String getTLine() {
        return tLine;
    }

    public void setTLine(String tLine) {
        this.tLine = tLine;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getLevel() {
        return level==null ? 0 : level;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public Boolean getSelectable() {
        return selectable;
    }

    public void setSelectable(Boolean selectable) {
        this.selectable = selectable;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getIcTarget() {
        return icTarget;
    }

    public void setIcTarget(String icTarget) {
        this.icTarget = icTarget;
    }

    public GTM getMainGTM() {
        return mainGTM == null ? null : GTM.fromId(mainGTM);
    }

    public void setMainGTM(GTM mainGTM) {
        this.mainGTM = mainGTM == null ? null : mainGTM.getId();
    }

    public String getSourceID() {
        return sourceID==null ? "" : sourceID;
    }

    public void setSourceID(String sourceID) {
        this.sourceID = sourceID;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Source getSource() {
        return source == null ? Source.MN : Source.fromId(source);
    }

    public void setSource(Source source) {
        this.source = source == null ? null : source.getId();
    }

    public String getFullName() {
        return fullName;
    }

    public Team getParent() {
        return parent;
    }

    public void setParent(Team parent) {
        this.parent = parent;
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

    // *** Functions to get data from Setup
    public int getRateAt(LocalDate date) {
        return setup==null ? 0 : setup.getRateAtDate(date);
    }

    public int getWorkDaysAt(LocalDate date) {
        return setup==null ? 0 : setup.getWorkDaysAtDate(date);
    }

    public int getWorkDays(String year) {
        try {
            int intYear = Integer.parseInt(year);
            return getWorkDaysAt(LocalDate.of(intYear, 1, 1));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getRateQx(String month) {
        try {
            int intYear = Integer.parseInt(month.substring(3, 7));
            int intMonth = Integer.parseInt(month.substring(7));
            return getRateAt(LocalDate.of(intYear, intMonth, 1));
        } catch (Exception e) {
            return 0;
        }

    }
}