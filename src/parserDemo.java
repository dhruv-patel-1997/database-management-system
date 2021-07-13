import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

public class parserDemo {
    public static void main(String[] args) throws IOException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder query = new StringBuilder();
        int c;
        boolean end = false;

        do {
            if ((c = inputReader.read()) == ';'){
                end = true;
            }

            if (c == ':') {
                //canceled query
                return;
            }
            query.append((char)c);

        } while (!end || c != '\n');

        try {
            inputReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        query.append((char)c);

        BufferedReader br = new BufferedReader(new StringReader(query.toString()));
        Tokenizer tokenizer = new Tokenizer(br);
        QueryParser parser = new QueryParser(tokenizer);

        try {
            parser.parse();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
