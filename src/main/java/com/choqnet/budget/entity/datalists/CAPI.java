package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum CAPI implements EnumClass<String> {

    C100("C - 100%"),
    C90("C - 90%"),
    C80("C - 80%"),
    C70("C - 70%"),
    C60("C - 60%"),
    C50("C - 50%"),
    C40("C - 40%"),
    C30("C - 30%"),
    C20("C - 20%"),
    C10("C - 10%"),
    C00("C - 00%");

    private String id;

    CAPI(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static CAPI fromId(String id) {
        for (CAPI at : CAPI.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

}