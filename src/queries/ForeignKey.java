package queries;

public class ForeignKey {
    private final String colname;
    private final String referencedTable;
    private final String referencedColumn;

    public ForeignKey (String colName, String referencedTable, String referencedColumn){
        this.colname = colName;
        this.referencedTable = referencedTable;
        this.referencedColumn = referencedColumn;
    }

    public String getColname(){
        return colname;
    }

    public String getReferencedTable() {
        return referencedTable;
    }

    public String getReferencedColumn() {
        return referencedColumn;
    }
}
