package test.java.queries;

import Utilities.Context;
import main.java.parsing.InvalidQueryException;
import main.java.parsing.Token;
import main.java.queries.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class InsertQueryTest {
    private static final InsertQuery query = new InsertQuery();
    private static final String dbName = "testInsertDb";
    private static final String tableName = "testInsertTable";
    private static final List<String> colNames = Arrays.asList("col1", "col2", "col3", "col4", "col5");
    private static final List<Token> sampleVals = Arrays.asList(new Token(Token.Type.STRING,"\"val\""),
            new Token(Token.Type.INTLITERAL,"10"),
            new Token(Token.Type.DECIMALLITERAL,"1.0"),
            new Token(Token.Type.BOOLEANLITERAL,"TRUE"),
            new Token(Token.Type.STRING,"\"val\""));

    @BeforeEach
    public void init() throws IOException, LockTimeOutException, InvalidQueryException {
        new CreateQuery().createDatabase(dbName);
        Context.setDbName(dbName);

        LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
        cols.put("col1",new Column("col1","TEXT"));
        cols.get("col1").setAllowNulls(false);
        cols.put("col2",new Column("col2","INT"));
        cols.put("col3",new Column("col3","DECIMAL"));
        cols.put("col4",new Column("col4","BOOLEAN"));
        cols.put("col5",new Column("col5","VARCHAR 10"));

        LinkedHashMap<String, Column> refCols = new LinkedHashMap<>();
        refCols.put("col1",new Column("col1","VARCHAR 10"));

        //create reference table
        List<PrimaryKey> refPrimaryKeys = new ArrayList<>();
        refPrimaryKeys.add(new PrimaryKey(new HashSet<String>(Collections.singleton("col1"))));
        String refTableName = "testInsertRefTable";
        new CreateQuery().createTable(refTableName,refCols,refPrimaryKeys,new ArrayList<>(),false);
        query.insert(refTableName,new ArrayList<String>(Collections.singleton("col1")),sampleVals);

        //create insert table
        List<ForeignKey> foreignKeys = new ArrayList<>();
        foreignKeys.add(new ForeignKey("col5", refTableName,"col1"));
        List<PrimaryKey> primaryKeys = new ArrayList<>();
        primaryKeys.add(new PrimaryKey(new HashSet<String>(Collections.singleton("col2"))));
        new CreateQuery().createTable(tableName,cols,primaryKeys,foreignKeys,false);
    }

    @AfterEach
    public void reset(){
        File file = new File("Databases/"+dbName);
        for(File f: file.listFiles()){
            f.delete();
        }
        file.delete();
    }

    @Test
    public void insertNoColsSuccess() throws IOException, LockTimeOutException, InvalidQueryException {
        assertTrue(query.insert(tableName,null,sampleVals));
        HashMap<String, ArrayList<String>> values = TableUtils.getColumns(dbName,tableName);
        assertTrue(values!=null && !values.isEmpty());
    }

    @Test
    public void insertWithColsSuccess() throws IOException, LockTimeOutException, InvalidQueryException {
        assertTrue(query.insert(tableName,colNames,sampleVals));
    }

    @Test
    public void invalidTable() throws IOException, LockTimeOutException {
        try {
            query.insert("notatable",null,sampleVals);
            fail();
        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void noColsTooManyValues() throws IOException, LockTimeOutException {
        List<Token> tooManyVals = new ArrayList<>(sampleVals);
        tooManyVals.add(new Token(Token.Type.NULL,"NULL"));
        try {
            query.insert(tableName,null,tooManyVals);
            fail();
        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void noColsNotEnoughValues() throws IOException, LockTimeOutException {
        List<Token> notEnoughVals = new ArrayList<>(sampleVals);
        notEnoughVals.remove(0);
        try {
            query.insert(tableName,null,notEnoughVals);
            fail();
        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void noValueGivenColDoesNotAllowNulls() throws IOException, LockTimeOutException {
        List<String> cols = Arrays.asList("col2");
        List<Token> vals = new ArrayList<>();
        vals.add(new Token(Token.Type.INTLITERAL,"10"));
        try {
            query.insert(tableName,cols,vals);
            fail();
        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void noValueGivenColAllowsNulls() throws IOException, LockTimeOutException, InvalidQueryException {
        List<String> cols = Arrays.asList("col1","col2");
        List<Token> vals = new ArrayList<>();
        vals.add(new Token(Token.Type.STRING,"\"val\""));
        vals.add(new Token(Token.Type.INTLITERAL,"10"));
        assertTrue(query.insert(tableName,cols,vals));
    }

    @Test
    public void insertNullColAllowsNulls() throws IOException, LockTimeOutException, InvalidQueryException {
        List<String> cols = Arrays.asList("col1","col2","col3");
        List<Token> vals = new ArrayList<>();
        vals.add(new Token(Token.Type.STRING,"\"val\""));
        vals.add(new Token(Token.Type.INTLITERAL,"10"));
        vals.add(new Token(Token.Type.NULL,"NULL"));
        assertTrue(query.insert(tableName,cols,vals));
    }

    @Test
    public void insertNullColDoesntAllowsNulls() throws IOException, LockTimeOutException {
        List<String> cols = Arrays.asList("col1","col2");
        List<Token> vals = new ArrayList<>();
        vals.add(new Token(Token.Type.NULL,"NULL"));
        vals.add(new Token(Token.Type.INTLITERAL,"10"));
        try {
            query.insert(tableName,cols,vals);
            fail();
        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void invalidDataType() throws IOException, LockTimeOutException {
        List<String> cols = Arrays.asList("col1","col2");
        List<Token> vals = new ArrayList<>();
        vals.add(new Token(Token.Type.STRING,"\"val\""));
        vals.add(new Token(Token.Type.STRING,"not a number"));
        try {
            assertFalse(query.insert(tableName,cols,vals));
            fail();
        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void primaryKeyValuesAlreadyPresent() throws IOException, LockTimeOutException {

        try {
            query.insert(tableName,colNames,sampleVals);
            query.insert(tableName,colNames,sampleVals);
            fail();
        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void primaryKeyValueNotAlreadyPresent() throws IOException, LockTimeOutException, InvalidQueryException {
        assertTrue(query.insert(tableName,colNames,sampleVals));
    }

    @Test
    public void primaryKeyValueIsNull() throws IOException, LockTimeOutException {
        List<String> cols = Collections.singletonList("col1");
        List<Token> vals = new ArrayList<>();
        vals.add(new Token(Token.Type.STRING,"\"val\""));
        try {
            query.insert(tableName,cols,vals);
            fail();
        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void foreignKeyValueNotInRefColumn() throws IOException, LockTimeOutException {
        List<String> cols = Arrays.asList("col1","col2","col5");
        List<Token> vals = new ArrayList<>();
        vals.add(new Token(Token.Type.STRING,"\"val\""));
        vals.add(new Token(Token.Type.INTLITERAL,"10"));
        vals.add(new Token(Token.Type.STRING,"\"not present\""));
        try {
            query.insert(tableName,cols,vals);
            fail();
        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void foreignKeyValueIsInRefColumn() throws IOException, LockTimeOutException, InvalidQueryException {
        List<String> cols = Arrays.asList("col1","col2","col5");
        List<Token> vals = new ArrayList<>();
        vals.add(new Token(Token.Type.STRING,"\"val\""));
        vals.add(new Token(Token.Type.INTLITERAL,"10"));
        vals.add(new Token(Token.Type.STRING,"\"val\""));
        assertTrue(query.insert(tableName,cols,vals));
        System.out.println("break");
    }
}
