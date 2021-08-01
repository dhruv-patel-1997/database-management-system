package main.java.queries;

import java.util.HashSet;

public class PrimaryKey {
    private final HashSet<String> columnNames;

    public PrimaryKey(HashSet<String> columnNames) {
        this.columnNames = columnNames;
    }

    public HashSet<String> getColumnNames(){
        return columnNames;
    }
}
