package ru.example.emailparser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.example.emailparser.database.DatabaseManager;
import ru.example.emailparser.service.DatabaseService;
import ru.example.emailparser.service.JobService;

@RestController
@RequestMapping("/email-parser")
public class EmailParserController {

    private final DatabaseService databaseService;
    private final JobService jobService;

    @Autowired
    public EmailParserController(DatabaseService databaseService, JobService jobService) {
        this.databaseService = databaseService;
        this.jobService = jobService;
    }

    @GetMapping("/help")
    public String[] getHelp() {
        return new String[] {
                "help - Показать доступные команды",
                "select-job - Выбрать тип задачи (IMAP или SMTP)",
                "listTables - Показать список таблиц в базе данных",
                "listRecords - Показать записи из всех таблиц",
                "viewLog - Просмотреть лог отправленных писем",
                "info - Получить информацию о приложении",
                "exit - Выйти из приложения"
        };
    }

    @GetMapping("/listTables")
    public ResponseEntity<String> listTables() {
        DatabaseManager dbManager = new DatabaseManager();
        String result = dbManager.listTables();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/listRecords")
    public String listRecords() {
        return databaseService.listRecords();
    }

    @GetMapping("/viewLog")
    public String viewLog() {
        return jobService.viewLog();
    }

    @GetMapping("/info")
    public String getInfo() {
        return "Email Parser v1.0 - Приложение для обработки и отправки писем с вложениями.";
    }

    @PostMapping("/selectJob")
    public String selectJob(@RequestParam("type") String type,
                            @RequestParam(value = "email", required = false) String email,
                            @RequestParam(value = "login", required = false) String login,
                            @RequestParam(value = "password", required = false) String password,
                            @RequestParam(value = "fileName", required = false) String fileName,
                            @RequestParam(value = "recipient", required = false) String recipient,
                            @RequestParam(value = "subject", required = false) String subject,
                            @RequestParam(value = "content", required = false) String content) {

        if ("imap".equalsIgnoreCase(type)) {
            if (email == null || login == null || password == null) {
                return "Неполные данные для IMAP задачи";
            }
            jobService.startImapJob(email, login, password);
            return "Запущен IMAP Job для пользователя: " + email;
        } else if ("smtp".equalsIgnoreCase(type)) {
            if (fileName == null || recipient == null) {
                return "Неполные данные для SMTP задачи";
            }
            jobService.startSmtpJob(fileName, recipient, subject, content);
            return "Запущен SMTP Job для получателя: " + recipient;
        } else {
            return "Неверный тип задачи";
        }
    }
}

