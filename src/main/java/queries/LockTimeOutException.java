package main.java.queries;

public class LockTimeOutException extends Exception{
    public LockTimeOutException(){
        super("Unable to obtain lock");
    }
    public LockTimeOutException(String message){
        super(message);
    }
}
