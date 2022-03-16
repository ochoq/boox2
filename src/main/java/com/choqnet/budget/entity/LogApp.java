package com.choqnet.budget.entity;

import com.choqnet.budget.entity.datalists.TypeMsg;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "LOG_APP")
@Entity
public class LogApp {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "CONTEXT")
    private String context;

    @Column(name = "TYPE_MSG")
    private String typeMsg;

    @Column(name = "MESSAGE")
    private String message;

    @Lob
    @Column(name = "VALUES_")
    private String values;

    @Column(name = "TIME_STAMP")
    private LocalDateTime timeStamp;

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TypeMsg getTypeMsg() {
        return typeMsg == null ? null : TypeMsg.fromId(typeMsg);
    }

    public void setTypeMsg(TypeMsg typeMsg) {
        this.typeMsg = typeMsg == null ? null : typeMsg.getId();
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}