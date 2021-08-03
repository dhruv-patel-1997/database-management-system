package main.java.queries;

import Utilities.Context;
import main.java.logs.GeneralLog;
import main.java.parsing.InvalidQueryException;

import java.time.LocalTime;
import java.util.logging.Logger;

public class UseQuery {
    public boolean useDataBase(String dbName) throws InvalidQueryException {


        if(Context.setDbName(dbName)){
            GeneralLog generalLog=new GeneralLog();
            Logger generalLogger=generalLog.setLogger();
            generalLogger.info("User: "+ Context.getUserName()+" At the start of use query");
            generalLogger.info("Database status at the start of use query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
            LocalTime start=LocalTime.now();
            LocalTime end=LocalTime.now();
            int diff=end.getNano()-start.getNano();
            generalLogger.info("Database status at the end of alter query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
            generalLogger.info("User: "+Context.getUserName()+"\nAt the end of add for alter query"+"\n"+"Execution Time of query: "+diff +" nanoseconds");
            return true;
        }
        else{
            return false;
        }


    }
}
