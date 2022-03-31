package com.choqnet.budget.scheduled;

import com.choqnet.budget.app.MainProcessBean;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class ScheduledJob implements Job {
    @Autowired
    MainProcessBean mpb;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        mpb.mainProcess();
    }
}
