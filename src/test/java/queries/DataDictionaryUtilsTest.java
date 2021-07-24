package test.java.queries;

import main.java.queries.Column;
import main.java.queries.DataDictionaryUtils;
import main.java.queries.ForeignKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class DataDictionaryUtilsTest {
    private static String dbName  = "testdb";
    private static String tableName  = "testTable";

    @BeforeAll
    public static void init(){

        ArrayList<Column> columns = new ArrayList<>();
        Column c1 = new Column("C1","VARCHAR 30");
        c1.setPrivateKey(true);

        Column c2 = new Column("C2","TEXT");
        c2.setAllowNulls(false);
        c2.setForeignKey(new ForeignKey("C2","othertable","othercolumn"));

        Column c3 = new Column("C3","DECIMAL");

        columns.add(c1);
        columns.add(c2);
        columns.add(c3);

        try {
            DataDictionaryUtils.create(dbName,tableName,columns);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void reset(){
        File directory = new File("DataBases/"+dbName);
        File[] files = directory.listFiles();
        for (File f: files){
            f.delete();
        }
        directory.delete();
    }

    @Test
    public void tableDictionaryExistsTest(){
        assertTrue(DataDictionaryUtils.tableDictionaryExists(dbName,tableName));
    }

    @Test
    public void tableDictionaryNotExistsTest(){
        assertFalse(DataDictionaryUtils.tableDictionaryExists(dbName,"testTableDoesntExist"));
    }

    @Test
    public void getColumnsTest(){
        try {
            HashMap<String,Column> columnHashMap = DataDictionaryUtils.getColumns(dbName,tableName);
            assertTrue(columnHashMap.containsKey("C1")&&columnHashMap.containsKey("C2")&&columnHashMap.containsKey("C3"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void createTest(){
        Column C1 = new Column("colName","TEXT");
        C1.setPrivateKey(true);
        C1.setForeignKey(new ForeignKey("colName","refTable","refCol"));
        try {
            DataDictionaryUtils.create(dbName,"testTable2", Arrays.asList(C1));
            HashMap<String,Column> columnHashMap = DataDictionaryUtils.getColumns(dbName,"testTable2");
            assertTrue(DataDictionaryUtils.tableDictionaryExists(dbName,"testTable2")
                    && columnHashMap.containsKey("colName")
                    && columnHashMap.get("colName").isPrivateKey()
                    && columnHashMap.get("colName").getForeignKey().getReferencedTable().equals("refTable")
                    && columnHashMap.get("colName").getForeignKey().getReferencedColumn().equals("refCol"));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void addColumnTest(){
        try {
            DataDictionaryUtils.addColumn(dbName,tableName,new Column("colAdded","TEXT"));
            DataDictionaryUtils.addColumn(dbName,tableName,new Column("colAdded2","TEXT"));
            assertTrue(DataDictionaryUtils.getColumns(dbName,tableName).containsKey("colAdded")
                    &&DataDictionaryUtils.getColumns(dbName,tableName).containsKey("colAdded2"));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void dropDictionaryColumnTest(){
        try {
            DataDictionaryUtils.addColumn(dbName,tableName,new Column("colToBeRemoved","TEXT"));
            DataDictionaryUtils.dropDictionaryColumn(dbName,tableName,"colToBeRemoved");
            assertFalse(DataDictionaryUtils.getColumns(dbName,tableName).containsKey("colToBeRemoved"));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void dropDictionaryTableTest(){
        try {
            DataDictionaryUtils.create("testdb","testTable2",null);
            DataDictionaryUtils.dropDictionaryTable("testdb","testTable2");
            assertFalse(DataDictionaryUtils.tableDictionaryExists("testdb","testTable2"));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}
