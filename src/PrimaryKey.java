import java.util.LinkedList;

public class PrimaryKey {
    private final LinkedList<String> columnNames;

    public PrimaryKey(LinkedList<String> columnNames) {
        this.columnNames = columnNames;
    }

    public LinkedList<String> getColumnNames(){
        return columnNames;
    }
}
