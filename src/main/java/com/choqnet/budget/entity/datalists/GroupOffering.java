package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum GroupOffering implements EnumClass<String> {

    ACC_ECOM("ACC - eCommerce"),
    ACC_GO("ACC - Global Online"),
    ACC_IS("ACC - In Store"),
    ACC_OCH("ACC - Omnichannel"),
    NA("value not in list");

    private String id;

    GroupOffering(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static GroupOffering fromId(String id) {
        for (GroupOffering at : GroupOffering.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}