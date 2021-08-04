package main.java.queries;

import Utilities.Context;
import Utilities.DataDictionaryUtils;
import Utilities.TableUtils;
import main.java.dataStructures.Column;
import main.java.dataStructures.ForeignKey;
import main.java.dataStructures.PrimaryKey;
import main.java.exceptions.LockTimeOutException;
import main.java.logs.GeneralLog;
import main.java.exceptions.InvalidQueryException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

public class CreateQuery {
    public boolean createTable(String tableName, LinkedHashMap<String, Column> columns, List<PrimaryKey> primaryKeys, List<ForeignKey> foreignKeys, boolean lockAfterCreate)
            throws IOException, LockTimeOutException, InvalidQueryException {
        //db must set and table can't already exist
        String dbName = Context.getDbName();

        GeneralLog generalLog=new GeneralLog();
        Logger generalLogger=generalLog.setLogger();
        LocalTime start=LocalTime.now();
        generalLogger.info("User: "+ Context.getUserName()+" At the start of create table for create query");
        try {
            generalLogger.info("Database status at the start of create query: "+ TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
        } catch (InvalidQueryException exception) {
            exception.printStackTrace();
        }
        if (dbName != null && databaseExists(dbName)){

            if (!tableExists(dbName,tableName)){
                //foreign key column, referenced table and reference column must exist
                for (ForeignKey fk : foreignKeys){
                    String fkTableName = fk.getReferencedTable();
                    String fkCol = fk.getReferencedColumn();
                    LinkedHashMap<String,Column> fkTable = DataDictionaryUtils.getColumns(dbName,fkTableName);
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
                    if (!referencedColumn.isPrimaryKey()||!DataDictionaryUtils.equalsDataType(thisDataType,referenceDataType)){
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
                        columns.get(colName).setAsPrimaryKey(true);
                    }
                }


                //Executing create table
                createTable(tableName, new ArrayList<>(columns.values()),lockAfterCreate);

                LocalTime end=LocalTime.now();
                int diff=end.getNano()-start.getNano();
                try {
                    generalLogger.info("Database status at the end of create query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
                } catch (InvalidQueryException exception) {
                    exception.printStackTrace();
                }
                generalLogger.info("User: "+Context.getUserName()+"\nAt the end of add for create query"+"\n"+"Execution Time of query: "+diff +" nanoseconds");
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
        throw new InvalidQueryException("Query was not successful");
    }

    private void createTable(String tableName,List<Column> columns, boolean lockAfterCreate) throws IOException, LockTimeOutException {
        System.out.println("creating table "+tableName);
        File table = new File(Context.getDbPath() + tableName + ".txt");
        table.createNewFile();
        //print column names| order doesn't matter
        FileWriter fw = new FileWriter(table);
        for (Column c: columns){
            fw.write(c.getColName()+"|\n");
        }
//        table.renameTo(new File(Context.getDbPath() + tableName + ".txt"));
//        table.createNewFile();
        fw.close();
        DataDictionaryUtils.create(Context.getDbName(),tableName,columns);
        if (lockAfterCreate) {
            DataDictionaryUtils.lockTable(Context.getDbName(), tableName);
        }
    }

    public boolean createDatabase(String dbName) throws InvalidQueryException {
        //database can't be null and can't be present already
        File db = new File("Databases/"+dbName+"/");
        if (!db.exists()){
            GeneralLog generalLog=new GeneralLog();
            Logger generalLogger=generalLog.setLogger();
            LocalTime start=LocalTime.now();
            generalLogger.info("User: "+ Context.getUserName()+" At the start of creating database for create query");
            if (Context.getDbName()==null){
                generalLogger.info("Database status at the start of create query: Database has not been assigned \n");
            } else {
                generalLogger.info("Database status at the start of create query: " + TableUtils.getGeneralLogTableInfo(Context.getDbName()) + "\n");
            }

            //Creating database and assigning it to use
            db.mkdir();
            Context.setDbName(dbName);

            LocalTime end=LocalTime.now();
            int diff=end.getNano()-start.getNano();
            generalLogger.info("User: "+Context.getUserName()+"\nAt the end of add for create query"+"\n"+"Execution Time of query: "+diff +" nanoseconds");
            System.out.println("creating database "+dbName);
            return true;
        } else {
            throw new InvalidQueryException("Database already exists");
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
