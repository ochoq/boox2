package com.choqnet.budget.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum IPRBRevenueCategory implements EnumClass<String> {

    RI("Revenue increase"),
    PR("Protective revenue"),
    CS("Cost saving");

    private String id;

    IPRBRevenueCategory(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static IPRBRevenueCategory fromId(String id) {
        for (IPRBRevenueCategory at : IPRBRevenueCategory.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}