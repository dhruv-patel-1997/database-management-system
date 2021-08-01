package main.java.queries;

import java.io.IOException;

public class AddAlterQuery {
    public void addColumn(String tableName,String columnName,String columnType){
        try {
            TableUtils.addEmptyColumnData(tableName,columnName);
            DataDictionaryUtils.addColumn(tableName,columnName,new Column(columnName,columnType));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
