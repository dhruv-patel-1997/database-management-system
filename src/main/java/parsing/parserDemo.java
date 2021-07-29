package main.java.parsing;

import main.java.queries.QueryParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class parserDemo {
    public static void main(String[] args) throws IOException {
        BufferedReader inputReader;
        int c;
        while (true) {
            inputReader = new BufferedReader(new InputStreamReader(System.in));
            StringBuilder query = new StringBuilder();
            boolean end = false;

            do {
                if ((c = inputReader.read()) == ';') {
                    end = true;
                }

                if (c == ':') {
                    //canceled query
                    try {
                        inputReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                query.append((char) c);

            } while (!end || c != '\n');

            query.append((char) c);

            Tokenizer tokenizer = new Tokenizer(query);
            QueryParser parser = new QueryParser(tokenizer);

            try {
                parser.parse();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
