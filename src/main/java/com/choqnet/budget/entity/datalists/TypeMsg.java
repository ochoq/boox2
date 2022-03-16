package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum TypeMsg implements EnumClass<String> {

    ERROR("Error"),
    WARNING("Warning"),
    INFO("Info"),
    DEBUG("Debug");

    private String id;

    TypeMsg(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static TypeMsg fromId(String id) {
        for (TypeMsg at : TypeMsg.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}