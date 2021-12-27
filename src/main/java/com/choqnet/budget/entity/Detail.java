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

    @NumberFormat(pattern = "0.00")
    @Column(name = "MD_Y")
    private Double mdY = 0.0;

    @NumberFormat(pattern = "0.00")
    @Column(name = "MD_Q1")
    private Double mdQ1 = 0.0;

    @NumberFormat(pattern = "0.00")
    @Column(name = "MD_Q2")
    private Double mdQ2 = 0.0;

    @NumberFormat(pattern = "0.00")
    @Column(name = "MD_Q3")
    private Double mdQ3 = 0.0;

    @NumberFormat(pattern = "0.00")
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
    @DependsOnProperties({"topic", "team"})
    public String getInstanceName() {
        return String.format("%s %s", topic, team);
    }
}