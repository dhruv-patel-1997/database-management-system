import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

public class TokenizerDemo {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        String s = "";
        while (!s.equals("exit")) {
            s = sc.nextLine();
            BufferedReader br = new BufferedReader(new StringReader(s));

            Tokenizer tokenizer = new Tokenizer(br);
            Token t;

            //prints type and value of each token entered
            while (true) {
                try {
                    if ((t = tokenizer.next()) == null) break;
                    System.out.println(t.getType() + " " + t.getStringValue());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            }

            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
