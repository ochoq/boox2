package com.choqnet.budget.entity;

import com.choqnet.budget.entity.datalists.*;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "IPRB")
@Entity(name="IPRB")
public class IPRB {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @NotNull
    @Column(name = "REFERENCE", nullable = false, unique = true)
    private String reference;

    @Column(name = "NAME", unique = true)
    private String name;

    @Column(name = "PORTFOLIO_CLASSIFICATION")
    private String portfolioClassification;

    @Column(name = "LEGAL_ENTITY")
    private String legalEntity;

    @Column(name = "STRATEGIC_PROGRAM")
    private String strategicProgram;

    @Column(name = "ACTIVITY_TYPE")
    private String activityType;

    @Column(name = "NEW_PRODUCT_INDICATOR")
    private String newProductIndicator;

    @Column(name = "GROUP_OFFERING")
    private String groupOffering;

    @Column(name = "OWNER")
    private String owner;

    @Column(name = "EST_CAPI")
    private String estCAPI;

    @Column(name = "EST_OOI")
    private String estOOI;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "COMMENT_")
    @Lob
    private String comment;

    @Column(name = "UPDATED")
    private LocalDateTime updated = LocalDateTime.now();

    @Column(name = "PROJECT_TYPE")
    private String projectType;

    @Column(name = "FIRST_MONTH_OFF")
    private String firstMonthOff;

    @Column(name = "OUT_BUDGET")
    private Boolean outBudget = false;

    public Boolean getOutBudget() {
        return outBudget==null ? false : outBudget;
    }

    public void setOutBudget(Boolean outBudget) {
        this.outBudget = outBudget;
    }

    public String getFirstMonthOff() {
        return firstMonthOff;
    }

    public void setFirstMonthOff(String firstMonthOff) {
        this.firstMonthOff = firstMonthOff;
    }

    public ProjectType getProjectType() {
        return projectType == null ? null : ProjectType.fromId(projectType);
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType == null ? null : projectType.getId();
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public OOI getEstOOI() {
        return estOOI == null ? null : OOI.fromId(estOOI);
    }

    public void setEstOOI(OOI estOOI) {
        this.estOOI = estOOI == null ? null : estOOI.getId();
    }

    public CAPI getEstCAPI() {
        return estCAPI == null ? null : CAPI.fromId(estCAPI);
    }

    public void setEstCAPI(CAPI estCAPI) {
        this.estCAPI = estCAPI == null ? null : estCAPI.getId();
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public GroupOffering getGroupOffering() {
        return groupOffering == null ? null : GroupOffering.fromId(groupOffering);
    }

    public void setGroupOffering(GroupOffering groupOffering) {
        this.groupOffering = groupOffering == null ? null : groupOffering.getId();
    }

    public NewProductIndicator getNewProductIndicator() {
        return newProductIndicator == null ? null : NewProductIndicator.fromId(newProductIndicator);
    }

    public void setNewProductIndicator(NewProductIndicator newProductIndicator) {
        this.newProductIndicator = newProductIndicator == null ? null : newProductIndicator.getId();
    }

    public ActivityType getActivityType() {
        return activityType == null ? null : ActivityType.fromId(activityType);
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType == null ? null : activityType.getId();
    }

    public StrategicProgram getStrategicProgram() {
        return strategicProgram == null ? null : StrategicProgram.fromId(strategicProgram);
    }

    public void setStrategicProgram(StrategicProgram strategicProgram) {
        this.strategicProgram = strategicProgram == null ? null : strategicProgram.getId();
    }

    public LegalEntity getLegalEntity() {
        return legalEntity == null ? null : LegalEntity.fromId(legalEntity);
    }

    public void setLegalEntity(LegalEntity legalEntity) {
        this.legalEntity = legalEntity == null ? null : legalEntity.getId();
    }

    public PortfolioClassification getPortfolioClassification() {
        return portfolioClassification == null ? null : PortfolioClassification.fromId(portfolioClassification);
    }

    public void setPortfolioClassification(PortfolioClassification portfolioClassification) {
        this.portfolioClassification = portfolioClassification == null ? null : portfolioClassification.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}