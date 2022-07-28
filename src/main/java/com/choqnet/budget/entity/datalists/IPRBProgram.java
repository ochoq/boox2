package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum IPRBProgram implements EnumClass<String> {

    DEF("Single Project"),
    ABO("ABO Migration"),
    FLO("Florida"),
    MPL("Marco Polo"),
    OMS("One MS"),
    POL("Polaris"),
    POS("Poseidon"),
    QUA("Quandrant II"),
    SEV("Severance"),
    SOC("Socrates"),
    TEA("Team"),
    UCR("UniCredit"),
    UNI("United"),
    VAL("Valhalla");

    private String id;

    IPRBProgram(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static IPRBProgram fromId(String id) {
        for (IPRBProgram at : IPRBProgram.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}