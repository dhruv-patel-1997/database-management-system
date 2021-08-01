package main.java.queries;

import java.io.File;

public class DropQuery {
    public boolean dropDatabase(String dbName){
        String directoryPath = "Databases/" + dbName;
        File file = new File(directoryPath);

        if(file.exists()){
            return deleteDirectory(file);
        }
        else{
            return false;
        }

    }

    public boolean dropTable(String dbName,String tableName){
        String directoryPath = "Databases/" + dbName + "/"+tableName+".txt";
        File file = new File(directoryPath);

        if(file.exists()){
            return file.delete();
        }
        else{
            return false;
        }
    }

    public static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return(directory.delete());
    }
}
