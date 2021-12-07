package com.choqnet.budget.screen.control_tower;

import com.choqnet.budget.app.UMsgBean;
import com.choqnet.budget.communications.UserNotification;
import io.jmix.core.DataManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.TextArea;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("ControlTower")
@UiDescriptor("control-tower.xml")
public class ControlTower extends Screen {
    @Autowired
    private UMsgBean umb;
    @Autowired
    private TextArea<String> txtMessage;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DataManager dataManager;

    // *** communications


    // *** utilities
    @Subscribe("btnSendMessage")
    public void onBtnSendMessageClick(Button.ClickEvent event) {
        umb.publishEvent(new UserNotification(this, txtMessage.getValue()));
    }

    /*
    @EventListener
    private void getUMessage(UserNotification event) {
        notifications.create()
                .withDescription(event.getMessage())
                .withCaption("System Message")
                .withType(Notifications.NotificationType.SYSTEM)
                .show();
    }

    @EventListener
    private void received(UserNotification event) {
        notifications.create()
                .withCaption("System Communication")
                .withDescription(event.getMessage())
                .withType(Notifications.NotificationType.WARNING)
                .withPosition(Notifications.Position.TOP_CENTER)
                .show();
    }

     */




}