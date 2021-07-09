import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;


public class Tokenizer {

    private final BufferedReader reader;
    private static HashSet<String> keywords;

    public Tokenizer(BufferedReader reader) {
        this.reader = reader;
        keywords = new HashSet<>();
        keywords.addAll(Arrays.asList(
                "CREATE", "DROP", "DATABASE", "TABLE", "PRIMARY",
                "FOREIGN", "KEY", "REFERENCES", "NOT", "NULL", "SELECT",
                "FROM", "WHERE", "INSERT", "INTO", "VALUES", "ADD", "DELETE",
                "VARCHAR", "INT", "DECIMAL", "TEXT"));
    }

    /*
    Returns the next available token that can be read from the buffered reader
    Null when there are no more tokens to be read
    Throws exception if an invalid token occurs
     */
    public Token next() throws Exception {
        StringBuilder tokenString = new StringBuilder();
        try {
            int curr = reader.read();
            if (curr == -1){
                //end of input
                //no syntax error has been found
                return null;
            }
            while (Character.toString(curr).matches("\s")){
                curr = reader.read();
            }
            switch(curr) {
                case ';':
                    return new Token(Token.Type.SEMICOLON, ";");
                case '*':
                    return new Token(Token.Type.STAR, "*");
                case '=':
                    return new Token(Token.Type.EQUAL, "=");
                case ',':
                    return new Token(Token.Type.COMMA, ",");
                case '(':
                    return new Token(Token.Type.OPENRND, "(");
                case ')':
                    return new Token(Token.Type.CLOSERND, ")");
                case '"':
                case '\'':
                    // See if String
                    int quote = curr;
                    int prev;
                    do {
                        tokenString.append((char)curr);
                        prev = curr;
                        curr = reader.read();
                    } while (curr != -1 && (curr != quote ||  prev == '\\'));
                    if (curr == -1) {
                        //reached end of input without a closing quote
                        throw new Exception("Invalid token " + tokenString);
                    }
                    //closing quote was found
                    tokenString.append(Character.toString(curr));
                    return new Token(Token.Type.STRING, tokenString.toString());

                default:
                    if (curr == '+'|| curr == '-'|| curr == '.'||(curr >= '0'&&curr <= '9')){
                        //see if number
                        boolean readDecimal = false;
                        if (curr == '.'){
                            tokenString.append((char)curr);
                            readDecimal = true;
                            //check that next char is a digit
                            reader.mark(1);
                            curr = reader.read();
                            if (curr < '0' || curr > '9') {
                                reader.reset();
                                return new Token(Token.Type.PERIOD, ".");
                            }
                        } else if (curr == '+'|| curr == '-') {
                            //starts with +/-
                            tokenString.append((char)curr);
                            reader.mark(1);
                            curr = reader.read();
                            if ((curr < '0' || curr > '9')&&(curr != '.')) {
                                reader.reset();
                                throw new Exception("Invalid token " + tokenString);
                            }
                        }

                        //continue to read until end of number is reached

                        do {
                            if (curr == '.'){
                                readDecimal = true;
                                //make sure next char is digit
                                curr = reader.read();
                                if (curr < '0' || curr > '9') {
                                    break;
                                }
                                tokenString.append(".");
                            }
                            tokenString.append((char)curr);
                            reader.mark(2);
                            curr = reader.read();
                        } while ((curr >= '0' && curr <= '9')||(curr == '.' && !readDecimal));

                            reader.reset();
                            if (readDecimal) {
                                return new Token(Token.Type.DEC,tokenString.toString());
                            } else {
                                return new Token(Token.Type.INT,tokenString.toString());
                            }

                        } else {

                        //see if keyword, boolean or identifier
                        if ((curr < 'A' || curr > 'Z')&&(curr < 'a' || curr > 'z')&&curr!='_'){
                            throw new Exception("Invalid token " + (char)curr);
                        }

                        do {
                            reader.mark(1);
                            tokenString.append((char)curr);
                            curr = reader.read();
                        } while ((curr >= 'A' && curr <= 'Z')||(curr >= 'a' && curr <= 'z')||curr == '_');

                        reader.reset();
                        String s = tokenString.toString().toUpperCase();
                        if (keywords.contains(s)){
                            return new Token(Token.Type.KEYWORD,tokenString.toString().toUpperCase());
                        } else if (s.equals("TRUE")||s.equals("FALSE")){
                            return new Token(Token.Type.BOOLEAN,s);
                        }
                        return new Token(Token.Type.IDENTIFIER,tokenString.toString());
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
