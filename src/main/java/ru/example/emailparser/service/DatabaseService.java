package ru.example.emailparser.service;

import org.springframework.stereotype.Service;
import ru.example.emailparser.database.DatabaseManager;

@Service
public class DatabaseService {

    private final DatabaseManager dbManager;

    public DatabaseService() {
        this.dbManager = new DatabaseManager();
    }

    public String listTables() {
        return dbManager.listTables();
    }

    public String listRecords() {
        return dbManager.listRecords();
    }
}
