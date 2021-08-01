package main.java.queries;

import main.java.Context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DropAlterQuery {
    public void dropColumn(String tableName,String columnName){
        try {
            HashMap<String, ArrayList<String>> data=TableUtils.getColumns(Context.getDbName(),tableName);
            data.remove(columnName);
            TableUtils.setColumns(data,tableName);
            DataDictionaryUtils.dropDictionaryColumn(Context.getDbName(),tableName,columnName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
