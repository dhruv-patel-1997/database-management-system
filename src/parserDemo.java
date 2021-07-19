import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

public class parserDemo {
    public static void main(String[] args) throws IOException {
        BufferedReader inputReader;
        BufferedReader br;
        int c;
        do {
            inputReader = new BufferedReader(new InputStreamReader(System.in));
            StringBuilder query = new StringBuilder();
            boolean end = false;

            do {
                if ((c = inputReader.read()) == ';') {
                    end = true;
                }

                if (c == ':') {
                    //canceled query
                    break;
                }
                query.append((char) c);

            } while (!end || c != '\n');

            query.append((char) c);

            br = new BufferedReader(new StringReader(query.toString()));
            Tokenizer tokenizer = new Tokenizer(br);
            QueryParser parser = new QueryParser(tokenizer);

            try {
                parser.parse();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } while (c!=':');

        try {
            br.close();
            inputReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
