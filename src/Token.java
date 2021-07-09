public class Token {

    public static enum Type {
        IDENTIFIER, KEYWORD, STRING, COMMA, PERIOD, NUM, BOOLEAN, SEMICOLON, OPENRND, CLOSERND, STAR, EQUAL
    }

    private final String stringValue;
    private final Type type;

    public Token(Type type, String stringValue){
        this.type = type;
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public Type getType(){
        return type;
    }
}
