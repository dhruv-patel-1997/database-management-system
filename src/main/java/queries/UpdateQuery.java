package main.java.queries;

import Utilities.Context;
import Utilities.TableUtils;
import main.java.exceptions.InvalidQueryException;
import main.java.exceptions.LockTimeOutException;
import main.java.logs.GeneralLog;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.logging.Logger;

public class UpdateQuery {
  public void update(String dbName, String tableName, ArrayList<String> columnName, ArrayList<String> columnType, ArrayList<String> columnValue, String colName, String colValue) throws IOException, LockTimeOutException, InvalidQueryException {

    GeneralLog generalLog=new GeneralLog();
    Logger generalLogger=generalLog.setLogger();
    generalLogger.info("User: "+ Context.getUserName()+" Updating the columns for given condition");
    generalLogger.info("Database status at the start of update query: "+ TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
    LocalTime start= LocalTime.now();

    TableUtils.updateHashMap(Context.getDbName(), tableName, columnName, columnType, columnValue, colName, colValue);

    LocalTime end=LocalTime.now();
    int diff=end.getNano()-start.getNano();
    generalLogger.info("Database status at the end of update query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
    generalLogger.info("User: "+Context.getUserName()+"\nAt the end of add for update query"+"\n"+"Execution Time of query: "+diff +" nanoseconds");
  }

}
