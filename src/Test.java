import main.java.Context;

public class Test {

  public static void main(String [] args)
  {
    System.out.println(Context.setDbName("Database1"));
    System.out.println(Context.getDbName());
    System.out.println(Context.isTableExist("employee"));

  }
}
