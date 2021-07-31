package test.java.queries;


import main.java.queries.TableUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableUtilsTest {

  private static String dbName = "Database1";
  private static String tableName = "employee";

  @BeforeAll
  public static void init(){

    String path  = "Databases/Database1";
    File f1 = new File(path);
    f1.mkdir();
    String fileName = "Databases/Database1/employee.txt";
    String str = "empId|ep1|ep2|ep3|ep4\n" +
            "empName|Leah|Harit|Dhruv|Saurabh\n" +
            "empAddress|Halifax|Ahmedabad|Halifax|Culcutta\n" +
            "empSalary|120|120|111|134";
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
      writer.write(str);
      writer.close();
    }
    catch(Exception e)
    {
      System.out.println("Exception occurred");
    }
  }


  @Test
  public void getColumnsForGivenColumns() {
    HashMap<String, ArrayList<String>> columns1 = new HashMap<>();
    ArrayList<String> empId = new ArrayList<>();
    empId.add("ep1");
    empId.add("ep2");
    empId.add("ep3");
    empId.add("ep4");
    ArrayList<String> empName = new ArrayList<>();
    empName.add("Leah");
    empName.add("Harit");
    empName.add("Dhruv");
    empName.add("Saurabh");
    ArrayList<String> empAddress = new ArrayList<>();
    empAddress.add("Halifax");
    empAddress.add("Ahmedabad");
    empAddress.add("Halifax");
    empAddress.add("Culcutta");
    ArrayList<String> empSalary = new ArrayList<>();
    empSalary.add("120");
    empSalary.add("120");
    empSalary.add("111");
    empSalary.add("134");
    columns1.put("empId", empId);
    columns1.put("empName", empName);
    columns1.put("empAddress", empAddress);
    columns1.put("empSalary", empSalary);
    ArrayList<String> columnNames = new ArrayList<>();
    columnNames.add("empId");
    columnNames.add("empName");
    columnNames.add("empAddress");
    columnNames.add("empSalary");
    try {
      assertEquals(TableUtils.getColumns(dbName, tableName, columnNames), columns1);
    } catch (Exception e) {
      System.out.println("Exception occured");
    }
  }
  @Test
  public void getColumnsForAll() {
    HashMap<String, ArrayList<String>> columns1 = new HashMap<>();
    ArrayList<String> empId = new ArrayList<>();
    empId.add("ep1");
    empId.add("ep2");
    empId.add("ep3");
    empId.add("ep4");
    ArrayList<String> empName = new ArrayList<>();
    empName.add("Leah");
    empName.add("Harit");
    empName.add("Dhruv");
    empName.add("Saurabh");
    ArrayList<String> empAddress = new ArrayList<>();
    empAddress.add("Halifax");
    empAddress.add("Ahmedabad");
    empAddress.add("Halifax");
    empAddress.add("Culcutta");
    ArrayList<String> empSalary = new ArrayList<>();
    empSalary.add("120");
    empSalary.add("120");
    empSalary.add("111");
    empSalary.add("134");
    columns1.put("empId", empId);
    columns1.put("empName", empName);
    columns1.put("empAddress", empAddress);
    columns1.put("empSalary", empSalary);
    try {
      assertEquals(TableUtils.getColumns(dbName, tableName), columns1);
    } catch (Exception e) {
      System.out.println("Exception occured");
    }
  }

  @AfterAll
  public static void reset(){
    File directory = new File("DataBases/Database1");
    File[] files = directory.listFiles();
    for (File f: files){
      f.delete();
    }
    directory.delete();
  }


}
