package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum Source implements EnumClass<String> {

    IJ("Techno JIRA"),
    MN("Manual");

    private String id;

    Source(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static Source fromId(String id) {
        for (Source at : Source.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}