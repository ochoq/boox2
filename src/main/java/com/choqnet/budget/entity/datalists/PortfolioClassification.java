package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum PortfolioClassification implements EnumClass<String> {

    BPR("Business Project (new product/feature)"),
    CRA("Customer Request (arrow)"),
    C_R("Compliance & Regulatory"),
    MSE("Maintenance & Small enhancements"),
    ENV("Envelope"),
    TEC("Technical"),
    OTH("Other (non product project)");

    private final String id;

    PortfolioClassification(String value) {
        this.id = value;
    }



    public String getId() {
        return id;
    }


    @Nullable
    public static PortfolioClassification fromId(String id) {
        for (PortfolioClassification at : PortfolioClassification.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}