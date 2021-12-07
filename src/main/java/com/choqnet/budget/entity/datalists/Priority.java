package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum Priority implements EnumClass<String> {

    P1("P1"),
    P2("P2"),
    P3("P3"),
    P4("P4"),
    P5("P5");

    private String id;

    Priority(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static Priority fromId(String id) {
        for (Priority at : Priority.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

    public boolean isLessOrEqual(Priority pThreshold) {
        return this.id.compareTo(pThreshold.getId())<=0;
    }
}