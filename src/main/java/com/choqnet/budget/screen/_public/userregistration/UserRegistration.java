package com.choqnet.budget.screen._public.userregistration;

import com.choqnet.budget.app.RegistrationService;
import com.choqnet.budget.entity.User;
import com.choqnet.budget.screen.login.LoginScreen;
import io.jmix.email.EmailException;
import io.jmix.email.EmailInfo;
import io.jmix.email.EmailInfoBuilder;
import io.jmix.email.Emailer;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@UiController("UserRegistration")
@UiDescriptor("user-registration.xml")
@Route(value = "register", root = true)
public class UserRegistration extends Screen {
    private static final Logger log = LoggerFactory.getLogger(UserRegistration.class);
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private TextField<String> emailField;
    @Autowired
    private TextField<String> firstNameField;
    @Autowired
    private TextField<String> lastNameField;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Form form;
    @Autowired
    private Button registerButton;
    @Autowired
    private ScreenValidation screenValidation;
    @Autowired
    private ComboBox<String> wishedRole;
    @Autowired
    private Emailer emailer;

    @Subscribe
    public void onInit(InitEvent event) {
        List<String> values = new ArrayList<>();
        values.add("Product Owner");
        values.add("Product Manager");
        values.add("Delivery Manager");
        wishedRole.setOptionsList(values);
        wishedRole.setValue("Product Owner");
    }

    @Subscribe("backToLogin")
    public void onBackToLoginClick(Button.ClickEvent event) {
        screenBuilders.screen(this)
                .withScreenClass(LoginScreen.class)
                .withOpenMode(OpenMode.ROOT)
                .build()
                .show();
    }

    @Subscribe("register")
    public void onRegister(Action.ActionPerformedEvent event) {
        if (!validateFields()) {
            return;
        }
        User user = registrationService.registerNewUser(emailField.getValue(), firstNameField.getValue(), lastNameField.getValue(), wishedRole.getValue());
        String activationToken = registrationService.generateRandomActivationToken();
        registrationService.saveActivationToken(user, activationToken);
        registrationService.sendActivationEmail(user);
        notifications.create(Notifications.NotificationType.HUMANIZED)
                .withDescription("User registered successfully. Check your email inbox.")
                .show();
        form.setEditable(false);
        registerButton.setEnabled(false);
        String body = String.format("Registration request from %s %s for the role of %s\n\n\n", firstNameField.getValue(), lastNameField.getValue(), wishedRole.getValue());
        try {
            EmailInfo email = EmailInfoBuilder.create()
                    .setSubject("User registration request: " + user.getUsername())
                    .setBody(body)
                    .setAddresses("olivier.choquet@ingenico.com,choqnet@gmail.com")
                    .build();

            emailer.sendEmail(email);
        } catch (EmailException e) {
            //e.printStackTrace();
            log.warn("Email problem");
            log.info("");
            log.info("Registration Information:");
            log.info("Login: " + user.getUsername());
            log.info("Message: " + body);
            log.info("");
        }
    }

    public boolean validateFields() {
        ValidationErrors validationErrors = screenValidation.validateUiComponents(form);
        if (!validationErrors.isEmpty()) {
            screenValidation.showValidationErrors(this, validationErrors);
            return false;
        }

        String email = emailField.getValue();
        String userName = firstNameField.getValue().toLowerCase().substring(0,1) + lastNameField.getValue().replace(" ", "").toLowerCase();
        if (registrationService.checkUserAlreadyExist(email, userName)) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withDescription("User with this email/userName already exists")
                    .withPosition(Notifications.Position.MIDDLE_CENTER)
                    .show();
            return false;
        }
        return true;
    }
}