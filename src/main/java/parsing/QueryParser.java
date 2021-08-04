package main.java.parsing;

import Utilities.Context;
import Utilities.DataDictionaryUtils;
import Utilities.TableMaker;
import Utilities.TableUtils;
import main.java.dataStructures.Column;
import main.java.dataStructures.ForeignKey;
import main.java.dataStructures.PrimaryKey;
import main.java.exceptions.LockTimeOutException;
import main.java.logs.EventLog;
import main.java.logs.GeneralLog;
import main.java.exceptions.InvalidQueryException;
import main.java.queries.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class QueryParser {
    private Tokenizer tokenizer;
    boolean isTransaction;
    boolean commitReached;
    Queue<Callable> queries;
    LinkedList<String> tablesToLock;

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

        //Check if user has input a transaction
        isTransaction = false;
        commitReached = false;
        queries = new LinkedList<>();
        tablesToLock = new LinkedList<>();
        if (tokenType == Token.Type.START){
            if (matchesTokenList(Arrays.asList(Token.Type.TRANSACTION, Token.Type.SEMICOLON)) != null) {
                isTransaction = true;
                token = tokenizer.next();
                tokenValue = token.getStringValue();
                tokenType = token.getType();
            } else {
                throw new InvalidQueryException("Invalid syntax: "+token.getStringValue());
            }
        }

        do {
            System.out.println("parsing query: "+tokenType);
            switch (tokenType) {
                case COMMIT:
                    commitReached = commit();
                    break;
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
                    update();
                    break;
                case ALTER:
                    alter();
                    break;
                case SELECT:
                    select();
                    break;
                case ERD:
                    generateErd(Context.getDbName());
                    break;
                default:
                    throw new InvalidQueryException("Invalid syntax: " + tokenValue);
            }
            if (isTransaction) {
                token = tokenizer.next();
                if (token != null) {
                    tokenValue = token.getStringValue();
                    tokenType = token.getType();
                }
            }
        } while (isTransaction && !commitReached);

        //get new transaction ID
        Context.incrTransactionId();

        //Lock tables
        HashMap<String,String> backup = new HashMap<>();
        for (String table: tablesToLock){
            if (DataDictionaryUtils.tableDictionaryExists(Context.getDbName(),table)) {
                DataDictionaryUtils.lockTable(Context.getDbName(), table);

                //make back ups of affected tables
                StringBuilder tableBackup = new StringBuilder();
                StringBuilder ddBackUp = new StringBuilder();
                File file = new File(Context.getDbPath() + table + ".txt");
                Scanner sc = new Scanner(file);
                while (sc.hasNext()) {
                    tableBackup.append(sc.nextLine()).append("\n");
                }
                file = new File(Context.getDbPath() + "dd_" + table + ".txt");
                sc = new Scanner(file);
                while (sc.hasNext()) {
                    ddBackUp.append(sc.nextLine()).append("\n");
                }
                sc.close();
                backup.put(Context.getDbPath() + table + ".txt", tableBackup.toString());
                backup.put(Context.getDbPath() + "dd_" + table + ".txt", ddBackUp.toString());
            }
        }


        //Run queries
        for (Callable query: queries){
            try {
                query.call();
            } catch (Exception e){
                //make sure callable throws exception on failure
                //if query failed, need to restore backup
                for (Map.Entry<String, String> entry : backup.entrySet()) {
                    //replace files
                    String content = entry.getValue();
                    File file = new File(entry.getKey());
                    FileWriter fw = new FileWriter(file);
                    fw.write(content);
                    fw.close();
                }
                for (String table: tablesToLock){
                    DataDictionaryUtils.unlockTable(Context.getDbName(),table);
                }
                throw new InvalidQueryException("Transaction failed, database restored to prior state");
            }
        }

        for (String table: tablesToLock){
            DataDictionaryUtils.unlockTable(Context.getDbName(),table);
        }

        //SQL dump for whole transaction
        if(tokenType.equals(Token.Type.CREATE) || tokenType.equals(Token.Type.INSERT) || tokenType.equals(Token.Type.ALTER) || tokenType.equals(Token.Type.UPDATE)){
            generateDump(Context.getDbName(),tokenizer.getInput()+"\n");
        }
    }

    private boolean commit() throws InvalidQueryException {
        Token token = tokenizer.next();
        if (token != null && token.getType() == Token.Type.SEMICOLON && tokenizer.next() == null) {
            return true;
        } else {
            throw new InvalidQueryException("Invalid syntax after COMMIT");
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
        if ((values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER,Token.Type.SEMICOLON))) != null){
            //valid use query that is part of a transaction;
            queries.add(new Callable() {
                @Override
                public Object call() {
                    try {
                        new UseQuery().useDataBase(values.get(0));
                    } catch (InvalidQueryException exception) {
                        exception.printStackTrace();
                    }
                    return null;
                }
            });
        } else {
            throw new InvalidQueryException("Invalid syntax for USE query");
        }
    }

    private void select() throws IOException, LockTimeOutException, InvalidQueryException {
        Token token;

        token = tokenizer.next();
        if(Context.getDbName()!=null) {
            if(token != null && token.getType() == Token.Type.STAR) {
                ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.FROM, Token.Type.IDENTIFIER));
                if(values == null) {
                    throw new InvalidQueryException("Invalid syntax for SELECT TABLE query expecting FROM and IDENTIFIER");
                } else {
                    token = tokenizer.next();
                    String tableName = values.get(1);
                    if(! Context.isTableExist(tableName))
                        throw new InvalidQueryException("Table does not exist: " + tableName);
                    if((token.getType() == Token.Type.SEMICOLON) && Context.isTableExist(tableName)) { // Parsing for simple query
                        tablesToLock.add(tableName);// Parsing ends here
                        queries.add(new Callable() {
                            @Override
                            public Object call() throws IOException, LockTimeOutException, InvalidQueryException {
                                SelectQuery selectQuery = new SelectQuery();//execution started
                                selectQuery.selectForAll(Context.getDbName(), tableName);

                                return null;
                            }
                        });

                    } else {
                        if(token.getType() != Token.Type.WHERE)
                            throw new InvalidQueryException("Please select the database first");
                        token = tokenizer.next();
                        if(token.getType() != Token.Type.IDENTIFIER)
                            throw new InvalidQueryException("Invalid syntax for SELECT TABLE query expecting IDENTIFIER");
                        String colName = token.getStringValue();
                        token = tokenizer.next();
                        if(token.getType() != Token.Type.EQUAL && token.getType() != Token.Type.LESS && token.getType() != Token.Type.GREATER)
                            throw new InvalidQueryException("Invalid syntax for SELECT TABLE query expecting '=','<','>'");
                        String operand = token.getStringValue();
                        token = tokenizer.next();
                        String columnValue = null;
                        if(token.getType() == Token.Type.STRING) {
                            columnValue = token.getStringValue();

                        } else if(token.getType() == Token.Type.INTLITERAL) {
                            columnValue = token.getStringValue();

                        } else if(token.getType() == Token.Type.BOOLEANLITERAL) {
                            columnValue = token.getStringValue();

                        }
                        token = tokenizer.next();
                        LinkedHashMap<String, Column> columnData = DataDictionaryUtils.getColumns(Context.getDbName(), tableName);
                        if(columnData.get(colName) == null)
                            throw new InvalidQueryException("Column with given name does not exist: " + colName);
                        if((token.getType() == Token.Type.SEMICOLON) && Context.isTableExist(tableName)) {
                            if(columnValue != null) {
                                tablesToLock.add(tableName);//Parsing for where condition
                                String finalColumnValue = columnValue;
                                queries.add(new Callable() {
                                    @Override
                                    public Object call() throws IOException, LockTimeOutException, InvalidQueryException {//execution starts here
                                        SelectQuery selectQuery = new SelectQuery();
                                        selectQuery.showAllForCondition(Context.getDbName(), tableName, colName, finalColumnValue, operand);
                                        return null;
                                    }
                                });
                            } else
                                throw new InvalidQueryException("Invalid Query exception");
                        }

                    }
                }

            } else if(token != null && token.getType() == Token.Type.IDENTIFIER) {
                ArrayList<String> columns = new ArrayList<>();
                while (token != null && token.getType() != Token.Type.FROM && token.getType() == Token.Type.IDENTIFIER) {
                    columns.add(token.getStringValue());
                    token = tokenizer.next();
                    if(token.getType() == Token.Type.FROM)
                        break;
                    if(token.getType() != Token.Type.COMMA)
                        throw new InvalidQueryException("Invalid syntax for SELECT TABLE query expecting COMMA");
                    token = tokenizer.next();
                }

                ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER));
                if(values == null) {
                    throw new InvalidQueryException("Invalid syntax for SELECT TABLE expecting IDENTIFIER");
                }
                String tableName = values.get(0);
                if(! Context.isTableExist(tableName))
                    throw new InvalidQueryException("Table does not exist: " + tableName);
                LinkedHashMap<String, Column> columnData = DataDictionaryUtils.getColumns(Context.getDbName(), tableName);
                for(int i = 0; i < columns.size(); i++) {
                    if(columnData.get(columns.get(i)) == null)
                        throw new InvalidQueryException("Column does not exist: " + columns.get(i));
                }
                token = tokenizer.next();
                if((token.getType() == Token.Type.SEMICOLON) && Context.isTableExist(tableName)) {
                    tablesToLock.add(tableName);//Parsing for particular columns without where condition
                    queries.add(new Callable() {
                        @Override
                        public Object call() throws IOException, LockTimeOutException, InvalidQueryException {//execution starts here
                            SelectQuery selectQuery = new SelectQuery();
                            selectQuery.showForLimited(Context.getDbName(), tableName, columns);
                            return null;
                        }
                    });

                } else {
                    if(token.getType() != Token.Type.WHERE)
                        throw new InvalidQueryException("Invalid syntax for SELECT TABLE query expecting WHERE");
                    token = tokenizer.next();
                    if(token.getType() != Token.Type.IDENTIFIER)
                        throw new InvalidQueryException("Invalid syntax for SELECT TABLE query expecting IDENTIFIER");
                    String colName = token.getStringValue();
                    token = tokenizer.next();
                    if(token.getType() != Token.Type.EQUAL && token.getType() != Token.Type.LESS && token.getType() != Token.Type.GREATER)
                        throw new InvalidQueryException("Invalid syntax for SELECT TABLE expecting only '=','<','>'");
                    String operand = token.getStringValue();
                    token = tokenizer.next();
                    String columnValue = null;
                    if(token.getType() == Token.Type.STRING) {
                        columnValue = token.getStringValue();

                    } else if(token.getType() == Token.Type.INTLITERAL) {
                        columnValue = token.getStringValue();

                    } else if(token.getType() == Token.Type.BOOLEANLITERAL) {
                        columnValue = token.getStringValue();

                    }
                    token = tokenizer.next();
                    if(columnData.get(colName) == null)
                        throw new InvalidQueryException("Column with given name does not exist: " + colName);
                    if((token.getType() == Token.Type.SEMICOLON) && Context.isTableExist(tableName)) {
                        tablesToLock.add(tableName);//parsing for specific columns with where condition
                        String finalColumnValue = columnValue;
                        queries.add(new Callable() {
                            @Override
                            public Object call() throws Exception {//execution starts here
                                SelectQuery selectQuery = new SelectQuery();
                                selectQuery.showForColumnsForCondition(Context.getDbName(), tableName, colName, finalColumnValue, columns, operand);
                                return null;
                            }
                        });
                    } else {
                        throw new InvalidQueryException("Invalid syntax for SELECT query");
                    }

                }

            } else {
                throw new InvalidQueryException("Invalid syntax for SELECT TABLE query");
            }
        }else
            throw new InvalidQueryException("Database not selected, first 'use databaseName;' to choose database");

    }
    private void  update() throws IOException, LockTimeOutException, InvalidQueryException {
        Token token;
        if(Context.getDbName()!=null) {
            ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER));
            String tableName = values.get(0);
            token = tokenizer.next();
            if(token != null && token.getType() != Token.Type.SET) {
                throw new InvalidQueryException("Invalid syntax1");
            }
            token = tokenizer.next();
            ArrayList<String> columnName = new ArrayList<>();
            ArrayList<String> columnType = new ArrayList<>();
            ArrayList<String> columnValue = new ArrayList<>();

            while (token != null && token.getType() != Token.Type.FROM && token.getType() == Token.Type.IDENTIFIER) {
                String colName = token.getStringValue();
                token = tokenizer.next();
                if(token.getType() != Token.Type.EQUAL)
                    throw new InvalidQueryException("Invalid syntax for UPDATE TABLE query2");
                String type;
                String colValue;
                token = tokenizer.next();
                if(token.getType() == Token.Type.STRING) {
                    type = "VARCHAR 255";
                    colValue = token.getStringValue();
                } else if(token.getType() == Token.Type.INTLITERAL) {
                    type = "INT";
                    colValue = token.getStringValue();
                } else if(token.getType() == Token.Type.BOOLEANLITERAL) {
                    type = "BOOLEAN";
                    colValue = token.getStringValue();
                }else if(token.getType() == Token.Type.NULL){
                    type="NULL";
                    colValue=" ";
                }
                else { //if the token type is not from above
                    throw new InvalidQueryException("Invalid syntax for UPDATE TABLE query we can only parse varchar, integer and boolean");
                }
                columnName.add(colName);
                columnType.add(type);
                columnValue.add(colValue);
                token = tokenizer.next();
                if(token.getType() == Token.Type.WHERE)
                    break;
                if(token.getType() != Token.Type.COMMA)
                    throw new InvalidQueryException("Invalid syntax for UPDATE TABLE query");//Parsing commas for multiple columns
                token = tokenizer.next();
            }

            if(!Context.isTableExist(tableName))
            {
                throw new InvalidQueryException("Invalid table name, no table exist with given name: "+tableName);
            }

            //Checks the data type compatibility for the columns which we are updating
            LinkedHashMap<String, Column> columnData = DataDictionaryUtils.getColumns(Context.getDbName(), tableName);
            for(int i = 0; i < columnName.size(); i++) {
                if(columnData.get(columnName.get(i)) != null) {
                    if(! (columnData.get(columnName.get(i)).getDataType().equals(columnType.get(i))||columnType.get(i).equals("NULL")))
                        throw new InvalidQueryException("Invalid data type for column: " + columnName.get(i) + " it should be: "
                                + columnData.get(columnName.get(i)).getDataType() + " You have passed: "
                                + columnType.get(i));
                    //checks primary key constraint
                    if(columnData.get(columnName.get(i)).isPrimaryKey()) {
                        ArrayList<String> columnValues = TableUtils.getColumns(Context.getDbName(), tableName, new ArrayList<String>(Arrays.asList(columnName.get(i)))).get(columnName.get(i));
                        if(columnValues != null && columnValues.contains(columnValue.get(i))) {
                            //value is already present
                            throw new InvalidQueryException("Primary key constraint fails: " + columnValue.get(i) + "is already present in table");
                        }
                    }//checks null key constraint
                    if(! columnData.get(columnName.get(i)).getAllowNulls()) {
                        if(columnType.get(i).equals("NULL"))
                            throw new InvalidQueryException("Null values not allowed for column: " + columnName.get(i));
                    }//checks foreign key constraint
                    if(columnData.get(columnName.get(i)).getForeignKey() != null) {
                        System.out.println(columnData.get(columnName.get(i)).getColName() + " is fk with val " + columnValue.get(i));
                        String refTable = columnData.get(columnName.get(i)).getForeignKey().getReferencedTable();
                        String refColumn = columnData.get(columnName.get(i)).getForeignKey().getReferencedColumn();
                        ArrayList<String> columnValues = TableUtils.getColumns(Context.getDbName(), refTable, new ArrayList<String>(Arrays.asList(refColumn))).get(refColumn);
                        if((columnValues == null || ! columnValues.contains(columnValue.get(i)))) {
                            //value is not present
                            throw new InvalidQueryException("Foreign key constraint fails: " + columnValue.get(i) + " not present in referenced column");
                        }
                    }
                }
                else
                    throw new InvalidQueryException("Column does not exist: "+columnName.get(i));
            }
            //Getting condition -> "WHERE A="abc;"
            token = tokenizer.next();
            if(token.getType() != Token.Type.IDENTIFIER)
                throw new InvalidQueryException("Invalid syntax for UPDATE TABLE query");
            String colName = token.getStringValue();
            token = tokenizer.next();
            if(token.getType() != Token.Type.EQUAL)
                throw new InvalidQueryException("Invalid syntax for UPDATE TABLE query");
            token = tokenizer.next();
            boolean result = false;
            String colValue;
            // condition is for integer
            if(token.getType() == Token.Type.INTLITERAL) {
                colValue = token.getStringValue();
            }// condition is for boolean
            else if(token.getType() == Token.Type.BOOLEANLITERAL) {
                colValue = token.getStringValue();
                // condition is for string
            } else if(token.getType() == Token.Type.STRING) {
                colValue = token.getStringValue();
            } else {
                throw new InvalidQueryException("Invalid syntax for UPDATE TABLE query");
            }
            token = tokenizer.next();
            if((token.getType() == Token.Type.SEMICOLON) && Context.isTableExist(tableName)) {
                tablesToLock.add(tableName);//Parsing ends here
                queries.add(new Callable() {
                    @Override
                    public Object call() throws IOException, LockTimeOutException, InvalidQueryException {//Execution starts here
                        UpdateQuery updateQuery = new UpdateQuery();
                        updateQuery.update(Context.getDbName(), tableName, columnName, columnType, columnValue, colName, colValue);
                        return null;
                    }
                });

            }else
            {
                throw new InvalidQueryException("Invalid syntax for UPDATE TABLE query");
            }


        }else
        {
            throw new InvalidQueryException("Please select Database first");
        }

    }




    private void alter() throws InvalidQueryException {
        if(Context.getDbName()!=null) {
            Token token = tokenizer.next();
            if (token != null && token.getType() == Token.Type.TABLE) {
                token = tokenizer.next();
                if (token != null && token.getType() == Token.Type.IDENTIFIER) {
                    String tableName = token.getStringValue();
                    token = tokenizer.next();
                    switch (token.getType()) {
                        case ADD:
                            alterAdd(tableName);
                            break;
                        case DROP:
                            alterDrop(tableName);
                            break;
                        default:
                            throw new InvalidQueryException("Invalid syntax for ALTER query");
                    }
                } else {
                    throw new InvalidQueryException("Invalid syntax for ALTER query");
                }
            } else {
                throw new InvalidQueryException("Invalid syntax for ALTER query");
            }
        }
        else{
            throw new InvalidQueryException("Invalid syntax for ALTER query");
        }
    }

    private void alterAdd(String tableName){
        try {
            ArrayList<String> values;
            Token token=tokenizer.next();

            if (token != null && token.getType() == Token.Type.IDENTIFIER) {
                String columnName = token.getStringValue();
                token = tokenizer.next();
                Token.Type tokenType = token.getType();
                AddAlterQuery addAlterQuery=new AddAlterQuery();
                if (tokenType == Token.Type.VARCHAR) {
                    if ((values = matchesTokenList(Arrays.asList(Token.Type.OPEN, Token.Type.INTLITERAL, Token.Type.CLOSED))) != null) {
                        token = tokenizer.next();
                        if (token != null && token.getType() == Token.Type.SEMICOLON) {
                            //Successful
                            queries.add(new Callable() {
                                @Override
                                public Object call() throws IOException, LockTimeOutException, InvalidQueryException {
                                    addAlterQuery.addColumn(tableName,columnName,"VARCHAR "+ values.get(1));
                                    System.out.println("Alter query executed successfully");
                                    return null;
                                }
                            });
                        } else {
                            throw new InvalidQueryException("Invalid syntax for ALTER query");
                        }
                    } else {
                        throw new InvalidQueryException("Invalid Varchar argument");
                    }
                } else if (tokenType == Token.Type.INT) {
                    token = tokenizer.next();
                    if (token != null && token.getType() == Token.Type.SEMICOLON) {
                        //Successful
                        addAlterQuery.addColumn(tableName,columnName,"INT");
                        System.out.println("Alter query executed successfully");
                    } else {
                        throw new InvalidQueryException("Invalid syntax for ALTER query");
                    }
                } else if (tokenType == Token.Type.DECIMAL) {
                    token = tokenizer.next();
                    if (token != null && token.getType() == Token.Type.SEMICOLON) {
                        //Successful
                        addAlterQuery.addColumn(tableName,columnName,"DECIMAL");
                        System.out.println("Alter query executed successfully");
                    } else {
                        throw new InvalidQueryException("Invalid syntax for ALTER query");
                    }
                } else if (tokenType == Token.Type.TEXT) {
                    token = tokenizer.next();
                    if (token != null && token.getType() == Token.Type.SEMICOLON) {
                        //Successful
                        addAlterQuery.addColumn(tableName,columnName,"TEXT");
                        System.out.println("Alter query executed successfully");
                    } else {
                        throw new InvalidQueryException("Invalid syntax for ALTER query");
                    }
                } else if (tokenType == Token.Type.BOOLEAN) {
                    token = tokenizer.next();
                    if (token != null && token.getType() == Token.Type.SEMICOLON) {
                        //Successful
                        addAlterQuery.addColumn(tableName,columnName,"BOOLEAN");
                        System.out.println("Alter query executed successfully");
                    } else {
                        throw new InvalidQueryException("Invalid syntax for ALTER query");
                    }
                } else {
                    throw new InvalidQueryException("Invalid data type for column: ");
                }
            }
            else{
                throw new InvalidQueryException("Invalid syntax for ALTER query");
            }

        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }

    private void alterDrop(String tableName){
        try {
            ArrayList<String> values;
            Token token=tokenizer.next();
            if (token != null && token.getType() == Token.Type.COLUMN) {
                token=tokenizer.next();
                if (token != null && token.getType() == Token.Type.IDENTIFIER) {
                    //Successful
                    DropAlterQuery dropAlterQuery=new DropAlterQuery();
                    String columnName = token.getStringValue();
                    queries.add(new Callable() {
                        @Override
                        public Object call() throws IOException, LockTimeOutException {
                            if(dropAlterQuery.dropColumn(tableName,columnName))
                                System.out.println("Alter query executed successfully");
                            return null;
                        }
                    });
                }
                else{
                    throw new InvalidQueryException("Invalid syntax for ALTER query");
                }
            }
            else{
                throw new InvalidQueryException("Invalid syntax for ALTER query");
            }

        } catch (InvalidQueryException e) {
            System.out.println(e.getMessage());
        }

    }

    private void drop() throws InvalidQueryException{
        EventLog eventLog=new EventLog();
        Logger eventLogger=eventLog.setLogger();
        Token token = tokenizer.next();
        if (token!=null && token.getType() == Token.Type.DATABASE){
            dropDatabase();
        } else if (token!=null && token.getType() == Token.Type.TABLE){
            dropTable();
        } else {
            eventLogger.info("Crashed for User:"+Context.getUserName()+"Invalid DROP query");
            throw new InvalidQueryException("Invalid DROP query");
        }
    }

    private void dropDatabase() throws InvalidQueryException{
        try {
            GeneralLog generalLog = new GeneralLog();
            EventLog eventLog = new EventLog();
            Logger general = generalLog.setLogger();
            Logger eventLogger = eventLog.setLogger();

            ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER, Token.Type.SEMICOLON));
            if (values != null && tokenizer.next() == null) {
                String dbName = values.get(0);
                //SUCCESSFUL QUERY
                LocalTime start = LocalTime.now();

                general.info("User: " + Context.getUserName() + " At the start of drop query");
                general.info("Database status at the start of drop query: " + TableUtils.getGeneralLogTableInfo(dbName) + "\n");

                queries.add(new Callable() {
                    @Override
                    public Object call() throws IOException, LockTimeOutException, InvalidQueryException {
                        DropQuery query = new DropQuery();
                        query.dropDatabase(dbName);
                        LocalTime end = LocalTime.now();
                        int diff = end.getNano() - start.getNano();
                        general.info("Database status at the end of drop query: " + TableUtils.getGeneralLogTableInfo(dbName) + "\n");
                        general.info("User: " + Context.getUserName() + "\nAt the end of drop query" + "\n" + "Execution Time of query: " + diff + " nanoseconds");
                        eventLogger.info("User " + Context.getUserName() + " deleted database " + dbName);
                        System.out.println("Database deleted successfully");
                        return null;
                    }
                });
            } else {
                throw new InvalidQueryException("Invalid Syntax for DROP DATABASE query");
            }
        }catch (InvalidQueryException exception){
            throw new InvalidQueryException("Invalid Syntax for DROP DATABASE query");
        }
    }

    private void dropTable() throws InvalidQueryException{
        GeneralLog generalLog=new GeneralLog();
        Logger generalLogger=generalLog.setLogger();

        ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER, Token.Type.SEMICOLON));
        if (values != null && tokenizer.next() == null) {
            String tableName = values.get(0);
            //SUCCESSFUL QUERY
            LocalTime start=LocalTime.now();
            DropQuery query=new DropQuery();
            if(Context.getDbName()==null){
                throw new InvalidQueryException("Please select database first");
            }

            queries.add(new Callable() {
                @Override
                public Object call() throws IOException, LockTimeOutException, InvalidQueryException {
                    generalLogger.info("User: "+Context.getUserName()+" At the start of drop query");
                    generalLogger.info("Database status at the start of drop query: "+TableUtils.getGeneralLogTableInfo(Context.getDbName())+"\n");
                    query.dropTable(Context.getDbName(),tableName);
                    query.ddDropTable(Context.getDbName(),tableName);
                    LocalTime end=LocalTime.now();
                    int diff=end.getNano()-start.getNano();
                    generalLogger.info("User: "+Context.getUserName()+"\nAt the end of drop query"+"\n"+"Execution Time of query: "+diff+" nanoseconds");
                    System.out.println("Dropped table successful");
                    return null;
                }
            });
        } else {
            throw new InvalidQueryException("Invalid Syntax for DROP TABLE query");
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
        EventLog eventLog=new EventLog();

        ArrayList<String> values = matchesTokenList(Arrays.asList(Token.Type.IDENTIFIER, Token.Type.SEMICOLON));
        if (values != null) {
            String dbName = values.get(0);
            //SUCCESSFUL QUERY
            queries.add(new Callable() {
                @Override
                public Object call() throws InvalidQueryException {
                    CreateQuery query = new CreateQuery();
                    try {
                        query.createDatabase(dbName);
                    } catch (InvalidQueryException exception) {
                        exception.printStackTrace();
                    }
                    Logger eventLogger=eventLog.setLogger();
                    eventLogger.info("User "+Context.getUserName()+ " created database "+dbName);
                    return null;
                }
            });
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
                        //add referenced table to list of tablesToLock
                        tablesToLock.add(values.get(5));
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
                && (token = tokenizer.next()) != null && token.getType() == Token.Type.SEMICOLON) {
            //SUCCESSFUL QUERY
            //generateDump(Context.getDbName(),tokenizer.getInput()+"\n");
            queries.add(new Callable() {
                @Override
                public Object call() throws IOException, LockTimeOutException, InvalidQueryException {
                    boolean keepTableLocked = false;
                    CreateQuery query = new CreateQuery();
                    if (isTransaction){
                        keepTableLocked = true;
                    }
                    query.createTable(tableName, columns, primaryKeys, foreignKeys,keepTableLocked);
                    return null;
                }
            });

        } else {
            throw new InvalidQueryException("Invalid syntax");
        }
    }

    private void insert() throws InvalidQueryException, LockTimeOutException, FileNotFoundException {
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
                if ((token = tokenizer.next()) != null && token.getType() == Token.Type.SEMICOLON){
                    //SUCCESSFUL QUERY

                    //generateDump(Context.getDbName(),tokenizer.getInput()+"\n");

                    //add callable to query queue and table to tablestoLock
                    tablesToLock.add(tableName);
                    queries.add(new Callable() {
                        @Override
                        public Object call() throws LockTimeOutException, IOException, InvalidQueryException {
                            InsertQuery query = new InsertQuery();
                            query.insert(tableName, cols,vals);
                            return null;
                        }
                    });

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

    private static void generateErd(String dbName) throws InvalidQueryException {
        try {
            if (dbName != null) {
                ArrayList<String> tables = TableUtils.getTableInDb(dbName);
                ArrayList<String> dd_tables = TableUtils.getTableInDb_DD(dbName);
                File erd = new File("Databases/erd/" + dbName + "/erd.txt");
                Files.createDirectories(Paths.get("Databases/erd/"+dbName));
                erd.createNewFile();
                FileWriter myWriter = new FileWriter("Databases/erd/" + dbName + "/erd.txt");
                String dd_data = "";
                if(!dd_tables.isEmpty()){
                    for(String s:dd_tables){
                        s=s.split("\\.")[0];
                        dd_data+="Table: "+s+"\n"+readAllBytesJava7("Databases/" + dbName + "/" + s + ".txt")+"\n\n";
                    }
                }
                for (String t : tables) {
                    t = t.split("\\.")[0];
                    LinkedHashMap<String, Column> data = DataDictionaryUtils.getColumns_erd(dbName, t);
                    for (Map.Entry<String, Column> entry : data.entrySet()) {
                        String key = entry.getKey();
                        Column value = entry.getValue();
                        if (value.getForeignKey() != null) {
                            myWriter.write("Table " + t + " having column "+value.getColName()+ " references " + value.getForeignKey().getReferencedTable() + " having primary key " + value.getForeignKey().getReferencedColumn()+"\n\n");
                            System.out.println("Table " + t + " having column "+value.getColName()+ value.getForeignKey().getReferencedTable() + " having primary key " + value.getForeignKey().getReferencedColumn());
                        }
                    }
                }
                myWriter.write(dd_data);
                myWriter.close();
            } else {
                throw new InvalidQueryException("Please use database first");
            }
        } catch (LockTimeOutException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String readAllBytesJava7(String filePath)
    {
        String content = "";

        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return content;
    }

    private void generateDump(String dbName,String data) throws InvalidQueryException {
        try {
            if (dbName != null) {
                File erd = new File("Databases/dump/" + dbName + "/dump.txt");
                Files.createDirectories(Paths.get("Databases/dump/"+dbName));
                erd.createNewFile();
                FileWriter myWriter = new FileWriter("Databases/dump/" + dbName + "/dump.txt",true);
                myWriter.write(data);
                myWriter.close();
            } else {
                throw new InvalidQueryException("Please use database first");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
