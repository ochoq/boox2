package com.choqnet.budget.communications;

import org.springframework.context.ApplicationEvent;

public class UserNotification extends ApplicationEvent {
    private String message;

    public String getMessage() {
        return message==null ? "" : message;
    }

    public UserNotification(Object source, String message) {
        super(source);
        this.message = message;
    }
}
