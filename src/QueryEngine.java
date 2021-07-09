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
                if (qc.loginUser()){
                    System.out.println("Login was successful.\n");
                    System.out.println("Enter query:");
                }

                else{
                    System.out.println("Login was not successful. Please press [y/Y] if you wish to change your password");
                    Scanner forgot_password = new Scanner(System.in);
                    String input=forgot_password.nextLine();
                    if(input.equals("y")||input.equals("Y")){
                        qc.forgotPassword();
                    }
                }
                break;
            case 2:
                qc.registerUser();
                break;
            case 3:
                //Code for forget password
                qc.forgotPassword();
                break;
            default:
                //Code for wrong input
        }

    }
}
