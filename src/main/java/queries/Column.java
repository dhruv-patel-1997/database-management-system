package main.java.queries;

public class Column {

    private String colName;
    private String dataType;
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

    public void  setAsPrivateKey(Boolean bool){
        privateKey = bool;
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
