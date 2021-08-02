package main.java.queries;
import main.java.Context;
import main.java.parsing.Token;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class InsertQuery {

    public boolean insert(String tableName, List<String> cols, List<Token> vals) throws LockTimeOutException, FileNotFoundException {
        //db must set
        String dbName = Context.getDbName();
        if (dbName != null && (new File(Context.getDbPath())).isDirectory()){
            //table must exist in db
            LinkedHashMap<String,Column> destinationColumns = DataDictionaryUtils.getColumns(dbName,tableName);
            if (destinationColumns != null){
                if (cols == null || cols.isEmpty()){
                    //if no columns given then value count must equal column count in table
                    if (vals.size() != destinationColumns.size()){
                        System.out.println("Number of values does not match number of columns in destination table");
                        return false;
                    }
                }

                //iterate through columns in destination table to make sure the value can be inserted
                List<Token> valuesToInsert = new ArrayList<>();

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
                            System.out.println("Column "+destination.getColName()+" cannot be null");
                            return false;
                        }
                    } else {
                        //check data type is correct for column
                        if (!DataDictionaryUtils.valueIsOfDataType(value,destination.getDataType())){
                            System.out.println("Invalid data type for column "+destination.getColName());
                            return false;
                        }
                    }

                    // if column is a primary key, cannot be a value that already exists
                    if (destination.isPrimaryKey()){
                        //check value doesn't already exist
                        ArrayList<String> columnValues = TableUtils.getColumns(Context.getDbName(),tableName,new ArrayList<String>(Arrays.asList(destination.getColName()))).get(destination.getColName());
                        if (columnValues != null && columnValues.contains(value.getStringValue())){
                            //value is already present
                            System.out.println("Primary key constraint fails: "+value+ "is already present in table");
                            return false;
                        }
                    }

                    //if column is a foreign key, value must exist in referenced table
                    if (destination.getForeignKey() != null){
                        String refTable = destination.getForeignKey().getReferencedTable();
                        String refColumn = destination.getForeignKey().getReferencedColumn();
                        ArrayList<String> columnValues = TableUtils.getColumns(Context.getDbName(),refTable,new ArrayList<String>(Arrays.asList(refColumn))).get(refColumn);
                        if (columnValues != null && !columnValues.contains(value.getStringValue())){
                            //value is not present
                            System.out.println("Foreign key constraint fails: "+value+ "not present in referenced column");
                            return false;
                        }
                    }
                }

                if (cols != null && !cols.isEmpty() && columnPresentInTableCount != cols.size()){
                    System.out.println("Columns given are not all present in destination table");
                    return false;
                }

                System.out.println("Inserting row");
                //TODO:
                //add logging
                /*Execution logic
                    lock table
                    get table file
                    append
                    unlock*/

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
        return false;
    }
}
