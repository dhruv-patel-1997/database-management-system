package main.java.logs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class GeneralLog {
    public Logger setLogger(){
        File file = new File("Databases/logs/GeneralLog.log");
        try {
            Files.createDirectories(Paths.get("Databases/logs"));
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger logger = Logger.getLogger("QueryLog");
        FileHandler fh;
        try {
            // This block configure the logger with handler and formatter
            fh = new FileHandler("Databases/logs/GeneralLog.log",true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.info("In Query log");
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }
}
