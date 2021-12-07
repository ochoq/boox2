package com.choqnet.budget.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "USER_AUTHENTICATION_LOG")
@Entity
public class UserAuthenticationLog {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "LOGGED_IN")
    private LocalDateTime loggedIn;

    @Column(name = "LOGGED_OUT")
    private LocalDateTime loggedOut;

    @Column(name = "SESSION_ID")
    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getLoggedOut() {
        return loggedOut;
    }

    public void setLoggedOut(LocalDateTime loggedOut) {
        this.loggedOut = loggedOut;
    }

    public LocalDateTime getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(LocalDateTime loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}