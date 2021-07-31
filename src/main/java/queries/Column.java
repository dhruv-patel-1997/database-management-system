package main.java.queries;

import java.util.ArrayList;

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
    private Boolean privateKey;
    private ForeignKey foreignKey;

    public Column(String colName, String dataType){
        this.colName = colName;
        this.dataType = dataType;
        allowNulls = true;
        privateKey = false;
        foreignKey = null;
    }

    public void setAllowNulls(boolean allowNulls){
        this.allowNulls = allowNulls;
    }

    public void  setPrivateKey(Boolean bool){
        privateKey = bool;
        setAllowNulls(false);
    }

    public void setForeignKey(ForeignKey key){
        this.foreignKey = key;
    }

    public ForeignKey getForeignKey(){
        return foreignKey;
    }

    public Boolean isPrivateKey(){
        return privateKey;
    }

    public String getColName() {
        return colName;
    }

    public String getDataType() {
        return dataType;
    }

    public Boolean getAllowNulls() {
        return allowNulls;
    }
}