package main.java.queries;

import main.java.Context;
import main.java.logs.GeneralLog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class DropAlterQuery {
    public void dropColumn(String tableName,String columnName){
        GeneralLog generalLog=new GeneralLog();
        Logger generalLogger=generalLog.setLogger();
        LocalTime start=LocalTime.now();
        generalLogger.info("User: "+ Context.getUserName()+" At the start of adding column for alter query");
        generalLogger.info("Database status at the start of alter query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
        try {
            HashMap<String, ArrayList<String>> data=TableUtils.getColumns(Context.getDbName(),tableName);
            data.remove(columnName);
            TableUtils.setColumns(data,tableName);
            try {
                DataDictionaryUtils.dropDictionaryColumn(Context.getDbName(),tableName,columnName);
                LocalTime end=LocalTime.now();

                int diff=end.getNano()-start.getNano();
                generalLogger.info("Database status at the end of alter query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
                generalLogger.info("User: "+Context.getUserName()+"\nAt the end of drop for alter query"+"\n"+"Execution Time of query: "+diff +" nanoseconds");
            } catch (LockTimeOutException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}