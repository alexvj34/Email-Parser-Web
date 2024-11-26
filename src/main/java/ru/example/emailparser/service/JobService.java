package ru.example.emailparser.service;

import org.springframework.stereotype.Service;
import ru.example.emailparser.parser.ImapJob;
import ru.example.emailparser.parser.SmtpJob;
import ru.example.emailparser.scheduler.JobScheduler;

@Service
public class JobService {

    public void startImapJob(String email, String login, String password) {
        JobScheduler.scheduleImapJob(email, login, password);
        ImapJob imapJob = new ImapJob(email, login, password);
        new Thread(imapJob).start();
    }

    public void startSmtpJob(String fileName, String recipient, String subject, String content) {
        SmtpJob smtpJob = new SmtpJob(fileName, recipient, subject, content);
        new Thread(smtpJob).start();
    }

    public String viewLog() {
        return "Логи отправленных писем: ...";  // Допилить реальную логику
    }
}
