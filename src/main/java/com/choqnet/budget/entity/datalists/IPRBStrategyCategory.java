package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum IPRBStrategyCategory implements EnumClass<String> {

    ECM("Boost eCom"),
    BCO("Business Continuity"),
    CST("Customer"),
    EXP("Expansion"),
    DIG("Digitalization"),
    INN("Innovation"),
    OCH("OmniChannel"),
    OPX("Operational Excellence"),
    VRT("Verticalization");

    private String id;

    IPRBStrategyCategory(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static IPRBStrategyCategory fromId(String id) {
        for (IPRBStrategyCategory at : IPRBStrategyCategory.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}