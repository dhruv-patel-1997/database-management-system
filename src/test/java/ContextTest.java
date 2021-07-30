package test.java;

import main.java.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;


import static org.junit.jupiter.api.Assertions.*;

class ContextTest {

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
  void getDbName() {

    Assertions.assertTrue(Context.setDbName("Database1"));
  }
  @Test
  void setDbName() {
    String expectedOutput="Database1";
    Context.setDbName("Database1");
    Assertions.assertEquals(expectedOutput, Context.getDbName());
  }
  @Test
  void isTableExist() {
    Context.setDbName("Database1");
    Assertions.assertTrue(Context.isTableExist("employee"));
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