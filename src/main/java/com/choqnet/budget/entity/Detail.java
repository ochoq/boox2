package com.choqnet.budget.entity;

import com.choqnet.budget.entity.datalists.Category;
import com.choqnet.budget.entity.datalists.Priority;
import com.choqnet.budget.entity.datalists.TShirt;
import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;

import javax.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "DETAIL", indexes = {
        @Index(name = "IDX_DETAIL_BUDGET_ID", columnList = "BUDGET_ID"),
        @Index(name = "IDX_DETAIL_IPRB_ID", columnList = "IPRB_ID"),
        @Index(name = "IDX_DETAIL_PROGRESS_ID", columnList = "PROGRESS_ID")
})
@Entity
public class Detail {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "TEAM_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    @Column(name = "TOPIC")
    private String topic;

    @Column(name = "ROADMAP")
    private String roadmap;

    @Column(name = "DETAIL")
    private String detail;

    @Column(name = "T_SHIRT")
    private Integer tShirt;

    @NumberFormat(pattern = "#,##0")
    @Column(name = "MD_Y")
    private Double mdY = 0.0;

    @NumberFormat(pattern = "#,##0")
    @Column(name = "MD_Q1")
    private Double mdQ1 = 0.0;

    @NumberFormat(pattern = "#,##0")
    @Column(name = "MD_Q2")
    private Double mdQ2 = 0.0;

    @NumberFormat(pattern = "#,##0")
    @Column(name = "MD_Q3")
    private Double mdQ3 = 0.0;

    @NumberFormat(pattern = "#,##0")
    @Column(name = "MD_Q4")
    private Double mdQ4 = 0.0;

    @Column(name = "MD_NY")
    private Double mdNY;

    @JoinColumn(name = "ONE_PAGER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private OnePager onePager;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "PRIORITY")
    private String priority = Priority.P1.getId();

    @Column(name = "JIRA")
    private String jira;

    @Column(name = "SIMPLE_DOMAIN")
    private String simpleDomain;

    @Column(name = "SIMPLE_PLATFORM")
    private String simplePlatform;

    @Column(name = "INCLUDED")
    private Boolean included;

    @Column(name = "TYPE_")
    private String type;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "BUDGET_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Budget budget;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "IPRB_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private IPRB iprb;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "PROGRESS_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Progress progress;

    @Column(name = "REMAINING")
    private Double remaining = 0.0;

    @NumberFormat(pattern = "#,##0.0")
    @Column(name = "BUDGET_COST")
    private Double budgetCost;

    @Column(name = "BUDGET_COST_Q1")
    private Double budgetCostQ1;

    @Column(name = "BUDGET_COST_Q2")
    private Double budgetCostQ2;

    @Column(name = "BUDGET_COST_Q3")
    private Double budgetCostQ3;

    @Column(name = "BUDGET_COST_Q4")
    private Double budgetCostQ4;

    public Double getMdNY() {
        return mdNY==null ? 0.0 : mdNY;
    }

    public void setMdNY(Double mdNY) {
        this.mdNY = mdNY;
    }

    public Double getBudgetCostQ4() {
        return budgetCostQ4==null ? 0.0 : budgetCostQ4;
    }

    public void setBudgetCostQ4(Double budgetCostQ4) {
        this.budgetCostQ4 = budgetCostQ4;
    }

    public Double getBudgetCostQ3() {
        return budgetCostQ3==null ? 0.0 : budgetCostQ3;
    }

    public void setBudgetCostQ3(Double budgetCostQ3) {
        this.budgetCostQ3 = budgetCostQ3;
    }

    public Double getBudgetCostQ2() {
        return budgetCostQ2==null ? 0.0 : budgetCostQ2;
    }

    public void setBudgetCostQ2(Double budgetCostQ2) {
        this.budgetCostQ2 = budgetCostQ2;
    }

    public Double getBudgetCostQ1() {
        return budgetCostQ1==null ? 0.0 : budgetCostQ1;
    }

    public void setBudgetCostQ1(Double budgetCostQ1) {
        this.budgetCostQ1 = budgetCostQ1;
    }

    // todo : better implementation of cost for details

    public Double getBudgetCost() {
        return budgetCost==null ? 0.0 : budgetCost;
    }

    public void setBudgetCost(Double budgetCost) {
        this.budgetCost = budgetCost;
    }


    public Double getRemaining() {
        if (budget==null || budget.getFrozen()) return 0.0;
        return (budget.getCloseQ1() ? 0.0 : (mdQ1==null ? 0.0 : mdQ1))  +
               (budget.getCloseQ2() ? 0.0 : (mdQ2==null ? 0.0 : mdQ2))  +
               (budget.getCloseQ3() ? 0.0 : (mdQ3==null ? 0.0 : mdQ3))  +
               (budget.getCloseQ4() ? 0.0 : (mdQ4==null ? 0.0 : mdQ4)) ;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public IPRB getIprb() {
        return iprb;
    }

    public void setIprb(IPRB iprb) {
        this.iprb = iprb;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public String getType() {
        if (progress==null || progress.getIprb()==null || progress.getIprb().getStrategicProgram()==null) {
            return "BAU";
        }
        switch(progress.getIprb().getStrategicProgram().toString()) {
            case "RUN":
                return "RUN";
            case "UNI":
            case "M_P":
                return "UMP";
            default:
                return "BAU";
        }
    }

    public String getSimplePlatform() {
        if (team==null || team.getIcTarget()==null) {
            return "Not assigned";
        }
        return team.getSimplePlatform();
    }

    public String getSimpleDomain() {
        if (team==null || team.getTDomain()==null) {
            return "OTHER";
        }
        return team.getSimpleDomain();
    }

    public Boolean getIncluded() {
        // is included in the budget if:
        // - there is a team connected
        // - priority is equal or less than the budget's threshold
        // - the IPRB is not out Budget
        // - the IPRB is not out Budget
        included = (progress != null) &&
                (team!=null) &&
                (getPriority().isLessOrEqual(progress.getBudget().getPrioThreshold())) &&
                (!progress.getIprb().getOutBudget());
        return included;
    }

    public String getTLine() {
        return team==null ? "" : team.getTLine();
    }

    public String getTFullName() {
        return team==null ? "" : team.getFullName();
    }

    public String getJira() {
        return jira==null ? "" : jira;
    }

    public void setJira(String jira) {
        this.jira = jira;
    }

    public void setOnePager(OnePager onePager) {
        this.onePager = onePager;
    }

    public OnePager getOnePager() {
        return onePager;
    }

    public Priority getPriority() {
        return priority == null ? Priority.P5 : Priority.fromId(priority);
    }

    public void setPriority(Priority priority) {
        this.priority = priority == null ? null : priority.getId();
    }

    public Category getCategory() {
        return category == null ? null : Category.fromId(category);
    }

    public void setCategory(Category category) {
        this.category = category == null ? null : category.getId();
    }

    public TShirt getTShirt() {
        return tShirt == null ? null : TShirt.fromId(tShirt);
    }

    public void setTShirt(TShirt tShirt) {
        this.tShirt = (tShirt == null ? 0 : tShirt.getId());
    }

    public Double getMdQ4() {
        return mdQ4 == null ? 0.0 : mdQ4;
    }

    public void setMdQ4(Double mdQ4) {
        this.mdQ4 = mdQ4;
    }

    public Double getMdQ3() {
        return mdQ3 == null ? 0.0 : mdQ3;
    }

    public void setMdQ3(Double mdQ3) {
        this.mdQ3 = mdQ3;
    }

    public Double getMdQ2() {
        return mdQ2 == null ? 0.0 : mdQ2;
    }

    public void setMdQ2(Double mdQ2) {
        this.mdQ2 = mdQ2;
    }

    public Double getMdQ1() {
        return mdQ1 == null ? 0.0 : mdQ1;
    }

    public void setMdQ1(Double mdQ1) {
        this.mdQ1 = mdQ1;
    }

    public Double getMdY() {
        return mdY == null ? 0.0 : mdY;
    }

    public void setMdY(Double mdY) {
        this.mdY = mdY;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getRoadmap() {
        return roadmap;
    }

    public void setRoadmap(String roadmap) {
        this.roadmap = roadmap;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Double getQx(int i)  {
        switch (i) {
            case 1:
                return getMdQ1();
            case 2:
                return getMdQ2();
            case 3:
                return getMdQ3();
            case 4:
                return getMdQ4();
            default:
                return 0.0;
        }
    }

    @InstanceName
    @DependsOnProperties({"roadmap", "team"})
    public String getInstanceName() {
        return String.format("<i> %s - %s", roadmap==null ? "" : roadmap, team==null ? "" : team.getName());
    }
}