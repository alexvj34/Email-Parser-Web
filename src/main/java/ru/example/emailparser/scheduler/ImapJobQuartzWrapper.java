package ru.example.emailparser.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import ru.example.emailparser.parser.ImapJob;

public class ImapJobQuartzWrapper implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Получаем данные из JobDataMap
        String email = context.getJobDetail().getJobDataMap().getString("email");
        String login = context.getJobDetail().getJobDataMap().getString("login");
        String password = context.getJobDetail().getJobDataMap().getString("password");

        // Запускаем ImapJob
        ImapJob imapJob = new ImapJob(email, login, password);
        imapJob.run();
    }
}
