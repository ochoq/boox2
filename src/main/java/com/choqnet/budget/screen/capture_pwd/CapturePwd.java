package com.choqnet.budget.screen.capture_pwd;

import com.choqnet.budget.app.RegistrationService;
import com.choqnet.budget.entity.User;
import com.choqnet.budget.screen.login.LoginScreen;
import com.choqnet.budget.screen.main.MainScreen;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinServletResponse;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.security.SystemAuthenticationToken;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.*;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.navigation.UrlParamsChangedEvent;
import io.jmix.ui.screen.*;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@UiController("CapturePwd")
@UiDescriptor("capture-pwd.xml")
@Route(path = "pwdreset", root = true)
public class CapturePwd extends Screen {
    private static final Logger log = LoggerFactory.getLogger(CapturePwd.class);
    private User user;
    private boolean initialized = false;

    @Autowired
    protected AuthenticationManager authenticationManager;
    @Autowired
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy;


    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private Label welcomeLabel;
    @Autowired
    private Form form;
    @Autowired
    private Button activateButton;
    @Autowired
    private Label notFoundLabel;
    @Autowired
    private Label lblInfo;
    @Autowired
    private LinkButton returnToLoginScreen;
    @Autowired
    private MessageBundle messageBundle;
    @Autowired
    private PasswordField passwordField;
    @Autowired
    private ScreenValidation screenValidation;
    @Autowired
    private ScreenBuilders screenBuilders;

    @Subscribe
    public void onUrlParamsChanged(UrlParamsChangedEvent event) {
        String receivedPwdResetToken = event.getParams().get("token");
        log.info("Got it");
        if (StringUtils.isNotEmpty(receivedPwdResetToken)) {
            user = registrationService.loadUserByPwdResetToken(receivedPwdResetToken);
        } else {
            user = null;
        }

        updateUI();

    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (!initialized) {
            updateUI();
        }
    }

    private void updateUI() {
        boolean success = user != null;
        welcomeLabel.setVisible(success);
        form.setVisible(success);
        activateButton.setVisible(success);
        notFoundLabel.setVisible(!success);
        lblInfo.setVisible(success);
        returnToLoginScreen.setVisible(!success);
        if (user != null) {
            welcomeLabel.setValue(messageBundle.formatMessage("resetPassword", user.getFirstName(), user.getLastName()));
            lblInfo.setValue("Your login is " + user.getUsername());
        }
        initialized = true;
    }

    @Subscribe("activateButton")
    public void onActivateButtonClick(Button.ClickEvent event) {
        if (!validateFields()) {
            return;
        }
        String password = passwordField.getValue();
        registrationService.recordPassword(user, password);
        //loginByPassword(password);
        loginAsTrusted();
    }

    public boolean validateFields() {
        ValidationErrors validationErrors = screenValidation.validateUiComponents(form);
        if (!validationErrors.isEmpty()) {
            screenValidation.showValidationErrors(this, validationErrors);
            return false;
        }
        return true;
    }
    // mostly copied from io.jmix.securityui.authentication.LoginScreenSupport
    private void loginAsTrusted() {
        log.info("Login without password");
        SystemAuthenticationToken token = new SystemAuthenticationToken(user.getUsername());
        Authentication authentication = authenticationManager.authenticate(token);
        VaadinServletRequest request = VaadinServletRequest.getCurrent();
        VaadinServletResponse response = VaadinServletResponse.getCurrent();
        sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
        SecurityContextHelper.setAuthentication(authentication);
        screenBuilders.screen(this)
                .withScreenClass(MainScreen.class)
                .withOpenMode(OpenMode.ROOT)
                .build()
                .show();
    }

    @Subscribe("returnToLoginScreen")
    public void onReturnToLoginScreenClick(Button.ClickEvent event) {
        screenBuilders.screen(this)
                .withScreenClass(LoginScreen.class)
                .withOpenMode(OpenMode.ROOT)
                .build()
                .show();
    }
}