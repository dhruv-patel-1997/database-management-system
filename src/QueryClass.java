import java.io.File;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.util.Scanner;

public class QueryClass {

    public void registerUser() {
        String uname, password,p2, q1, q2, q3, q4;
        System.out.println("Please Enter your details");
        System.out.println("Enter username(no space allowed) : ");
        Scanner sc = new Scanner(System.in);
        uname = sc.next();
        try {

            File file = new File("Databases/Users/" + uname + ".txt"); //gets the file into file object.
            if (file.exists()) {
                System.out.println("Username already exist, try something else");
                file.delete();
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
                int x=sc.nextInt();
                if(x==1)
                {
                    fw.write("Password:"+sha256(password)+"\nQ1:"+q1+"\nQ2:"+q2+"\nQ3:"+q3+"\nQ4:"+q4);
                    fw.close();
                    System.out.println("User Created Successfully ");
                }else
                {
                    file.delete();
                    registerUser();
                    return;
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
    /*
     * Used for creating hashes of the mobile user's identity
     *
     */
    public static String sha256(final String base) {
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
}
