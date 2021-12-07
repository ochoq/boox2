package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum TShirt implements EnumClass<Integer> {

    XS(20),
    S(50),
    M(100),
    L(200),
    XL(300),
    XXL(400),
    XXXL(800),
    FREE(0);

    private Integer id;

    TShirt(Integer value) {
        this.id = value;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static TShirt fromId(Integer id) {
        for (TShirt at : TShirt.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}