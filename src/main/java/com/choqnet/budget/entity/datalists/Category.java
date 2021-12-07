package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum Category implements EnumClass<String> {

    MAINTENANCE("Maintenance & Incidents"),
    TECH_DEBT("Tech. Debt"),
    ROADMAP("Roadmap"),
    SYNERGIES("Synergies"),
    MARCOPOLO("Marco Polo");

    private String id;

    Category(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static Category fromId(String id) {
        for (Category at : Category.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}