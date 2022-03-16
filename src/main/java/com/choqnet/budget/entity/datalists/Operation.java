package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum Operation implements EnumClass<String> {

    TEMPO("Get Worklogs in Tempo");

    private String id;

    Operation(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static Operation fromId(String id) {
        for (Operation at : Operation.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}