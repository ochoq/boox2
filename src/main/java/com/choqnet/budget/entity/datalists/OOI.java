package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum OOI implements EnumClass<String> {

    O100("O - 100%"),
    O90("O - 90%"),
    O80("O - 80%%"),
    O70("O - 70%"),
    O60("O - 60%"),
    O50("O - 50%"),
    O40("O - 40%"),
    O30("O - 30%"),
    O20("O - 20%"),
    O10("O - 10%"),
    O00("O - 00%");

    private String id;

    OOI(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static OOI fromId(String id) {
        for (OOI at : OOI.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

    public String OOICode(Integer id) {
        return "O - " + id + "%";
    }
}