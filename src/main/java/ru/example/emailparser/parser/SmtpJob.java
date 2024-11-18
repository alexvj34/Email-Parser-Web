package ru.example.emailparser.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.example.emailparser.database.DatabaseManager;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class SmtpJob implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SmtpJob.class);
    private final String fileName;
    private final String recipient;
    private final String subject;
    private final String content;
    private final DatabaseManager dbManager;

    private Properties properties;

    public SmtpJob(String fileName, String recipient, String subject, String content) {
        properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("src/main/resources/config.properties")){
            // Загружаем файл конфигурации
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            // Обработка ошибки загрузки файла
        }
        this.fileName = fileName;
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.dbManager = new DatabaseManager();

    }

    @Override
    public void run() {
        logger.info("Запуск SMTP Job для отправки файла: {}", fileName);

        int fileId = fetchFileId();
        if (fileId == -1) {
            logger.error("Файл с указанным именем или ID не найден: {}", fileName);
            return;
        }

        // Получаем параметры из файла .properties
        properties.getProperty("mail.smtp.host");
         properties.getProperty("mail.smtp.port");
        properties.getProperty("mail.smtp.auth");
        properties.getProperty("mail.smtp.ssl.enable");
        String login = properties.getProperty("mail.login");
        String password = properties.getProperty("mail.password");

        // Создаем сессию с аутентификацией
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(login, password);
            }
        });
        session.setDebug(true);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(login));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(subject);

            // Создание тела письма
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(content + "\n\nС уважением,\nВаше имя\nВаш телефон");

            // Мульти-часть для письма (тело и вложения)
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Проверка наличия файла и прикрепление его к письму
            File file = new File("D:/Свои проекты/javaTraining/email-parser/files/" + fileName);
            if (file.exists()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                // Используем правильный класс FileDataSource для вложения
                FileDataSource source = new FileDataSource(file);
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(file.getName());
                multipart.addBodyPart(attachmentPart);
                logger.info("Файл {} прикреплен к письму.", fileName);
            } else {
                logger.warn("Файл не найден: {}", fileName);
            }

            // Устанавливаем содержимое письма
            message.setContent(multipart);

            // Отправка сообщения
            Transport.send(message);
            logger.info("Письмо успешно отправлено: {}", recipient);

            // Логирование отправленного письма в базу данных
            logSentEmail(fileId);

        } catch (MessagingException e) {
            logger.error("Ошибка при отправке письма: {}", e.getMessage());
        }
    }

    private int fetchFileId() {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id FROM file_records WHERE id = ? OR name_of_files = ?")) {
            stmt.setString(1, fileName);
            stmt.setString(2, fileName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске файла: {}", e.getMessage());
        }
        return -1;
    }

    private void logSentEmail(int fileId) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO sent_emails (file_id, recipient_email) VALUES (?, ?)")) {
            stmt.setInt(1, fileId);
            stmt.setString(2, recipient);
            stmt.executeUpdate();
            logger.info("Отправленное письмо успешно записано в базу данных.");
        } catch (SQLException e) {
            logger.error("Ошибка при записи отправленного письма в базу данных: {}", e.getMessage());
        }
    }
}
