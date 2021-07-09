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

        if (token.getType() != Token.Type.KEYWORD){
            throw new Exception("Invalid query");
        }

        String tokenValue = token.getStringValue();
        switch (tokenValue) {
            case "CREATE":
                //validate query
                break;
            case "DROP":
                //validate query
                break;
            case "INSERT":
                //validate query
                break;
            case "UPDATE":
                //validate query
                break;
            case "DELETE":
                //validate query
                break;
            case "ALTER":
                //validate query
                break;
            case "SELECT":
                //validate query
                break;
        }


        // need to add return type
        return null;
    }
}
