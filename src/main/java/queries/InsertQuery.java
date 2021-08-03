package main.java.queries;
import Utilities.Context;
import main.java.logs.GeneralLog;
import main.java.parsing.InvalidQueryException;
import main.java.parsing.Token;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Logger;

public class InsertQuery {

    public boolean insert(String tableName, List<String> cols, List<Token> vals) throws LockTimeOutException, IOException, InvalidQueryException {
        //db must set
        String dbName = Context.getDbName();
        if (dbName != null && (new File(Context.getDbPath())).isDirectory()){
            //table must exist in db
            LinkedHashMap<String,Column> destinationColumns = DataDictionaryUtils.getColumns(dbName,tableName);
            if (destinationColumns != null){
                if (cols == null || cols.isEmpty()){
                    //if no columns given then value count must equal column count in table
                    if (vals.size() != destinationColumns.size()){
                        throw new InvalidQueryException("Number of values does not match number of columns in destination table");
                    }
                }

                //iterate through columns in destination table to make sure the value can be inserted
                HashMap<String,String> insertData = new HashMap<>();

                int valueIndex = 0;
                int columnPresentInTableCount = 0;
                for (Column destination: destinationColumns.values()){
                    Token value = null;
                    if (cols != null){
                        //columns are given in query
                        for (int columnIndex = 0; columnIndex < cols.size(); columnIndex++){
                            if (destination.getColName().equalsIgnoreCase(cols.get(columnIndex))){
                                //destination column is present in query
                                //the next value to insert is in position columnIndex in vals
                                value = vals.get(columnIndex);
                                columnPresentInTableCount++;
                                break;
                            }
                        }
                        if (value == null){
                            //column exists in table but was not given a value
                            value = new Token(Token.Type.NULL," ");
                        }
                    } else {
                        //cols are not given
                        value = vals.get(valueIndex++);
                    }

                    if (value.getType() == Token.Type.NULL){
                        //no value was given for this column or the value is null
                        //check that column allows nulls
                        if (!destination.getAllowNulls()){
                            throw new InvalidQueryException("Column "+destination.getColName()+" cannot be null");
                        }
                    } else {
                        //check data type is correct for column
                        if (!DataDictionaryUtils.valueIsOfDataType(value,destination.getDataType())){
                            throw new InvalidQueryException("Invalid data type for column "+destination.getColName());
                        }
                    }

                    // if column is a primary key, cannot be a value that already exists
                    if (destination.isPrimaryKey()){
                        //check value doesn't already exist
                        ArrayList<String> columnValues = TableUtils.getColumns(Context.getDbName(),tableName,new ArrayList<String>(Arrays.asList(destination.getColName()))).get(destination.getColName());
                        if (columnValues != null && columnValues.contains(value.getStringValue())){
                            //value is already present
                            throw new InvalidQueryException("Primary key constraint fails: "+value+ "is already present in table");
                        }
                    }

                    //if column is a foreign key, value must exist in referenced table
                    if (destination.getForeignKey() != null){
                        System.out.println(destination.getColName()+" is fk with val "+value.getStringValue());
                        String refTable = destination.getForeignKey().getReferencedTable();
                        String refColumn = destination.getForeignKey().getReferencedColumn();
                        ArrayList<String> columnValues = TableUtils.getColumns(Context.getDbName(),refTable,new ArrayList<String>(Arrays.asList(refColumn))).get(refColumn);
                        if (value.getType() != Token.Type.NULL && (columnValues == null || !columnValues.contains(value.getStringValue()))){
                            //value is not present
                            throw new InvalidQueryException("Foreign key constraint fails: "+value.getStringValue()+ " not present in referenced column");
                        }
                    }
                    insertData.put(destination.getColName(),value.getStringValue());
                }

                if (cols != null && !cols.isEmpty() && columnPresentInTableCount != cols.size()){
                    throw new InvalidQueryException("Columns given are not all present in destination table");
                }
                GeneralLog generalLog=new GeneralLog();
                Logger generalLogger=generalLog.setLogger();
                LocalTime start=LocalTime.now();
                generalLogger.info("User: "+ Context.getUserName()+" At the start of alter query");
                generalLogger.info("Database status at the start of insert query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
                LocalTime end=LocalTime.now();
                int diff=end.getNano()-start.getNano();
                generalLogger.info("Database status at the end of insert query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
                generalLogger.info("User: "+Context.getUserName()+"\nAt the end of add for insert query"+"\n"+"Execution Time of query: "+diff +" nanoseconds");
                System.out.println("Inserting row");
                TableUtils.insertRow(Context.getDbName(),tableName,insertData);
                return true;

            } else {
                System.out.println("Table "+tableName+" does not exist in database "+dbName);
            }
        } else {
            if (dbName == null) {
                System.out.println("No database has been selected, please enter USE query");
            } else {
                System.out.println("Database " + dbName + " does not exist");
            }
        }
        throw new InvalidQueryException("Invalid SELECT query");
    }
}
