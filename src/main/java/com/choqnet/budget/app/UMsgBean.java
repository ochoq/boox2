package com.choqnet.budget.app;

import com.vaadin.server.VaadinSession;
import io.jmix.ui.App;
import io.jmix.ui.AppUI;
import io.jmix.ui.UiEventPublisher;
import io.jmix.ui.event.AppInitializedEvent;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class UMsgBean {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UMsgBean.class);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<WeakReference<VaadinSession>> sessions = new ArrayList<>();

    @Autowired
    private UiEventPublisher uiEventPublisher;

    @EventListener
    public void onAppStart(AppInitializedEvent event) {
        lock.writeLock().lock();
        try {
            sessions.add(new WeakReference<>(VaadinSession.getCurrent()));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void publishEvent(ApplicationEvent event) {
        ArrayList<VaadinSession> activeSessions = new ArrayList<>();
        int removed = 0;
        lock.readLock().lock();

        try {
            for (Iterator<WeakReference<VaadinSession>> iterator = sessions.iterator(); iterator.hasNext(); ) {
                WeakReference<VaadinSession> reference = iterator.next();
                VaadinSession session = reference.get();
                if (session != null) {
                    activeSessions.add(session);
                } else {
                    lock.readLock().unlock();
                    lock.writeLock().lock();
                    try {
                        iterator.remove();
                        lock.readLock().lock();
                    } finally {
                        lock.writeLock().unlock();
                    }
                    removed++;
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        if (removed > 0) {
            log.debug("Removed {} Vaadin sessions", removed);
        }
        log.debug("Sending {} to {} Vaadin sessions", event, activeSessions.size());

        for (VaadinSession session : activeSessions) {
            // obtain lock on session state
            session.access(() -> {
                if (session.getState() == VaadinSession.State.OPEN) {
                    // active app in this session
                    App app = App.getInstance();

                    // notify all opened web browser tabs
                    List<AppUI> appUIs = app.getAppUIs();
                    for (AppUI ui : appUIs) {
                        if (!ui.isClosing()) {
                            // work in context of UI
                            ui.accessSynchronously(() -> {
                                ui.getUiEventsMulticaster().multicastEvent(event);
                            });
                        }
                    }
                }
            });
        }
    }
}