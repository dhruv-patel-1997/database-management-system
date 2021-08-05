import Utilities.Context;
import main.java.exceptions.LockTimeOutException;
import Utilities.TableUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Test {

  public static void main(String [] args) throws IOException, LockTimeOutException {

    System.out.println("university's Insert statements for current time");
    System.out.println();
    Context.setDbName("university");
    ArrayList<String> insertStatements = TableUtils.getInsertStatements(Context.getDbName());
    for(int i=0;i<insertStatements.size();i++)
      System.out.println(insertStatements.get(i));

  //Testing it for Database1 too;
    System.out.println("\n\n");
    System.out.println("Database1's Insert statements for current time");
    System.out.println();
    Context.setDbName("Database1");
    insertStatements = TableUtils.getInsertStatements(Context.getDbName());
    for(int i=0;i<insertStatements.size();i++)
      System.out.println(insertStatements.get(i));

  }

}
