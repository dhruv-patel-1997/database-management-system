package main.java.queries;

import Utilities.Context;
import main.java.logs.GeneralLog;

import java.io.IOException;
import java.time.LocalTime;
import java.util.logging.Logger;

public class AddAlterQuery {
    public void addColumn(String tableName,String columnName,String columnType){
        GeneralLog generalLog=new GeneralLog();
        Logger generalLogger=generalLog.setLogger();
        generalLogger.info("User: "+ Context.getUserName()+" At the start of adding column for alter query");
        generalLogger.info("Database status at the start of alter query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
        LocalTime start=LocalTime.now();
        try {
            TableUtils.addEmptyColumnData(tableName,columnName);
            try {
                DataDictionaryUtils.addColumn(tableName,columnName,new Column(columnName,columnType));
                LocalTime end=LocalTime.now();

                int diff=end.getNano()-start.getNano();
                generalLogger.info("Database status at the end of alter query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
                generalLogger.info("User: "+Context.getUserName()+"\nAt the end of add for alter query"+"\n"+"Execution Time of query: "+diff +" nanoseconds");
            } catch (LockTimeOutException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
