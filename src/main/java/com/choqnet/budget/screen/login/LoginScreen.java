package com.choqnet.budget.screen.login;

import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.annotation.DateTimeFormat;
import io.jmix.email.EmailException;
import io.jmix.email.EmailInfo;
import io.jmix.email.EmailInfoBuilder;
import io.jmix.email.Emailer;
import io.jmix.securityui.authentication.AuthDetails;
import io.jmix.securityui.authentication.LoginScreenSupport;
import io.jmix.ui.JmixApp;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import io.jmix.ui.security.UiLoginProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@UiController("LoginScreen")
@UiDescriptor("login-screen.xml")
@Route(path = "login", root = true)
public class LoginScreen extends Screen {

    @Autowired
    private TextField<String> usernameField;

    @Autowired
    private PasswordField passwordField;

    @Autowired
    private CheckBox rememberMeCheckBox;

    @Autowired
    private ComboBox<Locale> localesField;

    @Autowired
    private Notifications notifications;

    @Autowired
    private Messages messages;

    @Autowired
    private MessageTools messageTools;

    @Autowired
    private LoginScreenSupport loginScreenSupport;

    @Autowired
    private UiLoginProperties loginProperties;

    @Autowired
    private JmixApp app;

    private final Logger log = LoggerFactory.getLogger(LoginScreen.class);
    @Autowired
    private VBoxLayout loginWrapper;
    @Autowired
    private VBoxLayout registerForm;
    @Autowired
    private VBoxLayout loginMainBox;
    @Autowired
    private RadioButtonGroup<String> rdgRole;
    @Autowired
    private TextField<String> txtFirstName;
    @Autowired
    private TextField<String> txtLastName;
    @Autowired
    private TextField<String> txtEmail;
    @Autowired
    private Emailer emailer;
    @Autowired
    private VBoxLayout congratsPanel;
    @Autowired
    private TextArea<String> txtCongrats;
    @Autowired
    private TextArea txtComment;

    @Subscribe
    private void onInit(InitEvent event) {
        usernameField.focus();
        initLocalesField();
        // initDefaultCredentials();
        // sets the role's radioButtonGroup's items
        List<String> list = new ArrayList<>();
        list.add("Viewer");
        list.add("Product Owner");
        list.add("Product Manager");
        list.add("Delivery Manager");
        rdgRole.setOptionsList(list);
        rdgRole.setValue("Viewer");
    }

    private void initLocalesField() {
        localesField.setOptionsMap(messageTools.getAvailableLocalesMap());
        localesField.setValue(app.getLocale());
        localesField.addValueChangeListener(this::onLocalesFieldValueChangeEvent);
    }

    private void onLocalesFieldValueChangeEvent(HasValue.ValueChangeEvent<Locale> event) {
        //noinspection ConstantConditions
        app.setLocale(event.getValue());
        UiControllerUtils.getScreenContext(this).getScreens()
                .create(this.getClass(), OpenMode.ROOT)
                .show();
    }

    private void initDefaultCredentials() {
        String defaultUsername = loginProperties.getDefaultUsername();
        if (!StringUtils.isBlank(defaultUsername) && !"<disabled>".equals(defaultUsername)) {
            usernameField.setValue(defaultUsername);
        } else {
            usernameField.setValue("");
        }

        String defaultPassword = loginProperties.getDefaultPassword();
        if (!StringUtils.isBlank(defaultPassword) && !"<disabled>".equals(defaultPassword)) {
            passwordField.setValue(defaultPassword);
        } else {
            passwordField.setValue("");
        }
    }

    @Subscribe("submit")
    private void onSubmitActionPerformed(Action.ActionPerformedEvent event) {
        login();
    }

    private void login() {
        String username = usernameField.getValue();
        String password = passwordField.getValue();

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage(getClass(), "emptyUsernameOrPassword"))
                    .show();
            return;
        }

        try {
            loginScreenSupport.authenticate(
                    AuthDetails.of(username, password)
                            .withLocale(localesField.getValue())
                            .withRememberMe(rememberMeCheckBox.isChecked()), this);
        } catch (BadCredentialsException | DisabledException | LockedException e) {
            log.info("Login failed", e);
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(messages.getMessage(getClass(), "loginFailed"))
                    .withDescription(messages.getMessage(getClass(), "badCredentials"))
                    .show();
        }
    }



    // *** Custom Part
    @Subscribe("lnkRegister")
    public void onLnkRegisterClick(Button.ClickEvent event) {
        loginMainBox.setVisible(false);
        registerForm.setVisible(true);
    }

    @Subscribe("btnCancel")
    public void onBtnCancelClick(Button.ClickEvent event) {
        loginMainBox.setVisible(true);
        registerForm.setVisible(false);
    }

    @Subscribe("btnRegister")
    public void onBtnRegisterClick(Button.ClickEvent event) {
        try {
            txtFirstName.validate();
            txtLastName.validate();
            txtEmail.validate();
        } catch (ValidationException e) {
            return;
        }

        if (txtFirstName.isValid() && txtLastName.isValid() && txtEmail.isValid()) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:MM");
            String msg = "Boox Registration request from:\n\n"+ txtFirstName.getValue() + "\n" + txtLastName.getValue() + "\n" + txtEmail.getValue() + "\n" + rdgRole.getValue() + "\n\nComment:\n" + txtComment.getValue() + "\n\n" + LocalDateTime.now().format(dtf);
            EmailInfo emailInfo = EmailInfoBuilder.create("choqnet@gmail.com,ochoq@outlook.com,olivier.choquet@ingenico.com","Boox : Account creation request", msg)
                    .build();
            try {
                emailer.sendEmail(emailInfo);
                registerForm.setVisible(false);
                congratsPanel.setVisible(true);
                txtCongrats.setValue("Your request is sent and will be managed shortly.\n\nThanks for you interest in Boox.");
            } catch (EmailException e) {
                notifications.create()
                        .withDescription("Error while sending the request")
                        .withType(Notifications.NotificationType.ERROR)
                        .show();
            }
        }

    }



}
