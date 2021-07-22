package main.java.queries;

public class Column {
    public enum noArgDataType {
        TEXT,DECIMAL,INT,BOOLEAN
    }
    public enum argDataType {
        VARCHAR
    }

    private String colName;
    private String dataType;
    private String dataTypeArg;
    private Boolean allowNulls;

    public Column(String colName, noArgDataType dataType){
        this.colName = colName;
        this.dataType = dataType.toString();
        allowNulls = true;
    }

    public Column(String colName, argDataType dataType, String dataTypeArg){
        this.colName = colName;
        this.dataType = dataType.toString();
        this.dataTypeArg = dataTypeArg;
        allowNulls = true;
    }

    public void setAllowNulls(boolean allowNulls){
        this.allowNulls = allowNulls;
    }

    public String getColName() {
        return colName;
    }

    public String getDataType() {
        return dataType;
    }

    public String getDataTypeArg() {
        return dataTypeArg;
    }

    public Boolean getAllowNulls() {
        return allowNulls;
    }
}
