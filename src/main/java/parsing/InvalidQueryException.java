package main.java.parsing;

import main.java.Context;
import main.java.logs.EventLog;

import java.util.ConcurrentModificationException;

public class InvalidQueryException extends Exception {
    public InvalidQueryException(String message){
        super(message);
        EventLog eventLog=new EventLog();
        eventLog.setLogger().info("Query Crashed for User: " + Context.getUserName() + ".\nReason: " +message);
    }
}
