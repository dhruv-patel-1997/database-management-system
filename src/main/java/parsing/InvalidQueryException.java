package main.java.parsing;

import Utilities.Context;
import main.java.logs.EventLog;

public class InvalidQueryException extends Exception {
    public InvalidQueryException(String message){
        super(message);
        EventLog eventLog=new EventLog();
        eventLog.setLogger().info("Query Crashed for User: " + Context.getUserName() + ".\nReason: " +message);
    }
}
