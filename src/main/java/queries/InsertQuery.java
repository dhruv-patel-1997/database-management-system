package main.java.queries;

import main.java.Context;
import main.java.parsing.Token;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

public class InsertQuery {

    public boolean insert(String tableName, List<String> cols, List<Token> vals) throws LockTimeOutException {
        /*TODO: check against data dictionary
                    data types are correct for columns, extra check for varchar
                    if column is a primary key, can be a value that already exists???

                    Execution logic
                    lock table
                    get table file
                    append
                    unlock
       */
        //db must set

        String dbName = Context.getDbName();
        if (dbName != null && (new File(Context.getDbPath())).isDirectory()){
            //table must exist in db
            if (DataDictionaryUtils.tableDictionaryExists(dbName,tableName)){
                LinkedHashMap<String,Column> destinationColumns = DataDictionaryUtils.getColumns(dbName,tableName);
                if (cols == null || cols.isEmpty()){
                    //if no columns given then value count must equal column count in table
                    if (destinationColumns == null || vals.size() != destinationColumns.size()){
                        System.out.println("Number of values does not match number of columns in destination table");
                        return false;
                    } else {
                        //check that values are the correct data types for columns
                        int i = 0;
                        for (Column col: destinationColumns.values()){
                            Token value = vals.get(i++);
                            if (!DataDictionaryUtils.valueIsOfDataType(value, col.getDataType())){
                                System.out.println("Invalid dataType for column "+col.getColName());
                                return false;
                            }
                        }
                    }
                } else {
                    //if columns given they must be present in table
                    //have already checked in parser that column count given is equal to value count
                    int i = 0;
                    for (String colName: cols){
                        if (destinationColumns == null || !destinationColumns.containsKey(colName)){
                            System.out.println("Column "+colName+" is not present in destination table");
                            return false;
                        } else {
                            //check that values are the correct data types for columns
                            String datatype = destinationColumns.get(colName).getDataType();
                            Token value = vals.get(i++);
                            if (!DataDictionaryUtils.valueIsOfDataType(value,datatype)){
                                System.out.println("Invalid dataType for column "+colName);
                                return false;
                            }
                        }
                    }
                    //organize??
                    //columns in table that are not given in query must allow nulls
                    i = 0;
                    for (Column c: destinationColumns.values()){
                        if (cols.get(i).equals(c.getColName())){
                            i++;
                        } else {
                            if (!c.getAllowNulls()){
                                System.out.println("Column "+c.getColName()+" does not allow nulls");
                                return false;
                            }
                        }
                    }
                }

                // if column is a primary key, cannot be a value that already exists
                for (Column c: destinationColumns.values()){}


                /*Execution logic
                    lock table
                    get table file
                    append
                    unlock*/

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
