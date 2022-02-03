package com.choqnet.budget.app;

import com.choqnet.budget.entity.JTeam;
import com.choqnet.budget.entity.Team;
import com.choqnet.budget.entity.Token;
import com.choqnet.budget.entity.datalists.Source;
import com.google.gson.Gson;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.email.EmailException;
import io.jmix.email.EmailInfo;
import io.jmix.email.EmailInfoBuilder;
import io.jmix.email.Emailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
public class MainProcessBean {
    private static final Logger log = LoggerFactory.getLogger(MainProcessBean.class);
    private String token;
    private Gson gson = new Gson();
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Emailer emailer;

    public void mainProcess() {
        log.info("Main process starting....");
    }





    // *** JIRA functions
    private String getToken() {
        if (token==null) {
            // reads the token
            try {
                token = dataManager.load(Token.class).query("select e from Token e").one().getToken();
            } catch (Exception e) {
                log.error("MPB Error when getting token");
            }
        }
        return token;
    }

    public void getJiraTeams() {
        String msg = "";
        List<Team> teams = dataManager.load(Team.class).query("select e from Team e").list();
        String jiraReply = getJSON("https://jira.techno.ingenico.com/rest/tempo-teams/2/team/");
        JTeam[] jTeams = gson.fromJson(jiraReply, JTeam[].class);
        SaveContext saveContext = new SaveContext();
        for (JTeam jTeam: jTeams) {
            Optional<Team> target = teams.stream().filter(e -> jTeam.getId().toString().equals(e.getSourceID())).findFirst();
            Team cTeam;
            if (target.isPresent()) {
                cTeam = target.get();
            } else {
                cTeam = dataManager.create(Team.class);
                cTeam.setSourceID(jTeam.getId().toString());
                cTeam.setName("NEW " + jTeam.getId());
                cTeam.setFullName("NEW " + jTeam.getId());
                cTeam.setSelectable(false);
                cTeam.setEnabled(true);
                msg = msg + "\nNew team created [" + jTeam.getId() + "] - " + jTeam.getName();
            }
            cTeam.setSourceName(jTeam.getName());
            cTeam.setSource(Source.IJ);
            saveContext.saving(cTeam);
        }
        dataManager.save(saveContext);
        if (!"".equals(msg)) {
            EmailInfo emailInfo = EmailInfoBuilder.create("olivier.choquet@ingenico.com","Boox : New team detection", msg)
                    .build();
            try {
                emailer.sendEmail(emailInfo);
            } catch (EmailException e) {
                log.info("MPB Error sending mail " + e.getMessage());
            }
        }

    }

    private String getJSON(String url) {
        String answer = null;
        URL jiraURL;
        try {
            jiraURL = new URL(url);
            HttpURLConnection connectionIssue = (HttpURLConnection) jiraURL.openConnection();
            connectionIssue.setRequestMethod("GET");
            connectionIssue.setRequestProperty("Accept", "*/*");
            connectionIssue.setRequestProperty("Content-Type", "application/json");
            connectionIssue.setDoOutput(false);
            connectionIssue.setDoInput(true);
            // connection with token
            connectionIssue.setRequestProperty("Authorization", "Bearer " + getToken());
            connectionIssue.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(connectionIssue.getInputStream(), StandardCharsets.UTF_8));
            answer = in.readLine();
            in.close();
            connectionIssue.disconnect();
        } catch (Exception e) {
            log.error("MPB Error when trying to read Jira: " + url);
        }
        return answer;
    }

}