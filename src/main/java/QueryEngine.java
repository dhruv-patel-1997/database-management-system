package main.java;

import main.java.logs.QueryLog;

import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.logging.Logger;

public class QueryEngine {

    public static void main(String[]args){
        Scanner sc = new Scanner(System.in);
        QueryClass qc = new QueryClass();
        boolean exit = false;
        while (!exit) {
            System.out.println("Hello User Please Choose: ");
            System.out.println("1. Log in: ");
            System.out.println("2. Register your self ");
            System.out.println("3. Forgot password ");
            System.out.println("4. exit ");
            int choice;
            try{
                choice = sc.nextInt();
            } catch (InputMismatchException e){
                choice = -1;
            }
            sc.nextLine();
            switch (choice) {
                case 1:
                    if (qc.loginUser()) {
                        System.out.println();
                        qc.getQueries();
                    } else {
                        System.out.println("Login was not successful. Please press [y/Y] if you wish to change your password,\nelse press any key to return to main menu");
                        String input = sc.nextLine();
                        if (input.equals("y") || input.equals("Y")) {
                            qc.forgotPassword();
                        }
                    }
                    break;
                case 2:
                    qc.registerUser();
                    break;
                case 3:
                    qc.forgotPassword();
                    break;
                case 4:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid input, please try again.");
            }
            System.out.println();
        }
        sc.close();
    }
}
