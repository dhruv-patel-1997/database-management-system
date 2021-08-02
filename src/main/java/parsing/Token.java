package main.java.parsing;

import java.util.HashMap;

public class Token {

    public enum Type {
        IDENTIFIER, STRING, COMMA, PERIOD, INT, INTLITERAL, DECIMAL, DECIMALLITERAL, BOOLEAN, BOOLEANLITERAL, SEMICOLON, OPEN, CLOSED, STAR, EQUAL,
        USE, CREATE, DROP, DATABASE, TABLE, PRIMARY, FOREIGN, KEY, REFERENCES, NOT, NULL, SELECT, FROM, WHERE,LESS,GREATER,
        INSERT, INTO, VALUES, ALTER, UPDATE, ADD, DELETE, VARCHAR, TEXT, TRUNCATE,MODIFY, COLUMN, ERD, MYSQLDUMP
    }

    private static HashMap<String,Type> types = initKeywords();
    private final String stringValue;
    private final Type type;

    public Token(Type type, String stringValue){
        this.type = type;
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public Type getType() {
        return type;
    }


    //Returns new token if the argument is a valid token type, else returns null
    public static Token getTokenOfType(String typeName){
        Type type = types.get(typeName);
        if (type == null){
            return null;
        }
        return new Token(type, typeName);
    }

    private static HashMap<String, Type> initKeywords(){
        types = new HashMap<>();
        types.put("USE", Type.USE);
        types.put("CREATE", Type.CREATE);
        types.put("DROP", Type.DROP);
        types.put("DATABASE", Type.DATABASE);
        types.put("TABLE", Type.TABLE);
        types.put("PRIMARY", Type.PRIMARY);
        types.put("FOREIGN", Type.FOREIGN);
        types.put("KEY", Type.KEY);
        types.put("REFERENCES", Type.REFERENCES);
        types.put("NOT", Type.NOT);
        types.put("NULL", Type.NULL);
        types.put("SELECT", Type.SELECT);
        types.put("FROM", Type.FROM);
        types.put("WHERE", Type.WHERE);
        types.put("INSERT", Type.INSERT);
        types.put("INTO", Type.INTO);
        types.put("VALUES", Type.VALUES);
        types.put("ALTER", Type.ALTER);
        types.put("UPDATE", Type.UPDATE);
        types.put("ADD", Type.ADD);
        types.put("DELETE", Type.DELETE);
        types.put("VARCHAR", Type.VARCHAR);
        types.put("INT", Type.INT);
        types.put("DECIMAL", Type.DECIMAL);
        types.put("TEXT", Type.TEXT);
        types.put("BOOLEAN", Type.BOOLEAN);
        types.put("MODIFY", Type.MODIFY);
        types.put("COLUMN", Type.COLUMN);
        types.put("TRUNCATE", Type.TRUNCATE);
        types.put("ERD", Type.ERD);
        types.put("MYSQLDUMP", Type.MYSQLDUMP);
        return types;
    }
}
