package com.choqnet.budget.scheduled;

import com.choqnet.budget.app.MainProcessBean;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

import javax.inject.Inject;

public class MainScheduledTask implements Job {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MainScheduledTask.class);
    @Inject
    MainProcessBean mpb;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // launches the main scheduled process
        mpb.mainProcess();
    }
}
