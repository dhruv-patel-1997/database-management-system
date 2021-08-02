package main.java.queries;

import Utilities.Context;

public class UseQuery {
    public boolean useDataBase(String dbName) {
        if(Context.setDbName(dbName))
            return true;
        else
            return false;
    }
}
