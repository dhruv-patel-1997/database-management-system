import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class QueryParser {
    private Tokenizer tokenizer;

    public QueryParser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public void parse() throws InvalidQueryException {
        // call tokenizer.next(); to get the next token
        Token token = tokenizer.next();
        if(token==null){
            return;
        }

        Token.Type tokenType = token.getType();
        String tokenValue = token.getStringValue();

        switch (tokenType){
            case CREATE:
                create();
                break;
            case DROP:
                //validate query
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
                //validate query
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


    private void create() throws InvalidQueryException {
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
        ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER,Token.Type.SEMICOLON));
        if (values != null && tokenizer.next() == null) {
            String dbName = values.get(0);
            System.out.println("creating db " + dbName);
            //SUCCESSFUL QUERY
            CreateQuery query = new CreateQuery();
            query.createDatabase(dbName);
        } else {
            throw new InvalidQueryException("Invalid Syntax for CREATE DATABASE query");
        }
    }

    private void createTable() throws InvalidQueryException {
        Token token;
        ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER, Token.Type.OPEN));
        if (values == null) {
            throw new InvalidQueryException("Invalid syntax for CREATE TABLE query");
        }

        String tableName = values.get(0);
        LinkedList<Column> columns = new LinkedList<>();
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
                    if ((values = matchesTokenList(Arrays.asList(Token.Type.OPEN, Token.Type.INT, Token.Type.CLOSED)))!= null) {
                        System.out.println("column varchar");
                        column = new Column(colName,Column.argDataType.VARCHAR, values.get(1));
                    } else {
                        throw new InvalidQueryException("Invalid Varchar argument");
                    }
                } else if (tokenType == Token.Type.INT) {
                    System.out.println("column int");
                    column = new Column(colName,Column.noArgDataType.INT);
                } else if (tokenType == Token.Type.DECIMAL) {
                    System.out.println("column decimal");
                    column = new Column(colName,Column.noArgDataType.DECIMAL);
                } else if (tokenType == Token.Type.TEXT) {
                    System.out.println("column text");
                    column = new Column(colName,Column.noArgDataType.TEXT);
                } else if (tokenType == Token.Type.BOOLEAN) {
                    System.out.println("column boolean");
                    column = new Column(colName,Column.noArgDataType.BOOLEAN);
                } else {
                    throw new InvalidQueryException("Invalid data type for column: "+colName);
                }
                if ((token = tokenizer.next()) != null && token.getType() == Token.Type.NOT){
                    if ((token = tokenizer.next()) != null && token.getType() == Token.Type.NULL){
                        System.out.println("NOT NULL");
                        column.setAllowNulls(false);
                        token = tokenizer.next();
                    } else {
                        throw new InvalidQueryException("Invalid syntax for column: "+colName);
                    }
                }
                //Column declaration is syntactically correct, add column to list
                columns.add(column);
            } else {
                //KEY declaration
                if (token != null && token.getType() == Token.Type.PRIMARY) {
                    //Primary key declaration
                    if ((values = matchesTokenList(Arrays.asList(Token.Type.KEY, Token.Type.OPEN, Token.Type.IDENTIFIER))) != null) {
                        LinkedList<String> keyColumnNames = new LinkedList<>();
                        keyColumnNames.add(token.getStringValue());
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
                            System.out.println("primary key");
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
                        System.out.println("foreign key");
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
            System.out.println("creating table "+tableName);
            //SUCCESSFUL QUERY
            CreateQuery query = new CreateQuery();
            query.createTable(tableName, columns, primaryKeys, foreignKeys);
        } else {
            throw new InvalidQueryException("Invalid syntax");
        }
    }


    private void insert() throws InvalidQueryException {
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
}
