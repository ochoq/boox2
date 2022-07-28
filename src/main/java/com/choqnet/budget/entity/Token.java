package com.choqnet.budget.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "TOKEN")
@Entity
public class Token {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "MESSAGE")
    @Lob
    private String message;

    @Column(name = "TOKEN")
    private String token;

    @Column(name = "RECIPIENTS")
    private String recipients;

    @Column(name = "COUNTER")
    private Integer counter;

    public Integer getCounter() {
        return counter==null ? 0 : counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}