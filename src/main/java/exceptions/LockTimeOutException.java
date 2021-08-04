package main.java.exceptions;

import Utilities.Context;
import main.java.logs.EventLog;

public class LockTimeOutException extends Exception{
    public LockTimeOutException(){
        super("Unable to obtain lock");
        EventLog eventLog=new EventLog();
        eventLog.setLogger().info("Query Crashed for User: " + Context.getUserName() + ".\nReason: " +"Unable to obtain lock");
    }
    public LockTimeOutException(String message){
        super(message);
        EventLog eventLog=new EventLog();
        eventLog.setLogger().info("Query Crashed for User: " + Context.getUserName() + ".\nReason: " +message);
    }
}
