import Utilities.Context;
import main.java.queries.TableUtils;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class Test {

  public static void main(String [] args) throws FileNotFoundException {
    Context.setDbName("Database1");
    HashMap<String,String> hmp = new HashMap<>();
    hmp.put("empId","ep10");
    hmp.put("empName","xyz");
    hmp.put("empAddress"," ");
    hmp.put("empSalary","125");
    TableUtils.insertRow("Database1","employee",hmp);
  }
}
