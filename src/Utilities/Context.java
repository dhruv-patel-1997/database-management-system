package Utilities;

import java.io.File;
import java.time.LocalDateTime;

public class Context {

  private static String dbName;
  private static String dbPath;
  private static String userName;
  private static String transactionNum;

  public static String getDbPath() {
    return dbPath;
  }
  public static String getDbName() {
    return dbName;
  }
  public static String getUserName() {return userName;}

  public static boolean setDbName(String dbName) {

    String directoryPath = "Databases/" + dbName + "/";
    File file = new File(directoryPath);

    if(file.isDirectory()) {
      Context.dbName = dbName;
      setDbPath();
      return true;
    } else {
      return false;
    }

  }

  private static void setDbPath() {
    Context.dbPath = "Databases/" + dbName + "/";
  }

  public static boolean isTableExist(String tableName) {
    File f = new File(getDbPath()+tableName+".txt");
    if (f.exists())
      return true;
    else
      return false;
  }

  public static void setUserName(String userName){
    Context.userName = userName;
  }

  public static void logout() {
    userName = null;
    dbPath = null;
    dbName = null;
  }

  public static String getTransactionId() {
    return userName+transactionNum;
  }

  public static void incrTransactionId(){
    System.out.println(transactionNum);
    transactionNum = Thread.currentThread().getId() + "-" + LocalDateTime.now().toString();

  }
}
