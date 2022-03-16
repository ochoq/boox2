package com.choqnet.budget.entity;

import com.choqnet.budget.entity.datalists.Operation;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@JmixEntity
@Table(name = "COMMAND")
@Entity
public class Command {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "OPERATION")
    private String operation;

    @Column(name = "VALUE_")
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Operation getOperation() {
        return operation == null ? null : Operation.fromId(operation);
    }

    public void setOperation(Operation operation) {
        this.operation = operation == null ? null : operation.getId();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}