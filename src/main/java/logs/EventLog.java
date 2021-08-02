package main.java.logs;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class EventLog {
    public Logger setLogger(){
        File file = new File("Databases/logs/EventLog.log");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger logger = Logger.getLogger("EventLog");
        FileHandler fh;
        try {
            // This block configure the logger with handler and formatter
            fh = new FileHandler("Databases/logs/EventLog.log",true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }
}
