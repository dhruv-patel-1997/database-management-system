package main.java.queries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class DataDictionaryUtils {

    private DataDictionaryUtils(){}

    public static boolean tableDictionaryExists(String dbName, String tableName){
        String fileName = "Databases/"+dbName+"/dd_"+tableName+".txt";
        File file = new File (fileName);
        return file.exists();
    }

    public static HashMap<String,Column> getColumns(String dbName, String tableName) throws FileNotFoundException {
        if (tableDictionaryExists(dbName, tableName)){
            File file = new File("Databases/"+dbName+"/dd_"+tableName+".txt");
            Scanner sc = new Scanner(file);
            HashMap<String,Column> columns = new HashMap<>();

            sc.nextLine();
            while (sc.hasNext()){
                String[] columnDetails = sc.nextLine().split("\\|");
                String colName = columnDetails[0];
                String dataType = columnDetails[1];
                boolean allowNull = Boolean.parseBoolean(columnDetails[2]);
                boolean pk = Boolean.parseBoolean(columnDetails[3]);

                String fk = null;
                if (columnDetails.length>4) {
                    fk = columnDetails[4];
                }

                Column column = new Column(colName,dataType);
                column.setAllowNulls(allowNull);
                column.setPrivateKey(pk);

                if (fk != null){
                    String[] fkDetails = fk.split(" ");
                    ForeignKey key = new ForeignKey(colName,fkDetails[0],fkDetails[1]);
                    column.setForeignKey(key);
                }

                columns.put(colName,column);
            }
            sc.close();
            return columns;
        }
        return null;
    }

    public static void create(String dbName, String tableName, List<Column> columns) throws IOException {
        //get file or make new
        File dbPath = new File("Databases/" + dbName);
        dbPath.mkdir();

        String fileName = "Databases/" + dbName +"/tempDD.txt";
        File file = new File (fileName);

        FileWriter fileWriter = new FileWriter(fileName, false);

        fileWriter.write("[unlocked]\n");
        if (columns != null) {
            for (Column column : columns) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(column.getColName()).append("|");
                stringBuilder.append(column.getDataType()).append("|");
                stringBuilder.append(column.getAllowNulls()).append("|");
                stringBuilder.append(column.isPrivateKey()).append("|");

                ForeignKey fk = column.getForeignKey();
                if (fk != null) {
                    stringBuilder.append(fk.getReferencedTable()).append(" ").append(fk.getReferencedColumn());
                }
                fileWriter.write(stringBuilder.toString() + "\n");
            }
        }
        fileWriter.close();
        file.renameTo(new File("Databases/" + dbName + "/dd_" + tableName + ".txt"));
    }

    public static void addColumn(String dbName, String tableName, Column column) throws IOException {
        String fileName = "Databases/"+dbName+"/dd_"+tableName+".txt";
        File file = new File (fileName);
        if (file.exists()) {
            FileWriter fileWriter = new FileWriter(fileName, true);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(column.getColName()).append("|");
            stringBuilder.append(column.getDataType()).append("|");
            stringBuilder.append(column.getAllowNulls()).append("|");
            stringBuilder.append(column.isPrivateKey()).append("|");

            ForeignKey fk = column.getForeignKey();
            if (fk != null){
                stringBuilder.append(fk.getReferencedTable()).append(" ").append(fk.getReferencedColumn());
            }
            stringBuilder.append("\n");
            fileWriter.write(stringBuilder.toString());
            fileWriter.close();
        } else {
            throw new FileNotFoundException();
        }
    }

    public static void dropDictionaryColumn(String dbName, String tableName, String colName) throws IOException {
        String fileName = "Databases/"+dbName+"/dd_"+tableName+".txt";
        File file = new File (fileName);
        if (file.exists()) {
            Scanner sc = new Scanner(file);
            StringBuilder stringBuilder = new StringBuilder();
            String row;
            while(sc.hasNext()){
                row = sc.nextLine();
                if (!row.split("\\|")[0].matches(colName)){
                    stringBuilder.append(row).append("\n");
                }
            }
            sc.close();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.append(stringBuilder.toString());
            fileWriter.close();
        }
    }

    public static void dropDictionaryTable(String dbName, String tableName){
        String fileName = "Databases/"+dbName+"/dd_"+tableName+".txt";
        File file = new File (fileName);
        if (file.exists()) {
            file.delete();
        }
    }
}
