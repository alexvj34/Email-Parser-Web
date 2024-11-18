# Email-Parser-Web

Email Parser — это Java приложение, предназначенное для автоматизации работы с почтовыми ящиками и отправки писем с вложениями через SMTP. Проект включает в себя функциональность для получения писем через IMAP, обработки вложений и отправки писем с файлом в качестве вложения. Вся информация о загруженных файлах и отправленных письмах сохраняется в базе данных.

## Описание
Этот проект включает два основных компонента:

- ### IMAP Job — для получения новых писем, скачивания вложений и сохранения их в базу данных.
- ### SMTP Job — для отправки писем с вложениями на основе данных из базы.
Проект использует MySQL для хранения учетных данных и записей о файлах. Также добавлена возможность отправки уведомлений о выполненных задачах через email.

## Расписание задач
Для управления задачами используется Quartz Scheduler. Это позволяет запускать задачи (IMAP и SMTP) по расписанию, что упрощает автоматизацию процессов и предотвращает необходимость вручную запускать задачи. Каждая задача может быть запланирована с указанием интервала времени (например, ежедневно или раз в час).

## Основные функции расписания:

Задание может быть выполнено один раз или повторяться с указанным интервалом.
Поддержка различных типов расписаний (например, cron-выражения или фиксированные интервалы).
Все выполненные задачи логируются и сохраняются в базе данных для дальнейшего анализа.


## Структура проекта

src/main/java/ru/example/emailparser — основной код приложения.
parser/ — содержит классы для выполнения задач IMAP и SMTP.
database/ — работа с базой данных.
resources/ — веб-интерфейс (HTML, CSS, JavaScript).
src/main/resources/ — конфигурационные файлы и файл свойств для почтового сервиса.


### Требования
Java 8 или выше
MySQL база данных

### Внешние библиотеки:
JavaMail
SLF4J
Hibernate (если используется для взаимодействия с базой данных)
Quartz Scheduler (для управления расписанием)
### Установка и запуск
1. Клонируйте репозиторий.
2. Создайте базу данных в MySQL и используйте предоставленные SQL команды для создания таблиц.

```sql
CREATE TABLE credentials (
id INT AUTO_INCREMENT PRIMARY KEY,
mail VARCHAR(255) NOT NULL,
login VARCHAR(255) NOT NULL,
pass VARCHAR(255) NOT NULL
);

CREATE TABLE file_records (
id INT AUTO_INCREMENT PRIMARY KEY,
user_id INT NOT NULL,
date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
name_of_files VARCHAR(255) NOT NULL,
FOREIGN KEY (user_id) REFERENCES credentials(id)
);

CREATE TABLE sent_emails (
id INT AUTO_INCREMENT PRIMARY KEY,
file_id INT NOT NULL,
recipient_email VARCHAR(255) NOT NULL,
sent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (file_id) REFERENCES file_records(id)
);

CREATE TABLE scheduled_jobs (
id INT AUTO_INCREMENT PRIMARY KEY,
job_name VARCHAR(255) NOT NULL,
cron_expression VARCHAR(255) NOT NULL,
last_run TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
status VARCHAR(50) NOT NULL
);

структуры таблиц:
-  credentials: Эта таблица хранит данные для аутентификации почтовых сервисов.
-  file_records: Таблица содержит информацию о загруженных или обработанных файлах, которые привязаны к пользователям через поле user_id (ссылается на id в таблице credentials).
-  sent_emails: Таблица для записи отправленных писем. Она содержит информацию о файле, который был отправлен, и получателе письма. file_id ссылается на file_records.
```
3. Настройте подключение к базе данных и почтовому сервису в application.properties.
### Если используете Яндекс почту, обязательно генерируйте специальный пароль для доступа. Не пароль от почты!
Вся дока для настройки mail.password https://yandex.ru/support/mail/mail-clients/others.html)
Также в настройках вашего почтового клиента, необходимо настроить работу с imap и smtp!

4. Соберите и запустите приложение:

```
mvn clean install
mvn spring-boot:run
```
5. Откройте веб-интерфейс в браузере по адресу:

```
http://localhost:8080
```

## Пример использования
Веб-интерфейс позволяет вам:
Выбирать тип задачи (IMAP или SMTP).
Вводить параметры почтового сервиса (логин, пароль, получатель).
Просматривать sql таблицы.
И так далее.