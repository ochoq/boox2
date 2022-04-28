package com.choqnet.budget.screen._public.reset_password;

import com.choqnet.budget.app.RegistrationService;
import com.choqnet.budget.entity.User;
import com.choqnet.budget.screen.login.LoginScreen;
import io.jmix.core.DataManager;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.email.EmailException;
import io.jmix.email.EmailInfo;
import io.jmix.email.EmailInfoBuilder;
import io.jmix.email.Emailer;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Form;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("ResetPassword")
@UiDescriptor("reset-password.xml")
@Route(value = "resetPWD", root = true)
public class ResetPassword extends Screen {
    private static final Logger log = LoggerFactory.getLogger(ResetPassword.class);
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private ScreenValidation screenValidation;
    @Autowired
    private Form form;
    @Autowired
    private TextField<String> emailField;
    @Autowired
    private TextField<String> userNameField;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Emailer emailer;
    @Autowired
    private UnconstrainedDataManager unconstrainedDataManager;

    @Subscribe("backToLogin")
    public void onBackToLoginClick(Button.ClickEvent event) {
        screenBuilders.screen(this)
                .withScreenClass(LoginScreen.class)
                .withOpenMode(OpenMode.ROOT)
                .build()
                .show();
    }

    @Subscribe("resetPasswordButton")
    public void onResetPasswordButtonClick(Button.ClickEvent event) {
        if (!validateFields()) {
            return;
        }
        User user = registrationService.getUserByUserName(userNameField.getValue());
        if (user == null) {
            notifications.create()
                    .withType(Notifications.NotificationType.ERROR)
                    .withDescription("A problem occurred, please contact your administrator\nSorry for the inconvenience")
                    .show();
            return;
        }
        String pwdReset = registrationService.generateRandomActivationToken();
        registrationService.savePwdResetToken(user, pwdReset);
        registrationService.sendPwdResetEmail(user);
        notifications.create()
                .withDescription("A link is sent to reset your password, check your inbox.")
                .show();
        form.setEditable(false);
        String body = String.format("Password reset request sent to %s \n\n\n", userNameField.getValue());
        try {
            EmailInfo email = EmailInfoBuilder.create()
                    .setSubject("Password reset for " + user.getUsername())
                    .setBody(body)
                    .setAddresses("olivier.choquet@ingenico.com,choqnet@gmail.com")
                    .build();
            emailer.sendEmail(email);
        } catch (EmailException e) {
            //e.printStackTrace();
            log.warn("Email problem");
            log.info("");
            log.info("Password Reset Information:");
            log.info("user: " + user.getUsername());
            log.info("message: " + body);
            log.info("");
        }
    }

    private boolean validateFields() {
        ValidationErrors validationErrors = screenValidation.validateUiComponents(form);
        if (!validationErrors.isEmpty()) {
            screenValidation.showValidationErrors(this, validationErrors);
            return false;
        }

        String email = emailField.getValue().toLowerCase();
        String userName = userNameField.getValue().toLowerCase();
        if (!registrationService.checkUserAlreadyExist(email, userName)) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withDescription("Ooops, this user is unknown, please check your input.")
                    .withPosition(Notifications.Position.MIDDLE_CENTER)
                    .show();
            return false;
        }
        return true;
    }


}