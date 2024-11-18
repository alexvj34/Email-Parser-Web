package ru.example.emailparser.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.example.emailparser.database.DatabaseManager;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.FlagTerm;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ImapJob implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ImapJob.class);

    private final String email;
    private final String login;
    private final String password;
    private final DatabaseManager dbManager;  // Создаем объект DatabaseManager
    private Properties properties;

    public ImapJob(String email, String login, String password) {
        properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream resourceStream = loader.getResourceAsStream("config.properties")){
            // Загружаем файл конфигурации

            properties.load(resourceStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
            // Обработка ошибки загрузки файла
        }
        this.email = email;
        this.login = login;
        this.password = password;
        this.dbManager = new DatabaseManager();  // Инициализируем DatabaseManager
    }

    @Override
    public void run() {
        logger.info("Запуск Job IMAP для почты: {}", email);

        properties.getProperty("mail.imap.host");
         properties.getProperty("mail.imap.port");
        properties.getProperty("mail.imap.ssl.enable");
         properties.getProperty("mail.debug");
         properties.getProperty("mail.store.protocol");

        try {
            // Создаем директорию для файлов, если она не существует
            File directory = new File("files");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Подключаемся к почтовому серверу через IMAP
            Session session = Session.getInstance(properties);
            Store store = session.getStore("imaps");

            try {
                store.connect("imap.rambler.ru", login, password);  // Используем правильный хост
                logger.info("Успешное подключение к почте: {}", email);
            } catch (MessagingException e) {
                logger.error("Ошибка подключения к почте: {}", e.getMessage());
                return;  // Выходим из метода, если подключение не удалось
            }

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Ищем непрочитанные письма
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            if (messages.length == 0) {
                logger.info("Непрочитанных писем нет.");
                return;
            }

            for (Message message : messages) {
                logger.info("Найдено непрочитанное письмо: {}", message.getSubject());

                // Скачиваем вложенные файлы
                if (message.getContent() instanceof Multipart) {
                    Multipart multipart = (Multipart) message.getContent();

                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart bodyPart = multipart.getBodyPart(i);

                        if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                            MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;
                            String fileName = mimeBodyPart.getFileName();
                            InputStream inputStream = mimeBodyPart.getInputStream();

                            // Используем метод saveFile для сохранения файла
                            saveFile(inputStream, fileName);

                        }
                    }
                }

                // Помечаем письмо как прочитанное
                message.setFlag(Flags.Flag.SEEN, true);
                logger.info("Письмо помечено как прочитанное: {}", message.getSubject());
            }

            inbox.close(false);
            store.close();

        } catch (MessagingException | IOException e) {
            logger.error("Ошибка при работе с почтой: ", e);
        }
    }

    public void saveFile(InputStream inputStream, String encodedFileName) {
        try {
            String decodedFileName = MimeUtility.decodeText(encodedFileName);
            decodedFileName = decodedFileName.replaceAll("[\\\\/:*?\"<>|]", "_");

            // Проверка на длину имени файла, чтобы избежать проблем с слишком длинными именами
            if (decodedFileName.length() > 255) {
                decodedFileName = decodedFileName.substring(0, 255);  // Уменьшите длину имени файла
            }

            File directory = new File("files");
            if (!directory.exists()) {
                directory.mkdirs(); // Используем mkdirs для создания всех необходимых каталогов
            }

            File file = new File(directory, decodedFileName);
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            logger.info("Файл успешно скачан: {}", decodedFileName);

            // Записываем информацию о файле в базу данных
            dbManager.saveCredentialsAndGetUserId(email,login,password);
            int userId = dbManager.getUserId(email,login,password);
            dbManager.saveFileInfoToDatabase(userId, decodedFileName);
        } catch (Exception e) {
            logger.error("Ошибка при сохранении файла: {}", encodedFileName, e);
        }
    }
}
