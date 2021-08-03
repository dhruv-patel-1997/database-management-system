package main.java.queries;

public class Column {

    private String colName;
    private String dataType;
    private Boolean allowNulls;
    private Boolean primaryKey;
    private ForeignKey foreignKey;

    public Column(String colName, String dataType){
        this.colName = colName;
        this.dataType = dataType;
        allowNulls = true;
        primaryKey = false;
        foreignKey = null;
    }

    public void setAllowNulls(boolean allowNulls){
        this.allowNulls = allowNulls;
    }

    public void  setAsPrimaryKey(Boolean bool){
        primaryKey = bool;
        if (bool){
            setAllowNulls(false);
        }
    }

    public void setForeignKey(ForeignKey key){
        this.foreignKey = key;
    }

    public ForeignKey getForeignKey(){
        return foreignKey;
    }

    public Boolean isPrimaryKey(){
        return primaryKey;
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

    public void set(String dataType){
        this.dataType=dataType;
        this.colName=getColName();
        this.foreignKey=getForeignKey();
        this.allowNulls=getAllowNulls();
        this.privateKey=isPrivateKey();
    }
}
