package main.java.queries;

import main.java.Context;

import java.io.*;
import java.util.*;

public class TableUtils {
  /* public static boolean tableExists(String dbName, String tableName){
     String fileName = "Databases/"+dbName+"/"+tableName+".txt";
     File file = new File (fileName);
     return file.exists();
   }*/
  public static HashMap<String, ArrayList<String>> getColumns(String dbName, String tableName, ArrayList<String> columns) throws FileNotFoundException {
    if (Context.setDbName(dbName) && Context.isTableExist(tableName)) {
      File file = new File("Databases/" + dbName + "/" + tableName + ".txt");
      Scanner sc = new Scanner(file);
      HashMap<String, ArrayList<String>> totalColumn = new HashMap<>();

      while (sc.hasNext()) {
        String[] columnDetails = sc.nextLine().split("\\|");
        if (columns.contains(columnDetails[0])) {
          ArrayList<String> columnList = new ArrayList<>();
          for (int i = 1; i < columnDetails.length; i++) {
            columnList.add(columnDetails[i]);
          }
          totalColumn.put(columnDetails[0], columnList);
        }
      }
      sc.close();
      return totalColumn;
    }
    return null;
  }

  public static HashMap<String, ArrayList<String>> getColumns(String dbName, String tableName) throws FileNotFoundException {
    if (Context.setDbName(dbName) && Context.isTableExist(tableName)) {
      File file = new File("Databases/" + dbName + "/" + tableName + ".txt");
      Scanner sc = new Scanner(file);
      HashMap<String, ArrayList<String>> totalColumn = new HashMap<>();
      while (sc.hasNext()) {
        String[] columnDetails = sc.nextLine().split("\\|");
        ArrayList<String> columnList = new ArrayList<>();
        for (int i = 1; i < columnDetails.length; i++) {
          columnList.add(columnDetails[i]);
        }
        totalColumn.put(columnDetails[0], columnList);
      }
      sc.close();
      return totalColumn;
    }
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
          return totalColumn.get(key).get(0).length();
        }
      }
      return 0;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return 0;
    }
  }

  public static ArrayList<String> getTableInDb(String dbName){
    File folder = new File("Databases/" + dbName);
    return listFilesForFolder(folder);
  }

  public static HashMap<String,Integer> getGeneralLogTableInfo(String dbName){
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
    if(folder.listFiles().length!=0){
      for (final File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
          listFilesForFolder(fileEntry);
        } else {
          files.add(fileEntry.getName());
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
    int rows=getRows(Context.getDbName(),tableName);
    StringBuilder sbf = new StringBuilder("");
    sbf.append(colName+"|");
    for(int i=0;i<rows-1;i++){
      sbf.append("|");
    }
    FileWriter myWriter = new FileWriter("Databases/" + Context.getDbName() + "/" + tableName + ".txt");

      myWriter.write(sbf.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
