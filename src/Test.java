import Utilities.Context;
import main.java.queries.TableUtils;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class Test {

  public static void main(String [] args) throws FileNotFoundException {
    Context.setDbName("Database1");
    HashMap<String,String> hmp = new HashMap<>();
    hmp.put("empId","ep9");
    hmp.put("empName","abc");
    hmp.put("empAddress","New York");
    hmp.put("empSalary","");
    TableUtils.insertRow("Database1","employee",hmp);
  }
}
