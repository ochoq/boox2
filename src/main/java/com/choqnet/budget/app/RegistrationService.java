package com.choqnet.budget.app;

import com.choqnet.budget.entity.User;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.email.EmailException;
import io.jmix.email.EmailInfo;
import io.jmix.email.EmailInfoBuilder;
import io.jmix.email.Emailer;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RegistrationService {
    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);
    @Autowired
    private UnconstrainedDataManager unconstrainedDataManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private Emailer emailer;

    /**
     * @return true if user with this email (or login) already exists.
     */
    public boolean checkUserAlreadyExist(String email) {
        List<User> users = unconstrainedDataManager.load(User.class)
                .query("select e from User e where e.username = :email or e.email = :email")
                .parameter("email", email)
                .list();
        return !users.isEmpty();
    }

    public User registerNewUser(String email, String firstName, String lastName, String wishedRole) {
        User user = unconstrainedDataManager.create(User.class);
        user.setEmail(email);
        String userName = firstName.toLowerCase().substring(0,1) + lastName.replace(" ", "").toLowerCase();
        user.setUsername(userName);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRequestedRole(getShortRole(wishedRole));
        user.setNeedsActivation(true);
        user.setActive(false);
        User savedUser = unconstrainedDataManager.save(user);
        return savedUser;
    }

    private String getShortRole(String fullRole) {
        switch (fullRole) {
            case "Product Manager":
                return "PM";
            case "Delivery Manager":
                return "TM";
            case "Product Owner":
            default:
                return "PO";
        }
    }

    public String generateRandomActivationToken() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        ThreadLocalRandom current = ThreadLocalRandom.current();
        String generatedString = current.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return generatedString;
    }

    public void saveActivationToken(User user, String activationToken) {
        User freshUser = unconstrainedDataManager.load(User.class)
                .id(user.getId())
                .one();
        freshUser.setActivationToken(activationToken);
        unconstrainedDataManager.save(freshUser);
    }

    public void sendActivationEmail(User user) {
        user = unconstrainedDataManager.load(User.class)
                .id(user.getId())
                .one();

        String activationLink = "https://80.78.10.84/boox/#activate?token=" + user.getActivationToken();
        //String activationLink = "http://localhost:8080/#activate?token=" + user.getActivationToken();
        String body = String.format("Hello, %s %s.\nYour Boox activation link is: %s\nClick it to finish your registration.\nYour id is %s\n\nYou can find more information here: %s\n\n\n",
                user.getFirstName(),
                user.getLastName(),
                activationLink,
                user.getUsername(),
                "http://e.pc.cd/4w9otalK"
        );
        try {
            EmailInfo email = EmailInfoBuilder.create()
                    .setSubject("<<==-- Boox registration --==>>")
                    .setBody(body)
                    .setAddresses(user.getEmail())
                    .build();

            emailer.sendEmail(email);
        } catch (EmailException e) {
            //e.printStackTrace();
            log.info("");
            log.warn("Email problem");
            log.info("Activation Information:");
            log.info("Login: " + user.getUsername());
            log.info("Link: " + activationLink);
            log.info("");
        }
    }

    @Nullable
    public User loadUserByActivationToken(String token) {
        return unconstrainedDataManager.load(User.class)
                .query("select u from User u where u.activationToken = :token and u.needsActivation = true")
                .parameter("token", token)
                .optional()
                .orElse(null);
    }

    public void activateUser(User user, String password) {
        user = unconstrainedDataManager.load(User.class)
                .id(user.getId())
                .one();
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        user.setActivationToken(null);
        user.setNeedsActivation(false);
        List<Object> toSave = new ArrayList<>();
        toSave.add(user);
        toSave.add(createRoleAssignment(user, user.getRequestedRole(), RoleAssignmentRoleType.RESOURCE));
        unconstrainedDataManager.save(toSave.toArray());
        // alert admin
        String body = String.format("The user %s %s is activated with the role %s\n\n\n", user.getFirstName(), user.getLastName(), user.getRequestedRole());
        try {
            EmailInfo email = EmailInfoBuilder.create()
                    .setSubject("User activation: " + user.getUsername())
                    .setBody(body)
                    .setAddresses("olivier.choquet@ingenico.com,choqnet@gmail.com")
                    .build();
            emailer.sendEmail(email);
        } catch (EmailException e) {
            //e.printStackTrace();
            log.warn("Email problem");
            log.info("");
            log.info("Activation Information:");
            log.info("Login: " + user.getUsername());
            log.info("Message: " + body);
            log.info("");
        }
    }

    private RoleAssignmentEntity createRoleAssignment(User user, String roleCode, String roleType) {
        RoleAssignmentEntity roleAssignmentEntity = unconstrainedDataManager.create(RoleAssignmentEntity.class);
        roleAssignmentEntity.setRoleCode(roleCode);
        roleAssignmentEntity.setUsername(user.getUsername());
        roleAssignmentEntity.setRoleType(roleType);
        return roleAssignmentEntity;
    }

}