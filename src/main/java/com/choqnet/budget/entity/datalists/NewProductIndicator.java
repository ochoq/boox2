package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum NewProductIndicator implements EnumClass<String> {

    N("N"),
    NE("NE"),
    E("E"),
    NA("NA");

    private String id;

    NewProductIndicator(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static NewProductIndicator fromId(String id) {
        for (NewProductIndicator at : NewProductIndicator.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}