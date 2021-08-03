package main.java.queries;

import Utilities.Context;
import main.java.parsing.Token;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

public class DataDictionaryUtils {

  private DataDictionaryUtils() {
  }

  public static boolean tableDictionaryExists(String dbName, String tableName) {
    String fileName = "Databases/" + dbName + "/dd_" + tableName + ".txt";
    File file = new File(fileName);
    return file.exists();
  }

  public static LinkedHashMap<String, Column> getColumns(String dbName, String tableName) throws LockTimeOutException {
      File file = new File("Databases/" + dbName + "/dd_" + tableName + ".txt");
      if (file.exists()){
          lockTable(dbName,tableName);
          Scanner sc = null;
          try {
              sc = new Scanner(file);
          } catch (FileNotFoundException e) {
              e.printStackTrace();
              return null;
          }
          LinkedHashMap<String, Column> columns = new LinkedHashMap<>();

          sc.nextLine();
          while (sc.hasNext()) {
            String[] columnDetails = sc.nextLine().split("\\|");
            String colName = columnDetails[0];
            String dataType = columnDetails[1];
            boolean allowNull = Boolean.parseBoolean(columnDetails[2]);
            boolean pk = Boolean.parseBoolean(columnDetails[3]);

            String fk = null;
            if(columnDetails.length > 4) {
              fk = columnDetails[4];
            }

            Column column = new Column(colName, dataType);
            column.setAllowNulls(allowNull);
            column.setAsPrimaryKey(pk);

            if(fk != null) {
              String[] fkDetails = fk.split(" ");
              ForeignKey key = new ForeignKey(colName, fkDetails[0], fkDetails[1]);
              column.setForeignKey(key);
            }
            columns.put(colName, column);
          }
          sc.close();
          unlockTable(dbName,tableName);
          return columns;
      }
      return null;
  }

  public static void create(String dbName, String tableName, List<Column> columns) throws IOException {
    //get file or make new
    File dbPath = new File("Databases/" + dbName);
    dbPath.mkdir();

    String fileName = "Databases/" + dbName + "/tempDD.txt";
    File file = new File(fileName);

    FileWriter fileWriter = new FileWriter(fileName, false);

    fileWriter.write("[unlocked]\n");
    if(columns != null) {
      for(Column column: columns) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(column.getColName()).append("|");
        stringBuilder.append(column.getDataType()).append("|");
        stringBuilder.append(column.getAllowNulls()).append("|");
        stringBuilder.append(column.isPrimaryKey()).append("|");

        ForeignKey fk = column.getForeignKey();
        if(fk != null) {
          stringBuilder.append(fk.getReferencedTable()).append(" ").append(fk.getReferencedColumn());
        }
        fileWriter.write(stringBuilder.toString() + "\n");
      }
    }
    fileWriter.close();
    file.renameTo(new File("Databases/" + dbName + "/dd_" + tableName + ".txt"));
  }

  public static void addColumn(String dbName, String tableName, Column column) throws IOException, LockTimeOutException {
    String fileName = "Databases/" + dbName + "/dd_" + tableName + ".txt";
    File file = new File(fileName);
    if(file.exists()) {
        lockTable(dbName,tableName);
      FileWriter fileWriter = new FileWriter(fileName, true);

      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(column.getColName()).append("|");
      stringBuilder.append(column.getDataType()).append("|");
      stringBuilder.append(column.getAllowNulls()).append("|");
      stringBuilder.append(column.isPrimaryKey()).append("|");

      ForeignKey fk = column.getForeignKey();
      if(fk != null) {
        stringBuilder.append(fk.getReferencedTable()).append(" ").append(fk.getReferencedColumn());
      }
      stringBuilder.append("\n");
      fileWriter.write(stringBuilder.toString());
      fileWriter.close();
        unlockTable(dbName,tableName);
    } else {
      throw new FileNotFoundException();
    }
  }

  public static void dropDictionaryColumn(String dbName, String tableName, String colName) throws IOException, LockTimeOutException {
    String fileName = "Databases/" + dbName + "/dd_" + tableName + ".txt";
    File file = new File(fileName);
    if(file.exists()) {
        lockTable(dbName,tableName);
      Scanner sc = new Scanner(file);
      StringBuilder stringBuilder = new StringBuilder();
      String row;
      while (sc.hasNext()) {
        row = sc.nextLine();
        if(! row.split("\\|")[0].matches(colName)) {
          stringBuilder.append(row).append("\n");
        }
      }
      sc.close();
      FileWriter fileWriter = new FileWriter(file);
      fileWriter.append(stringBuilder.toString());
      fileWriter.close();
        unlockTable(dbName,tableName);
    }
  }

    public static void dropDictionaryTable(String dbName, String tableName) throws LockTimeOutException {
        String fileName = "Databases/"+dbName+"/dd_"+tableName+".txt";
        File file = new File (fileName);
        if (file.exists()) {
            lockTable(dbName,tableName);
            file.delete();
        }
    }

    public static void lockTable(String dbName, String tableName) throws LockTimeOutException {
        File file = new File("Databases/"+dbName+"/dd_"+tableName+".txt");
        boolean obtainedLock = false;
        int time = 0;
        while (!obtainedLock && time<5000) {
            try {
                Scanner sc = new Scanner(file);
                String lockValue = sc.next();
                boolean unlocked = (lockValue.equals("[unlocked]")||lockValue.equals("["+Context.getTransactionId()+"]"));
                sc.nextLine();
                if (unlocked) {
                    StringBuilder content = new StringBuilder();
                    content.append("[").append(Context.getTransactionId()).append("]\n");
                    while (sc.hasNext()) {
                        content.append(sc.nextLine()).append("\n");
                    }

                    FileWriter fw = new FileWriter(file);
                    fw.write(content.toString());
                    fw.close();
                    obtainedLock = true;

                } else {

                    try {
                        double timeToWait = 1000 * Math.random();
                        Thread.sleep((long) timeToWait);
                        time += timeToWait;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                sc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!obtainedLock){
            throw new LockTimeOutException();
        }
    }

    public static void unlockTable(String dbName, String tableName){
        File file = new File("Databases/"+dbName+"/dd_"+tableName+".txt");
        try {
            Scanner sc = new Scanner(file);
            sc.nextLine();
            StringBuilder content = new StringBuilder();
            content.append("[unlocked]\n");
            while (sc.hasNext()) {
                content.append(sc.nextLine()).append("\n");
            }
            sc.close();
            FileWriter fw = new FileWriter(file);
            fw.write(content.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean equalsDataType(String thisDataType, String referenceDataType) {
        //this datatype is varchar
        String[] first = thisDataType.split(" ");
        String[] second = referenceDataType.split(" ");
        if (first[0].equals("VARCHAR") && second[0].equals("VARCHAR")){
            if (first.length == 2 && second.length == 2){
                if (Integer.parseInt(first[1])<=Integer.parseInt(second[1])){
                    return true;
                }
            }
        }
        return thisDataType.equals(referenceDataType);
    }


    public static boolean valueIsOfDataType(Token value, String dataType){
        Boolean result = false;
        switch (dataType){
            case "INT":
                if (value.getType() == Token.Type.INTLITERAL){
                    result = true;
                }
                break;
            case "DECIMAL":
                if (value.getType() == Token.Type.DECIMALLITERAL){
                    result = true;
                }
                break;
            case "BOOLEAN":
                if (value.getType() == Token.Type.BOOLEANLITERAL){
                    result = true;
                }
                break;
            case "TEXT":
                if (value.getType() == Token.Type.STRING){
                    result = true;
                }
                break;
            default:
                if (value.getType() == Token.Type.STRING) {
                    String[] dataTypeArr = dataType.split(" ");
                    if (dataTypeArr[0].equals("VARCHAR")) {
                        if (Integer.parseInt(dataTypeArr[1]) >= value.getStringValue().length()) {
                            result = true;
                        }
                    }
                }
                break;
        }
        return result;
    }
}
