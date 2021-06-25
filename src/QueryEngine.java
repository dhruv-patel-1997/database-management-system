import java.util.Scanner;

public class QueryEngine {

    public static void main(String[]args){
        Scanner sc = new Scanner(System.in);
        QueryClass qc = new QueryClass();
        System.out.println("Hello User Please Choose: ");
        System.out.println("1. Log in: ");
        System.out.println("2. Register your self ");
        System.out.println("3. Forgot password ");
        int choice = sc.nextInt();
        switch (choice) {
            case 1:
               //Code for Log in
                break;
            case 2:
                qc.registerUser();
                break;
            case 3:
                //Code for forget password
                break;
            default:
                //Code for wrong input
        }

    }
}
