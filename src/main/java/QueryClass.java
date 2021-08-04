package main.java;

import Utilities.Context;
import main.java.QueryEngine;
import main.java.logs.QueryLog;
import main.java.parsing.InvalidQueryException;
import main.java.parsing.Tokenizer;
import main.java.queries.LockTimeOutException;
import main.java.queries.QueryParser;

import java.io.*;
import java.security.MessageDigest;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

public class QueryClass {

    public void registerUser() {
        String uname, password,p2, q1, q2, q3, q4;
        System.out.println("Please Enter your details");
        System.out.println("Enter username(no space allowed) : ");
        Scanner sc = new Scanner(System.in);
        uname = sc.next();
        try {

            File file = new File("Users/"+uname+".txt"); //gets the file into file object.
            if (file.exists()) {
                System.out.println("Username already exist, try something else");
                registerUser();
                return;
            } else {
                System.out.println("Username is valid ");
                FileWriter fw = new FileWriter(file);  //writes into  file.
                sc.nextLine();
                System.out.println("Please enter your password: ");
                password=sc.nextLine();
                System.out.println("Please re-enter the password: ");
                p2=sc.nextLine();

                while(!p2.equals(password)){
                    System.out.println("You must have made mistake writing the password, please add it again ");
                    System.out.println("Please enter your password: ");
                    password=sc.nextLine();
                    System.out.println("Please re-enter the password: ");
                    p2=sc.nextLine();
                }

                System.out.println("For secure authentication we will ask you four questions, Please remember while log in");

                System.out.println("What is your favourite colour? ");
                q1=sc.nextLine();

                System.out.println("What is your mother's maiden name?");
                q2=sc.nextLine();

                System.out.println("What primary school did you attend?");
                q3=sc.nextLine();

                System.out.println("What was the house number you lived in as a child? ");
                q4=sc.nextLine();
                System.out.println("----------------------------------------------------------------");
                System.out.println("Press 1. to confirm the details, else press any number.");
                System.out.println("username: "+uname);
                System.out.println("What is your favourite colour ? "+q1);
                System.out.println("What is your mother's maiden name? "+q2);
                System.out.println("What primary school did you attend? "+q3);
                System.out.println("What was the house number you lived in as a child?  "+q4);
                System.out.println("----------------------------------------------------------------");
                String x=sc.nextLine();
                if(x.equals("1"))
                {
					//Change here for single file structure.
                    fw.append("Password:"+sha256(password)+"\nQ1:"+q1+"\nQ2:"+q2+"\nQ3:"+q3+"\nQ4:"+q4);
                    fw.close();
                    System.out.println("User Created Successfully ");
                }else
                {
                    fw.close();
                    file.delete();
                    registerUser();
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public boolean loginUser() {
        String response = "1";
        Scanner sc = new Scanner(System.in);

        while (response.equals("1")) {

            System.out.println("Please enter username: ");
            String uname = sc.next();
            System.out.println("Please enter password: ");
            String pw = sha256(sc.next());
            String pwFromFile;
            sc.nextLine();

            try {
                pwFromFile = getPassword(uname);

                if (pwFromFile.equals(pw)) {
                    String[] securityQuestion = getSecurityQuestion(uname);
                    System.out.println(securityQuestion[0]);
                    String ans = sc.nextLine();

                    if (ans.equalsIgnoreCase(securityQuestion[1])) {
                        Context.setUserName(uname);
                        return true;
                    } else {
                        System.out.println("Incorrect answer");
                    }

                } else {
                    System.out.println("Incorrect password.");
                }

            } catch (FileNotFoundException e) {
                System.out.println("Username does not exist.");
            }
            System.out.println("Enter 1 to try to login again, else enter any other key.");
            response = sc.next();
            sc.nextLine();
        }
        return false;
    }

    /*
     * Returns the encrypted password from the file for the given username
     */
    private String getPassword(String uname) throws FileNotFoundException {
        File file = new File("Users/" + uname + ".txt");
        Scanner sc = new Scanner(file);
        String pw = null;
        while (sc.hasNext() && pw==null) {
            String line = sc.nextLine();
            if (line.contains("Password:")) {
                pw = line.split("Password:")[1];
            }
        }
        return pw;
    }

    /*
     * Returns a random security question and answer pair for the given username
     */
    private String[] getSecurityQuestion(String uname) throws FileNotFoundException {
        File file = new File("Users/" + uname + ".txt");
        Scanner sc = new Scanner(file);
        int question = new Random().nextInt(4)+1;
        String[] ans = null;

        while (sc.hasNext() && ans==null) {
            String line = (sc.nextLine());
            if (line.contains("Q" + question + ":")) {
                ans = line.split("Q" + question + ":");
            }
        }

        switch (question) {
            case 1:
                ans[0] = "What is your favourite colour? ";
                break;
            case 2:
                ans[0] = "What is your mother's maiden name?";
                break;
            case 3:
                ans[0] = "What primary school did you attend?";
                break;
            case 4:
                ans[0] = "What was the house number you lived in as a child? ";
                break;
        }
        return ans;
    }


    /*
     * Used for creating hashes of the mobile user's identity
     *
     */
    public  String sha256( String base) {
        try{
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(base.getBytes("UTF-8"));
            final StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                final String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString(); //returns the SHA-256 Hash as String
        } catch(Exception ex){
            System.out.println("Couldn't create SHA256 Hash");
            System.exit(0);
            return ""	;
        }
    }

    public void forgotPassword() {
        while (true) {
            Scanner sc = new Scanner(System.in);

            System.out.println("Please confirm your username: ");
            String uname = sc.nextLine();
            boolean securityFlag = true;
            try {
                String[] securityQuestion1 = getSecurityQuestion(uname);
                System.out.println(securityQuestion1[0]);
                String ans = sc.nextLine();

                if (ans.equalsIgnoreCase(securityQuestion1[1])) {
                    while (securityFlag) {

                        String[] securityQuestion2 = getSecurityQuestion(uname);
                        if (!securityQuestion2[0].equals(securityQuestion1[0])) {
                            System.out.println(securityQuestion2[0]);
                            String ans2 = sc.nextLine();
                            if (ans2.equalsIgnoreCase(securityQuestion2[1])) {
                                System.out.println("Please enter your password: ");
                                String password = sc.nextLine();
                                System.out.println("Please re-enter the password: ");
                                String p2 = sc.nextLine();

                                while (!p2.equals(password)) {
                                    System.out.println("You must have made mistake writing the password, please add it again ");
                                    System.out.println("Please enter your password: ");
                                    password = sc.nextLine();
                                    System.out.println("Please re-enter the password: ");
                                    p2 = sc.nextLine();
                                }

                                File f = new File("Users/" + uname + ".txt");
                                Scanner sc_file = new Scanner(f);
                                StringBuilder sb=new StringBuilder();

                                while (sc_file.hasNext()) {
                                    String line = sc_file.nextLine();
                                    if (line.contains("Password:")) {
                                        line ="Password:"+sha256(password);
                                    }
                                    sb.append(line).append("\n");
                                }
                                BufferedWriter fileWriter = new BufferedWriter(new FileWriter("Users/" + uname + ".txt"));
                                fileWriter.write(sb.toString());
                                fileWriter.close();
                                System.out.println("Password Update successful");
                                return;
                            } else {
                                System.out.println("Incorrect answer");
                                securityFlag = true;
                            }
                        } else {
                            securityFlag = true;
                        }
                    }

                } else {
                    System.out.println("Incorrect answer");
                }
            } catch (Exception e) {
                System.out.println("Username does not exist. Press 1 to try again or any other key to return to main menu");
                String input =  sc.nextLine();
                if(!input.equals("1")) {
                    System.out.println();
                    break;
                }
            }
        }
    }

    public void getQueries() {
        boolean loggedIn = true;
        Scanner sc = new Scanner(System.in);
        while (loggedIn) {
            System.out.println("Hello " + Context.getUserName() + ". Please choose: ");
            System.out.println("1. Enter query");
            System.out.println("2. Logout");
            int choice;
            try {
                choice = sc.nextInt();
            } catch (InputMismatchException e) {
                choice = -1;
            }
            sc.nextLine();
            switch (choice) {
                case 1:
                    System.out.println("Enter query: ");
                    StringBuilder sb = new StringBuilder();
                    String input;
                    boolean isTransaction = false;

                    do {
                        input = sc.nextLine();
                        sb.append(input);
                        if (input.toUpperCase().startsWith("START TRANSACTION:")) {
                            isTransaction = true;
                        }
                        if (!isTransaction && input.contains(";")){
                            break;
                        }
                        if (isTransaction && input.toUpperCase().contains("COMMIT;")){
                            break;
                        }
                    } while (sc.hasNext());
                    QueryParser qp = new QueryParser(new Tokenizer(sb));
                    try {
                        qp.parse();
                    } catch (InvalidQueryException | LockTimeOutException e) {
                        System.out.println(e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println();
                        QueryLog queryLog=new QueryLog();
                        Logger queryLogger=queryLog.setLogger();
                        queryLogger.info("User Name: "+Context.getUserName() +"\nQuery: "+sb.toString());
                    break;
                case 2:
                    Context.logout();
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid input, please try again.\n");
            }
        }
    }
}
