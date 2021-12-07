package com.choqnet.budget.entity.datalists;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;


public enum LegalEntity implements EnumClass<String> {

    WLBEL("WL - BEL"),
    WLFRA("WL - FRA"),
    WLGER("WL - GER"),
    WLNLD("WL - NLD"),
    WLLUX("WL - LUX"),
    WLUK("WL - UK"),
    WLAUS("WL - AUS"),
    WLBRA("WL - BRA"),
    WLSWE("WL - SWE"),
    SPSCHE("SPS - CHE"),
    SPSGER("SPS - GER"),
    SPSAUT("SPS - AUT"),
    SPSLUX("SPS - LUX"),
    SPSPOL("SPS - POL"),
    WLOCBEL("WLOC - BEL"),
    SaferpayCHE("Saferpay - CHE"),
    SIPSFRA("SIPS - FRA"),
    BOLSWE("BOL - SWE"),
    BeanstreamRoamNAR("Beanstream Roam - NAR"),
    IPPAUS("IPP - AUS"),
    PayOneGER("PayOne - GER"),
    PayUnityGER("PayUnity - GER"),
    CardLinkGRC("CardLink - GRC"),
    Sub1ARG("Sub1 - ARG"),
    GoPayCZE("GoPay - CZE"),
    AxisFRA("Axis - FRA"),
    SACCFRA("SACC - FRA"),
    IndiaTPIND("India TP - IND"),
    AxxeptaITA("Axxepta - ITA"),
    GlobalCollectNLD("Global Collect - NLD"),
    ConnexflowSPA("Connexflow - SPA"),
    CSACTSPA("CSACT - SPA"),
    BBSamportSWE("BB Samport - SWE"),
    DevCodeSWE("DevCode - SWE"),
    OCHSWE("OCH - SWE"),
    WOPASWE("WOPA - SWE"),
    SharedServicesAll("Shared Services - All"),
    PSQNLD("PSQ - NLD"),
    PSQGER("PSQ - GER"),
    PSQPOL("PSQ - POL"),
    Cataps("Cataps"),
    NonMS("Non MS"),
    GTMFI("GTM FI");

    private String id;

    // *** Id uses "_" to replace spaces and "dash" to replace "-", but when it is sent to Excel it must be replaced by Id with spaces
    // getClearId() returns this Id cleared (ex: WL_dash_BEL will be returned as WL - BEL)
    // fromClearId() works like fromID but accepts a clear Id as argument instead of a coded Id
    // fromId("WL_dash_BEL") --> "Wordline SA/NV"
    // fromClearId("WL - BEL") --> "Wordline SA/NV"

    LegalEntity(String value) {
        this.id = value;
    }


    public String getId() {
        return id;
    }

    @Nullable
    public static LegalEntity fromId(String id) {
        for (LegalEntity at : LegalEntity.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}