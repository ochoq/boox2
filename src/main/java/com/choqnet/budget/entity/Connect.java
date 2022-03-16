package com.choqnet.budget.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

@JmixEntity(name = "Connect_")
public class Connect {

    // class used to get the token sent by Dumbo

    private String access_token;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

}