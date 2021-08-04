package main.java.queries;


import Utilities.Context;
import Utilities.DataDictionaryUtils;
import Utilities.TableUtils;
import main.java.exceptions.LockTimeOutException;
import main.java.logs.GeneralLog;
import main.java.exceptions.InvalidQueryException;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class DropAlterQuery {
    public boolean dropColumn(String tableName,String columnName){
        try {
            GeneralLog generalLog=new GeneralLog();
            Logger generalLogger=generalLog.setLogger();
            LocalTime start=LocalTime.now();
            generalLogger.info("User: "+ Context.getUserName()+" At the start of adding column for alter query");
            generalLogger.info("Database status at the start of alter query: "+ TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");

            HashMap<String, ArrayList<String>> data=TableUtils.getColumns(Context.getDbName(),tableName);
            if(!data.containsKey(columnName)){
                System.out.println("No column found of the name "+columnName);
                return false;
            }
            if (data != null) {
                data.remove(columnName);
                TableUtils.setColumns(data,tableName);
            }
            try {
                DataDictionaryUtils.dropDictionaryColumn(Context.getDbName(),tableName,columnName);
                LocalTime end=LocalTime.now();

                int diff=end.getNano()-start.getNano();
                generalLogger.info("Database status at the end of alter query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
                generalLogger.info("User: "+Context.getUserName()+"\nAt the end of drop for alter query"+"\n"+"Execution Time of query: "+diff +" nanoseconds");
                return true;
            } catch (LockTimeOutException e) {
                e.printStackTrace();
                return false;
            }
        } catch (IOException | InvalidQueryException | LockTimeOutException e) {
            e.printStackTrace();
            return false;
        }

    }
}
