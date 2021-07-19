public class testing {
public enum t {
    TESTTYPE
}
    public static void main(String[] args){
        t type = t.TESTTYPE;
        System.out.println(type.toString());

        String s = "|||test|1|";
        String[] ss = s.split("\\|");
        int i = 1;
        for (String s1: ss){
            System.out.println(i+ s1);
            i++;
        }

        System.out.println(Token.getTokenOfType("VARCHAR"));

    }
}
