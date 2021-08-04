package test.java.queries;

import Utilities.Context;
import main.java.parsing.InvalidQueryException;
import main.java.parsing.Tokenizer;
import main.java.queries.DataDictionaryUtils;
import main.java.queries.LockTimeOutException;
import main.java.queries.QueryParser;
import main.java.queries.TableUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class QueryParserTest {
    String dbName = "parseTestDB";

    @BeforeEach
    public void init() throws IOException, LockTimeOutException, InvalidQueryException {
        Context.setUserName("1");
        QueryParser parser = new QueryParser(new Tokenizer("Create database parseTestDB;"));
        parser.parse();
        parser = new QueryParser(new Tokenizer("use parseTestDB;"));
        parser.parse();
        parser = new QueryParser(new Tokenizer(
                "CREATE TABLE Persons (\n" +
                "    PersonID int,\n" +
                "    LastName varchar(255),\n" +
                "    FirstName varchar(255),\n" +
                "    Address varchar(255),\n" +
                "    City varchar(255)\n" +
                ");"));
        parser.parse();
        parser = new QueryParser(new Tokenizer("insert into Persons values (0,\"x\",\"x\",\"x\",\"x\");"));
        parser.parse();
        //create test table and db and use db.
    }

    @AfterEach
    public void reset(){
        File file = new File("Databases/"+dbName);
        for (File f : file.listFiles()){
            f.delete();
        }
        file.delete();
    }

    @Test
    public void transactionQueryInvalidQueryFails(){

        //transaction interrupted  check original state is restored
        fail();
    }

    @Test
    public void transactionQueryLockTimeoutFails() throws LockTimeOutException, IOException, InvalidQueryException {
        // table needed by the transaction is already locked, transaction cant execute
        //check nothing has changed
        Context.incrTransactionId();
        DataDictionaryUtils.lockTable(dbName,"Persons");
        String input = "Start transaction: insert into Persons values (100,\"x\",\"x\",\"x\",\"x\");" +
                "update Persons set PersonID = 101 where PersonID = 0;" +
                "commit;";
        QueryParser parser = new QueryParser(new Tokenizer(input));
        try {
            parser.parse(); //parser will generate a new transaction ID before executing
            fail();
        } catch (LockTimeOutException e) {
            e.printStackTrace();
            HashMap<String, ArrayList<String>> columns = TableUtils.getColumns(dbName,"Persons", new ArrayList<String>(Arrays.asList("PersonID")));
            assertFalse(columns.get("PersonID").contains("101"));
            assertFalse(columns.get("PersonID").contains("100"));
            assertTrue(columns.get("PersonID").contains("0"));
        }
    }

    @Test
    public void transactionQueryWithCreateTableFails(){
        // table needed by the transaction is already locked, transaction cant execute
        //check nothing has changed
        //make sure table has been deleted
        fail();
    }

    @Test
    public void transactionQueryWithCreateDatabaseFails() throws LockTimeOutException, IOException, InvalidQueryException {
        // table needed by the transaction is already locked, transaction cant execute
        //check nothing has changed
        //make sure Database has been deleted
        fail();
    }

    @Test
    public void successfulTransaction(){
        //all tables are open, all transaction operations are performed successfully
        String input = "Start transaction: insert into Persons values (100,\"x\",\"x\",\"x\",\"x\");" +
                "update Persons set PersonID = 101 where PersonID = 100;" +
                "commit;";
        QueryParser parser = new QueryParser(new Tokenizer(input));
        try {
            parser.parse();
            HashMap<String, ArrayList<String>> columns = TableUtils.getColumns(dbName,"Persons", new ArrayList<String>(Arrays.asList("PersonID")));
            assertTrue(columns.get("PersonID").contains("101"));
            assertTrue(!columns.get("PersonID").contains("100"));
        } catch (InvalidQueryException | IOException | LockTimeOutException e) {
            e.printStackTrace();
            fail();
        }
    }

    //putting a create database or use database in transaction will not work
    // because we need to know what db the tables belong to in order to lock
}
