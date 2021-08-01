package main.java.parsing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Tokenizer {

    private final CharSequence input;
    private final Pattern intPattern;
    private final Pattern decPattern;
    private final Pattern identifierPattern;
    private final Pattern stringPattern;
    private int start;
    private final int end;
    private int curr;


    public Tokenizer(CharSequence input) {
        this.input = input;
        intPattern = Pattern.compile("[+-]?\\d+");
        decPattern = Pattern.compile("[+-]?\\d*\\.\\d+");
        identifierPattern = Pattern.compile("[a-zA-z_\\d]+");
        stringPattern = Pattern.compile("\"([^'\"\\\\]*(\\\\')*(\\\\\")*)*\"|'([^'\"\\\\]*(\\\\')*(\\\\\")*)*'");
        start = 0;
        end = input.length();
        curr = ' ';
    }

    /**
     * parses input for the next valid token
     * @return the next valid token or Null when there are no more tokens to be read
     * @throws  InvalidQueryException if invalid token occurs
     */
    public Token next() throws InvalidQueryException {

        if (start < end ) {
            curr = input.charAt(start);
            while (start < end - 1 && Character.isWhitespace(curr)) {
                curr = input.charAt(++start);
            }
        }
        if (start > end - 1 || start == end - 1 && Character.isWhitespace(curr)){
            //end of input
            //no syntax error has been found
            return null;
        }
        switch(curr) {
            case ';':
                start++;
                return new Token(Token.Type.SEMICOLON, ";");
            case '*':
                start++;
                return new Token(Token.Type.STAR, "*");
            case '=':
                start++;
                return new Token(Token.Type.EQUAL, "=");
            case ',':
                start++;
                return new Token(Token.Type.COMMA, ",");
            case '(':
                start++;
                return new Token(Token.Type.OPEN, "(");
            case ')':
                start++;
                return new Token(Token.Type.CLOSED, ")");
            case '"':
            case '\'':
                // See if String
                Matcher m = stringPattern.matcher(input);
                m.region(start,end);
                if (m.lookingAt()){
                    //string found
                    start = m.end();
                    return new Token(Token.Type.STRING, m.group());
                }
            default:
                //see if decimal
                m = decPattern.matcher(input);
                m.region(start,end);
                if (m.lookingAt()) {
                    start = m.end();
                    return new Token(Token.Type.DECIMALLITERAL, m.group());
                }

                //see if integer
                m = intPattern.matcher(input);
                m.region(start,end);
                if (m.lookingAt()) {
                    start = m.end();
                    return new Token(Token.Type.INTLITERAL, m.group());
                }

                //see if keyword, boolean or identifier
                m = identifierPattern.matcher(input);
                m.region(start,end);
                if (m.lookingAt()) {
                    start = m.end();
                    String stringValue = m.group();
                    Token token = Token.getTokenOfType(stringValue.toUpperCase());
                    if (token == null) {
                        if (stringValue.equalsIgnoreCase("TRUE") || stringValue.equalsIgnoreCase("FALSE")) {
                            token = new Token(Token.Type.BOOLEANLITERAL, stringValue.toUpperCase());
                        } else {
                            token = new Token(Token.Type.IDENTIFIER, stringValue);
                        }
                    }
                    return token;
                }
        }
        String invalid  = ((String)input.subSequence(start,end)).split(" ")[0];
        throw new InvalidQueryException("Invalid token "+invalid);
    }
}
