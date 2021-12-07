package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum ActivityType implements EnumClass<String> {

    PSI("RD-Product Solution Investments"),
    PET("RD-Product Efficiency and Tooling"),
    PMR("RD-Product Mandatory and Regulation"),
    PSM("RD-Product Solution Maintenance"),
    PMG("RD-Product Migration"),
    IET("INTP-Internal Efficiency and Tooling"),
    IRM("INTP-Regulatory and Mandatory"),
    PCP("PROD-Customer Projects"),
    RUN("Run");

    private String id;

    ActivityType(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ActivityType fromId(String id) {
        for (ActivityType at : ActivityType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}