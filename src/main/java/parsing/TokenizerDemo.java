package main.java.parsing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

public class TokenizerDemo {
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

        BufferedReader reader = new BufferedReader(new StringReader(query.toString()));
        Tokenizer tokenizer = new Tokenizer(reader);
        Token t;

        //prints type and value of each token entered
        while (true) {
            try {
                if ((t = tokenizer.next()) == null) {
                    break;
                }
                System.out.println(t.getType() + " " + t.getStringValue());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                break;
            }
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
