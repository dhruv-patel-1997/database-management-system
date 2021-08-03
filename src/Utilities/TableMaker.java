package Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TableMaker {

  private void printSpace(int n) {
    for(int i = 0; i < n; i++)
      System.out.print(" ");
  }
  private void printPipe() {
    System.out.print("|");
  }
  private void printDash(int n) {
    for(int i = 0; i < n; i++)
      System.out.print("-");
  }
  private void print(String s) {
    System.out.print(s);
  }
  public void printTable(HashMap<String, ArrayList<String>> tableData) {
    for(int i = 0; i < tableData.size(); i++) {
      printDash(30);
      print("+");
    }
    ArrayList<String> headings = new ArrayList<>();
    int totalRowSize=0;
    for(Map.Entry<String, ArrayList<String>> entry: tableData.entrySet()) {
      headings.add(entry.getKey());
      if(entry.getValue().size()>totalRowSize)
        totalRowSize=entry.getValue().size();
    }
    System.out.println();
    printPipe();
    for(int i=0;i<headings.size();i++)
    {
      print(headings.get(i));
      printSpace(30-headings.get(i).length());
      printPipe();
    }
    System.out.println();
    for(int i = 0; i < tableData.size(); i++) {
      printDash(30);
      print("+");
    }
    for(int i=0;i<totalRowSize;i++)
    {
      System.out.println();
      printPipe();
      for(int j=0;j<headings.size();j++) {
        print(tableData.get(headings.get(j)).get(i));
        printSpace(30-tableData.get(headings.get(j)).get(i).length());
        printPipe();
      }
      System.out.println();
      for(int k = 0; k < tableData.size(); k++) {
        printDash(30);
        print("+");
      }
    }
System.out.println();
  }

}
