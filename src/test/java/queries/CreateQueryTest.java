package test.java.queries;

import main.java.Context;
import main.java.parsing.InvalidQueryException;
import main.java.queries.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class CreateQueryTest {
    private static String dbName = "createTestDB";
    private static String tbname = "createTestTable";
    private static CreateQuery cq = new CreateQuery();

    @BeforeAll
    public static void init() throws IOException {
        File db = new File("Databases/createTestDB");
        db.mkdir();
        Context.setDbName("createTestDB");
        DataDictionaryUtils.create(dbName,tbname,null);
    }

    @AfterAll
    public static void reset(){
        File db = new File("Databases/createTestDB");
        File[] tables = db.listFiles();
        for (File t : tables){
            t.delete();
        }
        db.delete();
    }

    @Test
    public void createDatabaseAlreadyExistsTest(){
        assertFalse(cq.createDatabase(dbName));
    }

    @Test
    public void createDatabaseTest() throws InvalidQueryException {
        cq.createDatabase("createTestDB2");
        File db = new File("Databases/createTestDB2");
        assertTrue(db.exists());
        db.delete();
    }

    @Test
    public void createTableTest() throws IOException, LockTimeOutException {
        LinkedHashMap<String, Column> columns = new LinkedHashMap<>();
        ArrayList<ForeignKey> foreignKeys = new ArrayList<>();
        ArrayList<PrimaryKey> primaryKeys = new ArrayList<>();

        cq.createTable("table2",columns,primaryKeys,foreignKeys);
        assertTrue(DataDictionaryUtils.tableDictionaryExists(dbName,"table2")
                && DataDictionaryUtils.getColumns(dbName,"table2")!=null);
    }

    @Test
    public void createTableAlreadyExistsTest() throws IOException, LockTimeOutException {
        assertFalse(cq.createTable(tbname, null, null, null));
    }
}
