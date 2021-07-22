package main.java.queries;

import java.util.List;

public class CreateQuery {
    public void createTable(String tableName, List<Column> columns, List<PrimaryKey> primaryKeys, List<ForeignKey> foreignKeys) {
        //TODO:Validation and execution logic
        //table can't already exist, foreign key referenced table and row must exist
        //no duplicate column names in columns
        //column names in primary keys must be present in columns
        //no duplicates in primarykeys??
    }


    public void createDatabase(String dbName) {
        //TODO:Validation and execution logic
        //database can't be present already
    }
}
