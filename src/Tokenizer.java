import java.io.BufferedReader;
import java.io.IOException;


public class Tokenizer {

    private final BufferedReader reader;

    public Tokenizer(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * parses input for the next valid token
     * @return the next valid token or Null when there are no more tokens to be read
     * @throws  Exception if invalid token occurs
     */
    public Token next() throws InvalidQueryException {
        StringBuilder tokenString = new StringBuilder();
        try {
            int curr = reader.read();
            while (curr!= -1 && Character.toString(curr).matches("[\s\n\t]")){
                curr = reader.read();
            }
            switch(curr) {
                case -1:
                    //end of input
                    //no syntax error has been found
                    return null;
                case ';':
                    return new Token(Token.Type.SEMICOLON, ";");
                case '*':
                    return new Token(Token.Type.STAR, "*");
                case '=':
                    return new Token(Token.Type.EQUAL, "=");
                case ',':
                    return new Token(Token.Type.COMMA, ",");
                case '(':
                    return new Token(Token.Type.OPEN, "(");
                case ')':
                    return new Token(Token.Type.CLOSED, ")");
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
                        throw new InvalidQueryException("Invalid token " + tokenString);
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
                                throw new InvalidQueryException("Invalid token " + tokenString);
                            }
                            if (curr == '.'){
                                readDecimal = true;
                                //make sure next char is digit
                                curr = reader.read();
                                if (curr < '0' || curr > '9') {
                                    reader.reset();
                                    throw new InvalidQueryException("Invalid token " + tokenString);
                                }
                                tokenString.append(".");
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
                                return new Token(Token.Type.DECIMAL,tokenString.toString());
                            } else {
                                return new Token(Token.Type.INT,tokenString.toString());
                            }

                        } else {

                        //see if keyword, boolean or identifier
                        if ((curr < 'A' || curr > 'Z')&&(curr < 'a' || curr > 'z')&&curr!='_'){
                            throw new InvalidQueryException("Invalid token " + (char)curr);
                        }

                        do {
                            reader.mark(1);
                            tokenString.append((char)curr);
                            curr = reader.read();
                        } while ((curr >= 'A' && curr <= 'Z')||(curr >= 'a' && curr <= 'z')||curr == '_');

                        reader.reset();
                        String s = tokenString.toString().toUpperCase();
                        Token token = Token.getTokenOfType(s);
                        if (token!=null){
                            return token;
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
