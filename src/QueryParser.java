import java.util.LinkedList;
import java.util.List;


public class QueryParser {
    private Tokenizer tokenizer;

    public QueryParser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public Object parse() throws Exception {
        // call tokenizer.next(); to get the next token
        Token token = tokenizer.next();
        if(token==null){
            return null;
        }

        Token.Type tokenType = token.getType();
        String tokenValue = token.getStringValue();

        switch (tokenType){
            case CREATE:
                //validate query
                break;
            case DROP:
                //validate query
                break;
            case INSERT:
                //validate query
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
                throw new Exception("Syntax error: "+tokenValue);
        }

        // need to add return type
        return null;
    }

    private boolean matchesTokenList(List<Token.Type> tokenTypes) throws Exception {
        Token token;
        for (Token.Type type : tokenTypes){
            token = tokenizer.next();
            if (token == null || token.getType() != type){
                return false;
            }
        }
        return true;
    }
}
