package com.choqnet.budget.entity;

import com.choqnet.budget.entity.datalists.Priority;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;

import java.text.DecimalFormat;
import java.util.UUID;

@JmixEntity
public class CPTeam {
    @JmixGeneratedValue
    @JmixId
    private UUID id;

    private Capacity capacity;

    private String priority;

    @NumberFormat(pattern = "0.00")
    private Double demandQ1;

    @NumberFormat(pattern = "0.00")
    private Double demandQ2;

    @NumberFormat(pattern = "0.00")
    private Double demandQ3;

    @NumberFormat(pattern = "0.00")
    private Double demandQ4;

    private String labelQ1;

    private String labelQ2;

    private String labelQ3;

    private String labelQ4;

    private String labelY;

    private DecimalFormat df = new DecimalFormat("#,###");

    private String spacer;

    public String getSpacer() {
        return " ";
    }

    public void setPriority(Priority priority) {
        this.priority = priority == null ? null : priority.getId();
    }

    public Priority getPriority() {
        return priority == null ? null : Priority.fromId(priority);
    }

    public String getLabelY() {
        return df.format(getDemandY()) + " / " + df.format(getCapacityY());
    }

    public String getLabelQ4() {
        return df.format(demandQ4) + " / " + df.format(capacity.getMdQ4());
    }

    public String getLabelQ3() {
        return df.format(demandQ3) + " / " + df.format(capacity.getMdQ3());
    }

    public String getLabelQ2() {
        return df.format(demandQ2) + " / " + df.format(capacity.getMdQ2());
    }

    public String getLabelQ1() {
        return df.format(demandQ1) + " / " + df.format(capacity.getMdQ1());
    }

    public Double getDemandQ4() {
        return demandQ4;
    }

    public void setDemandQ4(Double demandQ4) {
        this.demandQ4 = demandQ4;
    }

    public Double getDemandQ3() {
        return demandQ3;
    }

    public void setDemandQ3(Double demandQ3) {
        this.demandQ3 = demandQ3;
    }

    public Double getDemandQ2() {
        return demandQ2;
    }

    public void setDemandQ2(Double demandQ2) {
        this.demandQ2 = demandQ2;
    }

    public Double getDemandQ1() {
        return demandQ1;
    }

    public void setDemandQ1(Double demandQ1) {
        this.demandQ1 = demandQ1;
    }

    public void setCapacity(Capacity capacity) {
        this.capacity = capacity;
    }

    public Capacity getCapacity() {
        return capacity;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    // *** yearly value
    public Double getDemandY() {
        return demandQ1 + demandQ2 +demandQ3 +demandQ4;
    }

    public  Double getCapacityY() {
        return getCapacity().getMdQ1() + getCapacity().getMdQ2() + getCapacity().getMdQ3() +getCapacity().getMdQ4();
    }

    // *** styles
    public String getStyleY() {
        return setStyle(getDemandY(), getCapacityY());
    }

    public String getStyleQ4() {
        return setStyle(demandQ4, capacity.getMdQ4());
    }

    public String getStyleQ3() {
        return setStyle(demandQ3, capacity.getMdQ3());
    }

    public String getStyleQ2() {
        return setStyle(demandQ2, capacity.getMdQ2());
    }

    public String getStyleQ1() {
        return setStyle(demandQ1, capacity.getMdQ1());
    }

    private String setStyle(Double demand, Double capacity) {
        if (Double.compare(capacity,0.0)==0) {
            if (Double.compare(demand,0.0)==0) {
                // no capa, no demand --> no style
                return "gray";
            } else  {
                return "red";
            }
        } else {
            double ratio = demand / capacity;
            if (Double.compare(ratio, 1.2)>0) {
                return "red";
            } else {
                if (Double.compare(ratio, 1.05)>0) {
                    return "orange";
                } else {
                    return "green";
                }
            }
        }
    }
}