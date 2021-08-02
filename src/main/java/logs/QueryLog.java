package main.java.logs;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class QueryLog {

    public Logger setLogger(String dbName){
        File file = new File("Databases/" + dbName + "/logs/QueryLog.log");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger logger = Logger.getLogger("QueryLog");
            FileHandler fh;
            try {
                // This block configure the logger with handler and formatter
                fh = new FileHandler("Databases/" + dbName + "/logs/QueryLog.log",true);
                logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
            } catch (SecurityException | IOException e) {
                e.printStackTrace();
            }
        return logger;
    }

    public Logger setLogger(){
        File file = new File("Databases/logs/QueryLog.log");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger logger = Logger.getLogger("QueryLog");
        FileHandler fh;
        try {
            // This block configure the logger with handler and formatter
            fh = new FileHandler("Databases/logs/QueryLog.log",true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.info("In Query log");
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        return logger;
    }
}
