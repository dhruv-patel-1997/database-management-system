package test.java.parsing;

import main.java.parsing.InvalidQueryException;
import main.java.parsing.Token;
import main.java.parsing.Tokenizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

public class TokenizerTest {


    @Test
    public void queryTest() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("Select* from tableName\n Where rowName = -1;");
        assertTrue(tokenizer.next().getType() == Token.Type.SELECT
                && tokenizer.next().getType() == Token.Type.STAR
                && tokenizer.next().getType() == Token.Type.FROM
                && tokenizer.next().getType() == Token.Type.IDENTIFIER
                && tokenizer.next().getType() == Token.Type.WHERE
                && tokenizer.next().getType() == Token.Type.IDENTIFIER
                && tokenizer.next().getType() == Token.Type.EQUAL
                && tokenizer.next().getType() == Token.Type.INTLITERAL
                && tokenizer.next().getType() == Token.Type.SEMICOLON);
    }

    @Test
    public void queryTest2() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("Select* from tableName +-6 ;");
        assertTrue(tokenizer.next().getType() == Token.Type.SELECT
                && tokenizer.next().getType() == Token.Type.STAR
                && tokenizer.next().getType() == Token.Type.FROM
                && tokenizer.next().getType() == Token.Type.IDENTIFIER);
        assertThrows(InvalidQueryException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                tokenizer.next();
            }
        });
    }

    @Test
    public void semiColonTest() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer(";");
        assertTrue(tokenizer.next().getType() == Token.Type.SEMICOLON);
    }

    @Test
    public void commaTest() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer(",");
        assertTrue(tokenizer.next().getType() == Token.Type.COMMA);
    }

    @Test
    public void starTest() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("*");
        assertTrue(tokenizer.next().getType() == Token.Type.STAR);
    }

    @Test
    public void openTest() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("(");
        assertTrue(tokenizer.next().getType() == Token.Type.OPEN);
    }

    @Test
    public void closeTest() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer(")");
        assertTrue(tokenizer.next().getType() == Token.Type.CLOSED);
    }

    @Test
    public void keywordTest() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("select");
        assertTrue(tokenizer.next().getType() == Token.Type.SELECT);
    }

    @Test
    public void intTest1() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("1");
        assertTrue(tokenizer.next().getType() == Token.Type.INTLITERAL);
    }
    @Test
    public void intTest2() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("+1");
        assertTrue(tokenizer.next().getType() == Token.Type.INTLITERAL);
    }

    @Test
    public void intTest3() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("-1");
        assertTrue(tokenizer.next().getType() == Token.Type.INTLITERAL);
    }

    @Test
    public void decTest1() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("1.0");
        assertTrue(tokenizer.next().getType() == Token.Type.DECIMALLITERAL);
    }

    @Test
    public void decTest2() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("+1.0");
        assertTrue(tokenizer.next().getType() == Token.Type.DECIMALLITERAL);
    }

    @Test
    public void decTest3() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("-1.0");
        assertTrue(tokenizer.next().getType() == Token.Type.DECIMALLITERAL);
    }

    @Test
    public void decTest4() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("+.0");
        assertTrue(tokenizer.next().getType() == Token.Type.DECIMALLITERAL);
    }

    @Test
    public void decTest5() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("-.0");
        assertTrue(tokenizer.next().getType() == Token.Type.DECIMALLITERAL);
    }

    @Test
    public void decTest6() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("1.0.0");
        Token t = tokenizer.next();
        assertTrue(t.getType() == Token.Type.DECIMALLITERAL
                && t.getStringValue().matches("1.0"));
    }

    @Test
    public void decTest7() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer(".1");
        assertTrue(tokenizer.next().getType() == Token.Type.DECIMALLITERAL);
    }

    @Test
    public void identifierTest1() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("identifier_test");
        Token t = tokenizer.next();
        System.out.println(t.getStringValue());
        assertTrue(t.getType() == Token.Type.IDENTIFIER
                && t.getStringValue().matches("identifier_test"));
    }

    @Test
    public void identifierTest2() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("identifiertest2");
        Token t = tokenizer.next();
        assertTrue(t.getType() == Token.Type.IDENTIFIER
                && t.getStringValue().matches("identifiertest2"));
    }

    @Test
    public void identifierTest3() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("-identifiertest");
        assertThrows(InvalidQueryException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                tokenizer.next();
            }
        });
    }

    @Test
    public void stringTest1() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("\"this is a string\"");
        Token t = tokenizer.next();
        assertTrue(t.getType() == Token.Type.STRING
                && t.getStringValue().matches("\"this is a string\""));
    }

    @Test
    public void stringTest2() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("\"this is a \\\" string\"");
        Token t = tokenizer.next();
        assertTrue(t.getType() == Token.Type.STRING
                && t.getStringValue().equals("\"this is a \\\" string\""));
    }

    @Test
    public void stringTest3() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer("\"this is not a string");
        assertThrows(InvalidQueryException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                tokenizer.next();
            }
        });
    }

    @Test
    public void singleCharTokenTest() throws InvalidQueryException {
        Tokenizer tokenizer = new Tokenizer(",*();");
        assertTrue(tokenizer.next().getType() == Token.Type.COMMA
                && tokenizer.next().getType() == Token.Type.STAR
                && tokenizer.next().getType() == Token.Type.OPEN
                && tokenizer.next().getType() == Token.Type.CLOSED
                && tokenizer.next().getType() == Token.Type.SEMICOLON);
    }
}
