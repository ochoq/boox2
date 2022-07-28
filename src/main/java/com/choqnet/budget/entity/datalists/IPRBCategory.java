package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum IPRBCategory implements EnumClass<String> {

    ENV("Envelope"),
    LOC("Local"),
    GLO("Global"),
    ARR("Arrow");

    private String id;

    IPRBCategory(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static IPRBCategory fromId(String id) {
        for (IPRBCategory at : IPRBCategory.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}