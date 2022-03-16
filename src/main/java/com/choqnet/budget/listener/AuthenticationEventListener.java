package com.choqnet.budget.listener;

import com.choqnet.budget.entity.User;
import com.choqnet.budget.entity.UserAuthenticationLog;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.ClientDetails;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AuthenticationEventListener implements HttpSessionListener {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AuthenticationEventListener.class);
    @Autowired
    private UnconstrainedDataManager dataManager;

    @EventListener
    public void onInteractiveAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
        UserAuthenticationLog logItem = dataManager.create(UserAuthenticationLog.class);
        User user = (User) event.getAuthentication().getPrincipal();
        logItem.setUsername(user.getUsername());
        logItem.setLoggedIn(LocalDateTime.now());
        // Jmix provides session ID in ClientDetails class
        logItem.setSessionId(((ClientDetails) event.getAuthentication().getDetails()).getSessionId());
        dataManager.save(logItem);
    }

    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        // we use session ID to find matching login record
        String sessionId = null;
        try {
            sessionId = ((ClientDetails) event.getAuthentication().getDetails()).getSessionId();
        } catch (Exception e) {
            log.info("Harsh application quit: no authentication");
        }
        UserAuthenticationLog logItem = getLogOptional(sessionId)
                .orElseGet(() -> {
                    UserAuthenticationLog newLogItem = dataManager.create(UserAuthenticationLog.class);
                    User user = (User) event.getAuthentication().getPrincipal();
                    newLogItem.setUsername(user.getUsername());
                    return newLogItem;
                });

        if (logItem.getLoggedOut() == null) {
            logItem.setLoggedOut(LocalDateTime.now());
            dataManager.save(logItem);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // we use session ID to find matching login record
        getLogOptional(se.getSession().getId())
                .ifPresent(logItem -> {
                    if (logItem.getLoggedOut() == null) {
                        logItem.setLoggedOut(LocalDateTime.now());
                        dataManager.save(logItem);
                    }
                });
    }

    private Optional<UserAuthenticationLog> getLogOptional(String sessionId) {
        return dataManager.load(UserAuthenticationLog.class)
                .query("e.sessionId = ?1", sessionId)
                .optional();
    }
}