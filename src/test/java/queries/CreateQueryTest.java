package test.java.queries;

import Utilities.Context;
import Utilities.DataDictionaryUtils;
import main.java.dataStructures.Column;
import main.java.dataStructures.ForeignKey;
import main.java.dataStructures.PrimaryKey;
import main.java.exceptions.LockTimeOutException;
import main.java.exceptions.InvalidQueryException;
import main.java.queries.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    public void createDatabaseAlreadyExistsTest() {
        try {
            cq.createDatabase(dbName);
            fail();
        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createDatabaseTest() throws InvalidQueryException {
        cq.createDatabase("createTestDB2");
        File db = new File("Databases/createTestDB2");
        assertTrue(db.exists());
        db.delete();
    }

    @Test
    public void createTableTest() throws IOException, LockTimeOutException, InvalidQueryException {
        LinkedHashMap<String, Column> columns = new LinkedHashMap<>();
        ArrayList<ForeignKey> foreignKeys = new ArrayList<>();
        ArrayList<PrimaryKey> primaryKeys = new ArrayList<>();

        cq.createTable("table2",columns,primaryKeys,foreignKeys,false);
        assertTrue(DataDictionaryUtils.tableDictionaryExists(dbName,"table2")
                && DataDictionaryUtils.getColumns(dbName,"table2")!=null);
    }

    @Test
    public void createTableAlreadyExistsTest() throws IOException, LockTimeOutException {
        try {
            cq.createTable(tbname, null, null, null, false);
            fail();
        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }
}
