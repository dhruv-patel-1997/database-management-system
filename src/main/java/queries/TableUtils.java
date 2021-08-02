package main.java.queries;

import Utilities.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TableUtils {
 /* public static boolean tableExists(String dbName, String tableName){
    String fileName = "Databases/"+dbName+"/"+tableName+".txt";
    File file = new File (fileName);
    return file.exists();
  }*/
  public static HashMap<String,ArrayList<String>> getColumns(String dbName, String tableName,ArrayList<String> columns) throws FileNotFoundException {
    if (Context.setDbName(dbName)&&Context.isTableExist(tableName)){
      File file = new File("Databases/"+dbName+"/"+tableName+".txt");
      Scanner sc = new Scanner(file);
      HashMap<String, ArrayList<String>> totalColumn = new HashMap<>();


      while (sc.hasNext()) {
        String[] columnDetails = sc.nextLine().split("\\|");
        if(columns.contains(columnDetails[0])) {
          ArrayList<String> columnList = new ArrayList<>();
          for(int i = 1; i < columnDetails.length; i++) {
              columnList.add(columnDetails[i]);
          }
          totalColumn.put(columnDetails[0],columnList);
        }

      }
      sc.close();
      return totalColumn;
    }
    return null;
  }
  public static HashMap<String,ArrayList<String>> getColumns(String dbName, String tableName) throws FileNotFoundException {
    if (Context.setDbName(dbName)&&Context.isTableExist(tableName)){
      File file = new File("Databases/"+dbName+"/"+tableName+".txt");
      Scanner sc = new Scanner(file);
      HashMap<String, ArrayList<String>> totalColumn = new HashMap<>();
      while (sc.hasNext()) {
        String[] columnDetails = sc.nextLine().split("\\|");
          ArrayList<String> columnList = new ArrayList<>();
          for(int i = 1; i < columnDetails.length; i++) {
              columnList.add(columnDetails[i]);
          }
          totalColumn.put(columnDetails[0],columnList);
      }
      sc.close();
      return totalColumn;
    }
    return null;
  }
  public static boolean insertRow(String dbName, String tableName, HashMap<String, String> insertData) throws FileNotFoundException {

    HashMap<String, ArrayList<String>> tableData = getColumns(dbName, tableName);
    for(Map.Entry<String, ArrayList<String>> entry: tableData.entrySet()) {
      entry.getValue().add(insertData.get(entry.getKey()));
    }
    insertTableData(dbName,tableName,tableData);
    return true;
  }
  public static void insertTableData(String dbName, String tableName,HashMap<String,ArrayList<String>> tableData)
  {
    if (Context.setDbName(dbName)&&Context.isTableExist(tableName)){
      try {
        FileWriter myWriter = new FileWriter("Databases/"+dbName+"/"+tableName+".txt");

        for(Map.Entry<String, ArrayList<String>> entry: tableData.entrySet()) {
         myWriter.write(entry.getKey()+"|");
         for(int i=0;i<entry.getValue().size();i++)
         {
           myWriter.write(entry.getValue().get(i)+"|");
         }
         myWriter.write("\n");
        }
        myWriter.close();

      } catch (IOException e) {

        e.printStackTrace();
      }


    }
  }
}
