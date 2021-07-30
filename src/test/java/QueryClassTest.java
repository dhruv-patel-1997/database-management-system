package test.java;
import main.java.QueryClass;

import static org.junit.jupiter.api.Assertions.*;

class QueryClassTest {

    @org.junit.jupiter.api.Test
    void registerUser() {
    }

    @org.junit.jupiter.api.Test
    void loginUser() {
    }

    @org.junit.jupiter.api.Test
    void sha256() {
      QueryClass qc = new QueryClass();
       String actual_output= qc.sha256("DataWarehousing");
       String expected_output = "d277905bede7636d2859567c17d602a68e724c8afdde9c8577b27a958fb38a07";
       assertEquals(expected_output,actual_output,"SHA-256 function not working");
    }

    @org.junit.jupiter.api.Test
    void forgotPassword() {
    }
}