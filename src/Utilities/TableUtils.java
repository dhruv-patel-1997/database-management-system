package Utilities;

import Utilities.Context;
import Utilities.DataDictionaryUtils;
import main.java.exceptions.LockTimeOutException;
import main.java.exceptions.InvalidQueryException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.*;
import java.util.*;

public class TableUtils {

  /* public static boolean tableExists(String dbName, String tableName){
     String fileName = "Databases/"+dbName+"/"+tableName+".txt";
     File file = new File (fileName);
     return file.exists();
   }*/
  public static HashMap<String, ArrayList<String>> getColumns(String dbName, String tableName, ArrayList<String> columns) throws IOException, LockTimeOutException {
  //  DataDictionaryUtils.lockTable(dbName,tableName);
    if(Context.setDbName(dbName) && Context.isTableExist(tableName)) {
      File file = new File("Databases/" + dbName + "/" + tableName + ".txt");
      Scanner sc = new Scanner(file);
      HashMap<String, ArrayList<String>> totalColumn = new HashMap<>();


      while (sc.hasNext()) {
        String[] columnDetails = sc.nextLine().split("\\|");
        if(columns.contains(columnDetails[0])) {
          ArrayList<String> columnList = new ArrayList<>();
          for(int i = 1; i < columnDetails.length; i++) {
            if(columnDetails[i].equals(" ")) {
              columnDetails[i] = " ";
            }

            columnList.add(columnDetails[i]);
          }
          totalColumn.put(columnDetails[0],columnList);
        }

      }
      sc.close();
      //DataDictionaryUtils.unlockTable(dbName,tableName);
      return totalColumn;
    }
    return null;
  }
  public static HashMap<String, ArrayList<String>> getColumns(String dbName, String tableName) throws IOException, LockTimeOutException {
    //DataDictionaryUtils.lockTable(dbName,tableName);
    if(Context.setDbName(dbName) && Context.isTableExist(tableName)) {
      File file = new File("Databases/" + dbName + "/" + tableName + ".txt");
      Scanner sc = new Scanner(file);
      HashMap<String, ArrayList<String>> totalColumn = new HashMap<>();
      while (sc.hasNext()) {
        String[] columnDetails = sc.nextLine().split("\\|");
        ArrayList<String> columnList = new ArrayList<>();
        for(int i = 1; i < columnDetails.length; i++) {
          if(columnDetails[i].equals(" ")) {
            columnDetails[i] = " ";
          }
          columnList.add(columnDetails[i]);
        }
        totalColumn.put(columnDetails[0], columnList);
      }
      sc.close();
   //   DataDictionaryUtils.unlockTable(dbName,tableName);
      return totalColumn;
    }
   // DataDictionaryUtils.unlockTable(dbName,tableName);
    return null;
  }

  public static void setColumns(HashMap<String, ArrayList<String>> data,String tableName){
    File file = new File("Databases/" + Context.getDbName() + "/" + tableName + ".txt");
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(file);
      writer.print("");
      writer.close();

      FileWriter myWriter = new FileWriter("Databases/" + Context.getDbName() + "/" + tableName + ".txt");
      Iterator hmIterator = data.entrySet().iterator();
      while (hmIterator.hasNext()) {
        StringBuilder sbf = new StringBuilder("");
        Map.Entry mapElement = (Map.Entry) hmIterator.next();
        sbf.append(mapElement.getKey() + "|");
        ArrayList<String> temp = (ArrayList<String>) mapElement.getValue();
        for (String s : temp) {
          sbf.append(s + "|");
        }
        String s = sbf.toString().substring(0, sbf.length() - 1);
        System.out.println(s);
        myWriter.write(s+"\n");
      }

      myWriter.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static int getRows(String dbName, String tableName) {
    HashMap<String, ArrayList<String>> totalColumn;
    try {
      totalColumn = getColumns(dbName, tableName);
      if(totalColumn != null){
        for (Map.Entry mapElement : totalColumn.entrySet()) {
          String key = (String) mapElement.getKey();
          if(!totalColumn.get(key).isEmpty())
            return totalColumn.get(key).size();
          else
            return 0;
        }
      }
      return 0;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return 0;
    } catch (IOException e) {
      e.printStackTrace();
      return 0;
    } catch (LockTimeOutException e) {
      e.printStackTrace();
      return 0;
    }
  }

  public static ArrayList<String> getTableInDb(String dbName){
    File folder = new File("Databases/" + dbName);
    return listFilesForFolder(folder);
  }

  public static ArrayList<String> getTableInDb_DD(String dbName){
    File folder = new File("Databases/" + dbName);
    return listFilesForFolder_DD(folder);
  }

  public static HashMap<String,Integer> getGeneralLogTableInfo(String dbName) throws InvalidQueryException{
    if(dbName==null){
      throw new InvalidQueryException("Please select database first.");
    }
    HashMap<String,Integer> generalLog=new HashMap<>();
    if(getTableInDb(dbName)!=null){
      ArrayList<String> tables=getTableInDb(dbName);

      for(String table:tables){
        table=table.split("\\.")[0];
        generalLog.put(table,getRows(dbName,table));
      }
    }
    return generalLog;
  }

  private static ArrayList<String> listFilesForFolder(final File folder) {
    ArrayList<String> files=new ArrayList<>();
    if (folder.listFiles() == null || folder.listFiles().length == 0) {
      return null;
    } else {
      for (final File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
          listFilesForFolder(fileEntry);
        } else {
          if(!fileEntry.getName().contains("dd_")){
            files.add(fileEntry.getName());
          }
        }
      }
    }
    return files;
  }

  private static ArrayList<String> listFilesForFolder_DD(final File folder) {
    ArrayList<String> files=new ArrayList<>();
    if(folder.listFiles().length!=0){
      for (final File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
          listFilesForFolder(fileEntry);
        } else {
          if(fileEntry.getName().contains("dd_")){
            files.add(fileEntry.getName());
          }
        }
      }
    }
    else{
      return null;
    }
    return files;
  }

  public static void addEmptyColumnData(String tableName,String colName){
    try{
      int rowCount = getRows(Context.getDbName(),tableName);
      StringBuilder sbf = new StringBuilder();
      sbf.append(colName).append("|");
      sbf.append(" |".repeat(rowCount));
      sbf.append("\n");
      FileWriter myWriter = new FileWriter("Databases/" + Context.getDbName() + "/" + tableName + ".txt",true);

      myWriter.append(sbf.toString());
      myWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static boolean insertRow(String dbName, String tableName, HashMap<String, String> insertData) throws IOException, LockTimeOutException {

    HashMap<String, ArrayList<String>> tableData = getColumns(dbName, tableName);
    for(Map.Entry<String, ArrayList<String>> entry: tableData.entrySet()) {
      entry.getValue().add(insertData.get(entry.getKey()));
    }
    insertTableData(dbName, tableName, tableData);
    return true;
  }
  public static void insertTableData(String dbName, String tableName, HashMap<String, ArrayList<String>> tableData) {
    if(Context.setDbName(dbName) && Context.isTableExist(tableName)) {
      try {
        FileWriter myWriter = new FileWriter("Databases/" + dbName + "/" + tableName + ".txt");

        for(Map.Entry<String, ArrayList<String>> entry: tableData.entrySet()) {
          myWriter.write(entry.getKey() + "|");
          for(int i = 0; i < entry.getValue().size(); i++) {
            myWriter.write(entry.getValue().get(i) + "|");
          }
          myWriter.write("\n");
        }
        myWriter.close();

      } catch (IOException e) {

        e.printStackTrace();
      }
    }
  }
  public static HashMap<String, ArrayList<String>> getColumnsForEquals(String dbName,String tableName,String colName,String colValue,String operand) throws IOException, LockTimeOutException {
    HashMap<String, ArrayList<String>> tableData= getColumns(dbName,tableName);
    ArrayList<Integer> indexes = new ArrayList<>();
    if(operand.equals("=")){
    for(Map.Entry<String, ArrayList<String>> entry: tableData.entrySet()) {
      if(entry.getKey().equals(colName)) {
        for(int i = 0; i < entry.getValue().size(); i++)
        {
          if(entry.getValue().get(i).equals(colValue))
          {
            indexes.add(i);
          }
        }
      }
    }
    }else if(operand.equals("<"))
    {
      for(Map.Entry<String, ArrayList<String>> entry: tableData.entrySet()) {
        if(entry.getKey().equals(colName)) {
          for(int i = 0; i < entry.getValue().size(); i++)
          {
            if(Integer.parseInt(entry.getValue().get(i))<Integer.parseInt(colValue))
            {
              indexes.add(i);
            }
          }
        }
      }
    }else if(operand.equals(">"))
    {
      for(Map.Entry<String, ArrayList<String>> entry: tableData.entrySet()) {
        if(entry.getKey().equals(colName)) {
          for(int i = 0; i < entry.getValue().size(); i++)
          {
            if(Integer.parseInt(entry.getValue().get(i))>Integer.parseInt(colValue))
            {
              indexes.add(i);
            }
          }
        }
      }
    }
    HashMap<String, ArrayList<String>> updatedData= new HashMap<>();

    for(Map.Entry<String, ArrayList<String>> entry: tableData.entrySet()) {
      ArrayList<String> colValues = new ArrayList<>();
      for(int i=0;i<indexes.size();i++)
      {
        colValues.add(entry.getValue().get(indexes.get(i)));
      }
      updatedData.put(entry.getKey(),colValues);
    }
    return updatedData;

  }
  public static HashMap<String, ArrayList<String>> getLimitedColumnsForEquals(String dbName,String tableName,String colName,String colValue,ArrayList<String> columns,String operand) throws IOException, LockTimeOutException {

    HashMap<String, ArrayList<String>> updatedData = getColumnsForEquals(dbName,tableName,colName,colValue,operand);
    ArrayList<String> removedList = new ArrayList<>();
    for(Map.Entry<String, ArrayList<String>> entry: updatedData.entrySet()) {
      if(!columns.contains(entry.getKey()))
      {
        removedList.add(entry.getKey());
      }
    }
    for(int i=0;i<removedList.size();i++)
    {
      updatedData.remove(removedList.get(i));
    }

    return updatedData;

  }
  public static boolean updateHashMap(String dbName,String tableName,ArrayList<String> columnName,ArrayList<String> columnType,ArrayList<String> columnValue,String colName,String colValue) throws IOException, LockTimeOutException {
   //DataDictionaryUtils.lockTable(dbName,tableName);
    HashMap<String,ArrayList<String>> tableData = getColumns(dbName,tableName);
    ArrayList<Integer> indexes = new ArrayList<>();
    for(int i=0;i<tableData.get(colName).size();i++)
    {
      if(tableData.get(colName).get(i).equals(colValue))
        indexes.add(i);
    }
    for(int i=0;i<columnName.size();i++)
    {
      for(int j=0;j<indexes.size();j++) {
        tableData.get(columnName.get(i)).set(indexes.get(j), columnValue.get(i));
      }
    }
    insertTableData(dbName,tableName,tableData);
  //  DataDictionaryUtils.unlockTable(dbName,tableName);
    return true;
  }
  public static ArrayList<String> getInsertStatements(String dbName) throws IOException, LockTimeOutException {
    ArrayList<String> tableNames = getTableInDb(dbName);
    for(int i=0;i<tableNames.size();i++)
    {
      tableNames.set(i,tableNames.get(i).substring(0,tableNames.get(i).length()-4));
    }
    ArrayList<String> insertStatements = new ArrayList<>();
    if(tableNames==null)
      return null;
    else
    {
      for(int tableIndex=0;tableIndex<tableNames.size();tableIndex++)
      {
        HashMap<String,ArrayList<String>> tableData = getColumns(dbName,tableNames.get(tableIndex));
        ArrayList<String> headings = new ArrayList<>();
        int totalRowSize=0;
        for(Map.Entry<String, ArrayList<String>> entry: tableData.entrySet()) {
          headings.add(entry.getKey());
          if(entry.getValue().size()>totalRowSize)
            totalRowSize=entry.getValue().size();
        }

        for(int i=0;i<totalRowSize;i++)
        {
          String insertstatement= "INSERT INTO "+tableNames.get(tableIndex)+" (";
          for(int headingIndex=0;headingIndex<headings.size();headingIndex++)
          {
            insertstatement=insertstatement.concat(headings.get(headingIndex));
            if(headingIndex<headings.size()-1)
              insertstatement= insertstatement.concat(", ");
            else
              insertstatement=insertstatement.concat(")");
          }
          insertstatement=insertstatement.concat(" VALUES (");
          for(int j=0;j<headings.size();j++) {
            if(tableData.get(headings.get(j)).get(i).equals(" "))
              insertstatement=insertstatement.concat("NULL");
            else
              insertstatement=insertstatement.concat(tableData.get(headings.get(j)).get(i));
            if(j< headings.size()-1)
              insertstatement=insertstatement.concat(", ");
            else
              insertstatement=insertstatement.concat(")");
          }
          insertstatement= insertstatement.concat(";");
          insertStatements.add(insertstatement);
        }
      }
    }
    return  insertStatements;
  }
}
