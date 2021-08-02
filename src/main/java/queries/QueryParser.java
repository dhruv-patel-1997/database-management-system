package main.java.queries;

import Utilities.Context;
import Utilities.TableMaker;
import main.java.parsing.InvalidQueryException;
import main.java.parsing.Token;
import main.java.parsing.Tokenizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class QueryParser {
    private Tokenizer tokenizer;

    public QueryParser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public void parse() throws InvalidQueryException, IOException, LockTimeOutException {
        // call tokenizer.next(); to get the next token
        Token token = tokenizer.next();
        if(token==null){
            return;
        }

        Token.Type tokenType = token.getType();
        String tokenValue = token.getStringValue();

        switch (tokenType){
            case USE:
                use();
                break;
            case CREATE:
                create();
                break;
            case DROP:
                drop();
                break;
            case INSERT:
                insert();
                break;
            case UPDATE:
                //validate query
                break;
            case DELETE:
                //validate query
                break;
            case ALTER:
                //validate query
                break;
            case SELECT:
                select();
                break;
            case TRUNCATE:
                truncate();
                break;
            default:
                throw new InvalidQueryException("Invalid syntax: "+tokenValue);
        }
    }

    /*
    * Compares each element in tokenTypes with the next token
    * If a mismatch occurs returns NULL
    * Otherwise returns the list of values held by each token in order.
    * */
    private ArrayList<String> matchesTokenList(List<Token.Type> tokenTypes) throws InvalidQueryException {
        Token token;
        ArrayList<String> values = new ArrayList<>();
        for (Token.Type type : tokenTypes){
            token = tokenizer.next();
            if (token == null || token.getType() != type){
                return null;
            }
            values.add(token.getStringValue());
        }
        return values;
    }

    private void use() throws InvalidQueryException {
        ArrayList<String> values;
        if ((values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER,Token.Type.SEMICOLON))) != null && tokenizer.next() == null){
            UseQuery query = new UseQuery();
            query.useDataBase(values.get(0));
            System.out.println("using database "+values.get(0));
        } else {
            throw new InvalidQueryException("Invalid syntax for USE query");
        }
    }

    private void truncate() throws InvalidQueryException{
        ArrayList<String> values;
        if ((values = matchesTokenList(Arrays.asList(Token.Type.TABLE,Token.Type.IDENTIFIER,Token.Type.SEMICOLON))) != null && tokenizer.next() == null){
            UseQuery query = new UseQuery();
            query.useDataBase(values.get(1));
            System.out.println("truncate table "+values.get(0));
        } else {
            throw new InvalidQueryException("Invalid syntax for USE query");
        }
    }

    private void drop() throws InvalidQueryException{
        Token token = tokenizer.next();
        if (token!=null && token.getType() == Token.Type.DATABASE){
            dropDatabase();
        } else if (token!=null && token.getType() == Token.Type.TABLE){
            dropTable();
        } else {
            throw new InvalidQueryException("Invalid CREATE query");
        }
    }

    private void dropDatabase() throws InvalidQueryException{
        ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER, Token.Type.SEMICOLON));
        if (values != null && tokenizer.next() == null) {
            String dbName = values.get(0);
            System.out.println("dropping db " + dbName);
            //SUCCESSFUL QUERY
            CreateQuery query = new CreateQuery();
            query.createDatabase(dbName);
        } else {
            throw new InvalidQueryException("Invalid Syntax for CREATE DATABASE query");
        }
    }

    private void dropTable() throws InvalidQueryException{
        ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER, Token.Type.SEMICOLON));
        if (values != null && tokenizer.next() == null) {
            String dbName = values.get(0);
            System.out.println("droping table " + dbName);
            //SUCCESSFUL QUERY
            CreateQuery query = new CreateQuery();
            query.createDatabase(dbName);
        } else {
            throw new InvalidQueryException("Invalid Syntax for CREATE DATABASE query");
        }
    }

    private void create() throws InvalidQueryException, IOException, LockTimeOutException {
        Token token = tokenizer.next();
        if (token!=null && token.getType() == Token.Type.DATABASE){
            createDatabase();
        } else if (token!=null && token.getType() == Token.Type.TABLE){
            createTable();
        } else {
            throw new InvalidQueryException("Invalid CREATE query");
        }
    }

    private void createDatabase() throws InvalidQueryException {
        ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER, Token.Type.SEMICOLON));
        if (values != null && tokenizer.next() == null) {
            String dbName = values.get(0);
            //SUCCESSFUL QUERY
            CreateQuery query = new CreateQuery();
            query.createDatabase(dbName);
        } else {
            throw new InvalidQueryException("Invalid Syntax for CREATE DATABASE query");
        }
    }

    private void createTable() throws InvalidQueryException, IOException, LockTimeOutException {
        Token token;
        ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER, Token.Type.OPEN));
        if (values == null) {
            throw new InvalidQueryException("Invalid syntax for CREATE TABLE query");
        }

        String tableName = values.get(0);
        LinkedHashMap<String,Column> columns = new LinkedHashMap<>();
        LinkedList<PrimaryKey> primaryKeys = new LinkedList<>();
        LinkedList<ForeignKey> foreignKeys = new LinkedList<>();

        do {
            token = tokenizer.next();
            if (token != null && token.getType() == Token.Type.IDENTIFIER) {
                //Column declaration
                Column column;
                String colName = token.getStringValue();

                token = tokenizer.next();
                if (token == null) {
                    throw new InvalidQueryException("Invalid syntax for column: "+colName);
                }

                //Get data type
                Token.Type tokenType = token.getType();

                if (tokenType == Token.Type.VARCHAR) {
                    if ((values = matchesTokenList(Arrays.asList(Token.Type.OPEN, Token.Type.INTLITERAL, Token.Type.CLOSED)))!= null) {
                        column = new Column(colName,tokenType+" "+values.get(1));
                    } else {
                        throw new InvalidQueryException("Invalid Varchar argument");
                    }
                } else if (tokenType == Token.Type.INT) {
                    column = new Column(colName,tokenType.toString());
                } else if (tokenType == Token.Type.DECIMAL) {
                    column = new Column(colName, tokenType.toString());
                } else if (tokenType == Token.Type.TEXT) {
                    column = new Column(colName,tokenType.toString());
                } else if (tokenType == Token.Type.BOOLEAN) {
                    column = new Column(colName,tokenType.toString());
                } else {
                    throw new InvalidQueryException("Invalid data type for column: "+colName);
                }
                if ((token = tokenizer.next()) != null && token.getType() == Token.Type.NOT){
                    if ((token = tokenizer.next()) != null && token.getType() == Token.Type.NULL){
                        column.setAllowNulls(false);
                        token = tokenizer.next();
                    } else {
                        throw new InvalidQueryException("Invalid syntax for column: "+colName);
                    }
                }
                //Column declaration is syntactically correct, add column to list
                if (columns.containsKey(colName)){
                    throw new InvalidQueryException("Duplicate column name: "+colName);
                }
                columns.put(colName,column);
            } else {
                //KEY declaration
                if (token != null && token.getType() == Token.Type.PRIMARY) {
                    //Primary key declaration
                    if ((values = matchesTokenList(Arrays.asList(Token.Type.KEY, Token.Type.OPEN, Token.Type.IDENTIFIER))) != null) {
                        HashSet<String> keyColumnNames = new HashSet<>();
                        keyColumnNames.add(values.get(2));
                        while ((token = tokenizer.next()) != null && token.getType() == Token.Type.COMMA){
                            if ((token = tokenizer.next()) != null && token.getType() == Token.Type.IDENTIFIER){
                                keyColumnNames.add(token.getStringValue());
                            } else {
                                throw new InvalidQueryException("Invalid primary key syntax");
                            }
                        }
                        if (token != null && token.getType() == Token.Type.CLOSED){
                            //key declaration is syntactically correct, add to list
                            primaryKeys.add(new PrimaryKey(keyColumnNames));
                        } else {
                            throw new InvalidQueryException("Invalid primary key syntax");
                        }
                    } else {
                        throw new InvalidQueryException("Invalid primary key syntax");
                    }
                } else if (token != null && token.getType() == Token.Type.FOREIGN) {
                    //Foreign key declaration
                    if ((values = matchesTokenList(Arrays.asList(Token.Type.KEY, Token.Type.OPEN, Token.Type.IDENTIFIER,
                            Token.Type.CLOSED, Token.Type.REFERENCES, Token.Type.IDENTIFIER,
                            Token.Type.OPEN, Token.Type.IDENTIFIER, Token.Type.CLOSED))) != null) {
                        //key declaration is syntactically correct, add to list
                        foreignKeys.add(new ForeignKey(values.get(2), values.get(5), values.get(7)));
                    } else {
                        throw new InvalidQueryException("Invalid foreign key syntax");                       }
                } else {
                    throw new InvalidQueryException("Invalid syntax");
                }
                token = tokenizer.next();
            }
        } while (token != null && token.getType() == Token.Type.COMMA);

        if (columns.isEmpty()) {
            throw new InvalidQueryException("Invalid syntax, at least one column is required in CREATE TABLE query");
        }

        if (token != null && token.getType() == Token.Type.CLOSED
                && (token = tokenizer.next()) != null && token.getType() == Token.Type.SEMICOLON && tokenizer.next() == null) {
            //SUCCESSFUL QUERY

            CreateQuery query = new CreateQuery();
            query.createTable(tableName, columns, primaryKeys, foreignKeys);
        } else {
            throw new InvalidQueryException("Invalid syntax");
        }
    }


    private void insert() throws InvalidQueryException, LockTimeOutException {
        Token token;
        ArrayList<String> stringValues = matchesTokenList(Arrays.asList(Token.Type.INTO, Token.Type.IDENTIFIER));
        if (stringValues == null) {
            throw new InvalidQueryException("Invalid syntax");
        }

        String tableName = stringValues.get(1);
        LinkedList<String> cols = new LinkedList<>();
        LinkedList<Token> vals = new LinkedList<>();

        if ((token = tokenizer.next()) != null && token.getType() == Token.Type.OPEN){
            //parse list of columns
            token = tokenizer.next();
            while (token != null && token.getType() == Token.Type.IDENTIFIER){
                cols.add(token.getStringValue());
                if ((token = tokenizer.next()) == null){
                    throw new InvalidQueryException("Invalid syntax");
                } else if (token.getType() != Token.Type.COMMA){
                    if (token.getType()!= Token.Type.CLOSED) {
                        throw new InvalidQueryException("Invalid syntax");
                    }
                    //end of column list
                    break;
                }
                token = tokenizer.next();
            }
            if (cols.isEmpty()){
                throw new InvalidQueryException("Invalid syntax");
            }
            token = tokenizer.next();
        }

        if (token != null && token.getType() == Token.Type.VALUES
                && (token = tokenizer.next()) != null && token.getType() == Token.Type.OPEN){
            //parse list of values
            token = tokenizer.next();
            Token.Type tokenType;
            while (token != null && ((tokenType = token.getType()) == Token.Type.INTLITERAL
                    || tokenType == Token.Type.DECIMALLITERAL || tokenType == Token.Type.STRING
                    || tokenType == Token.Type.BOOLEANLITERAL || tokenType == Token.Type.NULL)){
                vals.add(token);
                if ((token = tokenizer.next()) == null){
                    throw new InvalidQueryException("Invalid syntax in values list");
                }
                if (token.getType() != Token.Type.COMMA){
                    if (token.getType() != Token.Type.CLOSED) {
                        throw new InvalidQueryException("Invalid syntax at: "+token.getType());
                    }
                    //end of values list
                    break;
                }
                token = tokenizer.next();
            }
            if (vals.isEmpty()){
                throw new InvalidQueryException("Invalid syntax, no values were given");
            }

            //If columns listed, make sure column count matches values count
            if (cols.isEmpty() || cols.size() == vals.size()){
                if ((token = tokenizer.next()) != null && token.getType() == Token.Type.SEMICOLON && tokenizer.next() == null){
                    //SUCCESSFUL QUERY
                    InsertQuery query = new InsertQuery();
                    System.out.println("Insert into table "+tableName);
                    query.insert(tableName, cols,vals);
                } else {
                    throw new InvalidQueryException("Invalid syntax");
                }
            } else {
                throw new InvalidQueryException("Invalid syntax, number of columns does not match number of values");
            }
        } else {
            throw new InvalidQueryException("Invalid syntax");
        }
    }

    private void select() throws InvalidQueryException, FileNotFoundException {
        Token token;
        token = tokenizer.next();
        if(token != null && token.getType()==Token.Type.STAR) {
            ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.FROM, Token.Type.IDENTIFIER, Token.Type.SEMICOLON));
            if(values == null) {
                throw new InvalidQueryException("Invalid syntax for SELECT TABLE query");
            }
            String tableName = values.get(1);
            if(Context.isTableExist(tableName)) {
                showTable(TableUtils.getColumns(Context.getDbName(), tableName));
            }
            else {
                throw new InvalidQueryException("No table exist with given name: "+tableName);
            }
        }else if(token!=null && token.getType()==Token.Type.IDENTIFIER)
        {
            ArrayList<String> columns = new ArrayList<>();
            while(token!=null && token.getType()!=Token.Type.FROM && token.getType()==Token.Type.IDENTIFIER)
            {
                columns.add(token.getStringValue());
                token=tokenizer.next();
                if(token.getType()==Token.Type.FROM)
                    break;
                if(token.getType()!=Token.Type.COMMA)
                    throw new InvalidQueryException("Invalid syntax for SELECT TABLE query");
                token=tokenizer.next();
            }
            ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER, Token.Type.SEMICOLON));
            if(values == null) {
                throw new InvalidQueryException("Invalid syntax for SELECT TABLE query");
            }
            String tableName = values.get(0);
            if(Context.isTableExist(tableName)) {
                showTable(TableUtils.getColumns(Context.getDbName(), tableName,columns));
            }
            else {
                throw new InvalidQueryException("No table exist with given name: "+tableName);
            }

        }else
        {
            throw new InvalidQueryException("Invalid syntax for SELECT TABLE query");
        }
    }

    private void showTable(HashMap<String,ArrayList<String>> tableData)
    {
        TableMaker tm = new TableMaker();
        tm.printTable(tableData);
    }
}
