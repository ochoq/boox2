package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum GTM implements EnumClass<String> {

    GSV("GSV"),
    DC("Digital Commerce"),
    RB("Regional Business"),
    OCH("OmniChannel"),
    OTH("Other");

    private String id;

    GTM(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static GTM fromId(String id) {
        for (GTM at : GTM.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}