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
@Table(name = "DETAIL")
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

    @NumberFormat(pattern = "#,##0", groupingSeparator = " ")
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

    @JoinColumn(name = "ONE_PAGER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private OnePager onePager;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "PRIORITY")
    private String priority = Priority.P5.getId();

    @JoinColumn(name = "DEMAND_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Demand demand;

    @Column(name = "JIRA")
    private String jira;

    @Column(name = "T_FULL_NAME")
    private String tFullName;

    @Column(name = "T_LINE")
    private String tLine;

    @Column(name = "SIMPLE_DOMAIN")
    private String simpleDomain;

    @Column(name = "SIMPLE_PLATFORM")
    private String simplePlatform;

    @Column(name = "INCLUDED")
    private Boolean included;

    @Column(name = "TYPE_")
    private String type;

    public String getType() {
        if (demand==null || demand.getIprb()==null || demand.getIprb().getStrategicProgram()==null) {
            return "BAU";
        }
        switch(demand.getIprb().getStrategicProgram().toString()) {
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
        boolean bTeam = team!=null;
        boolean bPrio = getPriority().isLessOrEqual(demand.getBudget().getPrioThreshold());
        boolean bIPRB = !demand.getIprb().getOutBudget();
        included = (team!=null) && (getPriority().isLessOrEqual(demand.getBudget().getPrioThreshold())) && (!demand.getIprb().getOutBudget());
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


    public Demand getDemand() {
        return demand;
    }

    public void setDemand(Demand demand) {
        this.demand = demand;
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
        return String.format("<i> %s - %s", roadmap, team.getName());
    }
}