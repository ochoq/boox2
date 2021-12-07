package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum StrategicProgram implements EnumClass<String> {

    UNI("United"),
    M_P("Marco Polo"),
    SPR("Single Project"),
    RUN("Run");

    private String id;

    StrategicProgram(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static StrategicProgram fromId(String id) {
        for (StrategicProgram at : StrategicProgram.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}