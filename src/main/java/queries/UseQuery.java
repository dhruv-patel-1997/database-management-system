package main.java.queries;

import Utilities.Context;
import Utilities.TableUtils;
import main.java.logs.GeneralLog;
import main.java.exceptions.InvalidQueryException;

import java.time.Duration;
import java.time.LocalTime;
import java.util.logging.Logger;

public class UseQuery {
    public boolean useDataBase(String dbName) throws InvalidQueryException {
        GeneralLog generalLog=new GeneralLog();
        Logger generalLogger=generalLog.setLogger();
        generalLogger.info("User: "+ Context.getUserName()+" At the start of use query");
        LocalTime start=LocalTime.now();


        if(Context.setDbName(dbName)){

            LocalTime end=LocalTime.now();
            int diff = Duration.between(end,start).getNano();
            generalLogger.info("User: "+Context.getUserName()+"\nAt the end of add for use query"+"\n"+"Execution Time of query: "+diff +" nanoseconds");
            return true;
        }
        else{
            return false;
        }


    }
}
