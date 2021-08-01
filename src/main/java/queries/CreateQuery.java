package main.java.queries;

import main.java.Context;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateQuery {
    public boolean createTable(String tableName, HashMap<String,Column> columns, List<PrimaryKey> primaryKeys, List<ForeignKey> foreignKeys)
            throws IOException, LockTimeOutException {
        //db must set and table can't already exist
        String dbName = Context.getDbName();
        String message;
        if (dbName != null && databaseExists(dbName)){
            if (!tableExists(dbName,tableName)){
                //foreign key column, referenced table and reference column must exist
                for (ForeignKey fk : foreignKeys){
                    String fkTableName = fk.getReferencedTable();
                    String fkCol = fk.getReferencedColumn();
                    HashMap<String,Column> fkTable = DataDictionaryUtils.getColumns(dbName,fkTableName);
                    if (!columns.containsKey(fk.getColname())
                            || fkTable == null
                            || !fkTable.containsKey(fkCol)){
                        //foreign key doesn't exist
                        System.out.println("foreign key constraint fails");
                        return false;
                    }
                    //referenced column must be primary key and of same data type
                    Column referencedColumn = fkTable.get(fkCol);
                    String referenceDataType = referencedColumn.getDataType();
                    String thisDataType = columns.get(fk.getColname()).getDataType();
                    if (!referencedColumn.isPrivateKey()||!equalsDataType(thisDataType,referenceDataType)){
                        System.out.println("foreign key constraint fails");
                        return false;
                    }
                    //add foreign key to column
                    columns.get(fk.getColname()).setForeignKey(fk);
                }
                //column names in primary keys must be present in columns
                for (PrimaryKey pk : primaryKeys){
                    for (String colName : pk.getColumnNames()){
                        if (!columns.containsKey(colName)){
                            System.out.println("Primary key column "+colName+" is not declared");
                            return false;
                        }
                        //add primary key to column
                        columns.get(colName).setAsPrivateKey(true);
                    }
                }

                //Query can be executed!
                createTable(tableName, new ArrayList<>(columns.values()));
                return true;
            } else {
                System.out.println("Table already exists in database "+dbName);
            }
        } else {
            if (dbName == null){
                System.out.println("No database has been selected, please enter USE query");
            } else {
                System.out.println("Database "+dbName+" does not exist");
            }
        }
        return false;
    }

    private boolean equalsDataType(String thisDataType, String referenceDataType) {
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

    private void createTable(String tableName,List<Column> columns) throws IOException, LockTimeOutException {
        System.out.println("creating table "+tableName);
        File table = new File(Context.getDbPath()+tableName+".txt");
        table.createNewFile();
        DataDictionaryUtils.create(Context.getDbName(),tableName,columns);
    }

    public boolean createDatabase(String dbName) {
        //database can't be null and can't be present already
        File db = new File("Databases/"+dbName+"/");
        if (!db.exists()){
            System.out.println("creating database "+dbName);
            db.mkdir();
            return true;
        } else {
            System.out.println("Database already exists");
            return false;
        }
    }

    private boolean databaseExists(String dbName){
        File dir = new File("Databases/"+dbName);
        return dir.isDirectory();
    }

    private boolean tableExists(String dbName, String tableName){
        return DataDictionaryUtils.tableDictionaryExists(dbName,tableName);
    }
}
