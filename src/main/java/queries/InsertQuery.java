package main.java.queries;
import main.java.parsing.Token;
import java.util.List;

public class InsertQuery {

    public void insert(String tableName, List<String> cols, List<Token> vals) {
        /*TODO: check against data dictionary
                    (db is set, table exists, cols exist in table, if no cols, value count matches that of table,
                    data types are correct for columns, extra check for varchar, and if there arent values for all columns, check that they are allowed to be null)
                    if column is a primary key, can be a value that already exists???
                    Execution logic
                    lock table
                    get table file
                    append
                    unlock
       */
    }
}
