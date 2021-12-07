package com.choqnet.budget.app;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class MainProcessBean {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MainProcessBean.class);

    public void mainProcess() {
        log.info("Main process starting....");
    }
}