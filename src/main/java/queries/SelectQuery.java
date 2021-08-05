package main.java.queries;

import Utilities.Context;
import Utilities.TableMaker;
import Utilities.TableUtils;
import main.java.exceptions.InvalidQueryException;
import main.java.exceptions.LockTimeOutException;
import main.java.logs.GeneralLog;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class SelectQuery {

  public void selectForAll(String dbName, String tableName) throws IOException, LockTimeOutException, InvalidQueryException {
    GeneralLog generalLog=new GeneralLog();
    Logger generalLogger=generalLog.setLogger();
    generalLogger.info("User: "+ Context.getUserName()+" Select statement running displaying table ");
    generalLogger.info("Database status at the start of SELECT query: "+ TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
    LocalTime start= LocalTime.now();

    HashMap<String, ArrayList<String>> data = TableUtils.getColumns(Context.getDbName(), tableName);

    LocalTime end=LocalTime.now();
    int diff=end.getNano()-start.getNano();
    generalLogger.info("Database status at the end of SELECT query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
    generalLogger.info("User: "+Context.getUserName()+"\nAt the end of add for SELECT query"+"\n"+"Execution Time of query: "+diff +" nanoseconds");

    System.out.println();
    showTable(data);

  }
  private void showTable(HashMap<String, ArrayList<String>> tableData)
  {
    TableMaker tm = new TableMaker();
    tm.printTable(tableData);
  }

  public void showAllForCondition(String dbName, String tableName, String colName, String finalColumnValue, String operand) throws IOException, LockTimeOutException, InvalidQueryException {

    GeneralLog generalLog=new GeneralLog();
    Logger generalLogger=generalLog.setLogger();
    generalLogger.info("User: "+ Context.getUserName()+" Select statement running displaying table ");
    generalLogger.info("Database status at the start of SELECT query: "+ TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
    LocalTime start= LocalTime.now();

    HashMap<String, ArrayList<String>> data =TableUtils.getColumnsForEquals(Context.getDbName(), tableName, colName, finalColumnValue, operand);

    LocalTime end=LocalTime.now();
    int diff = Duration.between(end,start).getNano();
    generalLogger.info("Database status at the end of SELECT query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
    generalLogger.info("User: "+Context.getUserName()+"\nAt the end of add for SELECT query"+"\n"+"Execution Time of query: "+diff +" nanoseconds");

    System.out.println();
    showTable(data);

  }
  public void showForLimited(String dbName, String tableName, ArrayList<String> columns) throws IOException, LockTimeOutException, InvalidQueryException {

    GeneralLog generalLog=new GeneralLog();
    Logger generalLogger=generalLog.setLogger();
    generalLogger.info("User: "+ Context.getUserName()+" Select statement running displaying table ");
    generalLogger.info("Database status at the start of SELECT query: "+ TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
    LocalTime start= LocalTime.now();

    HashMap<String, ArrayList<String>> data= TableUtils.getColumns(Context.getDbName(), tableName, columns);


    LocalTime end=LocalTime.now();
    int diff = Duration.between(end,start).getNano();
    generalLogger.info("Database status at the end of SELECT query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
    generalLogger.info("User: "+Context.getUserName()+"\nAt the end of add for SELECT query"+"\n"+"Execution Time of query: "+diff +" nanoseconds");

    System.out.println();
    showTable(data);
  }
  public void showForColumnsForCondition(String dbName, String tableName, String colName, String finalColumnValue, ArrayList<String> columns, String operand) throws IOException, LockTimeOutException, InvalidQueryException {


    GeneralLog generalLog=new GeneralLog();
    Logger generalLogger=generalLog.setLogger();
    generalLogger.info("User: "+ Context.getUserName()+" Select statement running displaying table ");
    generalLogger.info("Database status at the start of SELECT query: "+ TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
    LocalTime start= LocalTime.now();


    HashMap<String, ArrayList<String>> data = TableUtils.getLimitedColumnsForEquals(Context.getDbName(), tableName, colName, finalColumnValue, columns, operand);

    LocalTime end=LocalTime.now();
    int diff = Duration.between(end,start).getNano();
    generalLogger.info("Database status at the end of SELECT query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
    generalLogger.info("User: "+Context.getUserName()+"\nAt the end of add for SELECT query"+"\n"+"Execution Time of query: "+diff +" nanoseconds");

    System.out.println();
    showTable(data);

  }
}
