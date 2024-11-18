package ru.example.emailparser.scheduler;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class JobScheduler {

    public static void scheduleImapJob(String email, String login, String password) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(ImapJobQuartzWrapper.class)
                    .withIdentity("imapJob", "group1")
                    .build();
            jobDetail.getJobDataMap().put("email", email);
            jobDetail.getJobDataMap().put("login", login);
            jobDetail.getJobDataMap().put("password", password);

            // Настраиваем триггер с cron-выражением для запуска каждые 10 минут
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("imapTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ?"))
                    .build();
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(jobDetail, trigger);

            System.out.println("Job 1 - IMAP успешно запланирована!");

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
