package main.java.queries;

import main.java.Context;
import main.java.logs.QueryLog;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class CreateQuery {
    public void createTable(String tableName, HashMap<String,Column> columns, List<PrimaryKey> primaryKeys, List<ForeignKey> foreignKeys) {
        //TODO:Validation and execution logic
        //table can't already exist, foreign key referenced table and row must exist
        //no duplicate column names in columns
        //column names in primary keys must be present in columns
        //no duplicates in primarykeys??
        //create dd
    }


    public void createDatabase(String dbName) {
        //TODO:Validation and execution logic
        //database can't be present already
    }
}
