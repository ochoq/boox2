package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum ProjectType implements EnumClass<String> {

    NEW("New Project"),
    ONGOING("Ongoing Project"),
    RUN("Non Project");

    private String id;

    ProjectType(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ProjectType fromId(String id) {
        for (ProjectType at : ProjectType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}