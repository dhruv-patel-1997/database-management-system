public class QueryParser {
    private Tokenizer tokenizer;

    public QueryParser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public void parse(){
        // call tokenizer.next(); to get the next token
        try {
            Token token;
            while((token = tokenizer.next())!=null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // need to add return type
    }
}
