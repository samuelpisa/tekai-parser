package com.tekai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static com.tekai.Helpers.word;

import java.util.regex.Pattern;

import org.junit.Test;

import com.tekai.Expression;
import com.tekai.Parselet;
import com.tekai.Parser;
import com.tekai.standard.AtomParselet;
import com.tekai.standard.BeforeMiddleAfterParselet;
import com.tekai.standard.InfixParselet;
import com.tekai.standard.PostfixParselet;
import com.tekai.standard.PrefixParselet;

public class ParserTest {

    @Test
    public void justAnAtom() {
        assertParsing("Just a number", "[1]:NUMBER", "1");
        assertParsing("Just an identifier", "[abc]:IDENTIFIER", "abc");
    }

    @Test
    public void simpleExpression() {
        assertParsing("Simple infix", "([+]:ARITHMETIC [1]:NUMBER [2]:NUMBER)", "1 + 2");
        assertParsing("Double infix (left associativity)", "([+]:ARITHMETIC ([+]:ARITHMETIC [1]:NUMBER [2]:NUMBER) [3]:NUMBER)", "1 + 2 + 3");
        assertParsing("Double infix with parenthesis", "([+]:ARITHMETIC [1]:NUMBER ([(]:PARENTHESIS ([+]:ARITHMETIC [2]:NUMBER [3]:NUMBER)))", "1 + (2 + 3)");
    }

    @Test
    public void functions() {
        assertParsing("[abc]:FUNCTION", "abc()");
        assertParsing("([abc]:FUNCTION [1]:NUMBER)", "abc(1)");
        assertParsing("([abc]:FUNCTION [1]:NUMBER [2]:NUMBER)", "abc(1, 2)");
        assertParsing("([abc]:FUNCTION [1]:NUMBER [2]:NUMBER [3]:NUMBER)", "abc(1, 2, 3)");
        assertParsing("([+]:ARITHMETIC ([abc]:FUNCTION [4]:NUMBER) ([def]:FUNCTION [3]:NUMBER [2]:NUMBER))", "abc(4) + def(3, 2)");
        assertParsing("([abc]:FUNCTION ([+]:ARITHMETIC ([(]:PARENTHESIS ([+]:ARITHMETIC [2]:NUMBER [1]:NUMBER)) [3]:NUMBER))", "abc((2 + 1) + 3)");
        assertParsing("([+]:ARITHMETIC ([(]:PARENTHESIS ([+]:ARITHMETIC ([+]:ARITHMETIC [1]:NUMBER ([abc]:FUNCTION ([+]:ARITHMETIC [2]:NUMBER [3]:NUMBER) [4]:NUMBER)) [5]:NUMBER)) [6]:NUMBER)", "(1 + abc(2 + 3, 4) + 5) + 6");
        assertParsing("([abc]:FUNCTION ([def]:FUNCTION [1]:NUMBER) ([ghi]:FUNCTION [2]:NUMBER))", "abc(def(1), ghi(2))");
        assertParsing("([position]:FUNCTION ['abc']:STRING [column]:IDENTIFIER)", "position('abc' in column)");
        assertParsing("([CAST]:FUNCTION ([as]:ALIAS [column2]:IDENTIFIER [VARCHAR]:IDENTIFIER))", "CAST(column2 as VARCHAR)");
    }

    @Test
    public void selectFrom() {
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [column1]:IDENTIFIER [column2]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER [table2]:IDENTIFIER))", "SELECT column1, column2 FROM table, table2");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER))", "SELECT * FROM table");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER ([INNER JOIN]:JOIN [outra_table]:IDENTIFIER ([ON]:ON [xxx]:IDENTIFIER))))", "SELECT * FROM table INNER JOIN outra_table ON xxx");

    }

    @Test
    public void selectWithWhere(){
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER) ([WHERE]:WHERE ([+]:ARITHMETIC [column]:IDENTIFIER [2]:NUMBER)))", "SELECT  * FROM table WHERE column + 2");

        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER) ([WHERE]:WHERE ([=]:OPERATOR [table.column1]:IDENTIFIER [table.column2]:IDENTIFIER)))", "SELECT  * FROM table WHERE table.column1 = table.column2");

        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER) ([WHERE]:WHERE ([AND]:BOOLEAN ([=]:OPERATOR [column]:IDENTIFIER [2]:NUMBER) ([=]:OPERATOR [id]:IDENTIFIER [3]:NUMBER))))",
            "SELECT * FROM table WHERE column = 2 AND id = 3");

        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER) ([WHERE]:WHERE ([OR]:BOOLEAN ([AND]:BOOLEAN ([=]:OPERATOR [column]:IDENTIFIER [2]:NUMBER) ([=]:OPERATOR [id]:IDENTIFIER [:param]:PARAMETER)) ([=]:OPERATOR [column]:IDENTIFIER [5.5]:NUMBER))))",
            "SELECT * FROM table WHERE column = 2 AND id = :param OR column = 5.5");

        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER) ([WHERE]:WHERE ([OR]:BOOLEAN ([AND]:BOOLEAN ([(]:PARENTHESIS ([=]:OPERATOR [column]:IDENTIFIER [2]:NUMBER)) ([=]:OPERATOR [id]:IDENTIFIER [35.89]:NUMBER)) ([(]:PARENTHESIS ([=]:OPERATOR [column]:IDENTIFIER [5]:NUMBER)))))",
            "SELECT * FROM table WHERE (column = 2) AND id = 35.89 OR (column = 5)");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER) ([WHERE]:WHERE ([>]:OPERATOR [column]:IDENTIFIER [2]:NUMBER)))", "SELECT  * FROM table WHERE column >2");
    }

    @Test
    public void selectWithAlias(){
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [tb.column1]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER))", "SELECT tb.column1 FROM table");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([AS]:ALIAS [column]:IDENTIFIER [nome]:IDENTIFIER)) ([FROM]:FROM [table]:IDENTIFIER))", "SELECT column AS nome FROM table");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([AS]:ALIAS [tb.column1]:IDENTIFIER [nome]:IDENTIFIER)) ([FROM]:FROM [table]:IDENTIFIER))", "SELECT tb.column1 AS nome FROM table");
    }

    @Test
    public void selectWithConcat(){
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([||]:CONCAT [column1]:IDENTIFIER [column2]:IDENTIFIER)) ([FROM]:FROM [table]:IDENTIFIER))", "SELECT column1 || column2 FROM table");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([||]:CONCAT [column1]:IDENTIFIER [column2]:IDENTIFIER [column3]:IDENTIFIER)) ([FROM]:FROM [table]:IDENTIFIER))",
                "SELECT column1 || column2 || column3 FROM table");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([||]:CONCAT [column1]:IDENTIFIER [column2]:IDENTIFIER ([abc]:FUNCTION [column3]:IDENTIFIER [column4]:IDENTIFIER))) ([FROM]:FROM [table]:IDENTIFIER))",
                "SELECT column1 || column2 || abc(column3, column4) FROM table");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([||]:CONCAT [column1]:IDENTIFIER ['string']:STRING ([abc]:FUNCTION [column3]:IDENTIFIER [column4]:IDENTIFIER))) ([FROM]:FROM [table]:IDENTIFIER))",
                "SELECT column1 || 'string' || abc(column3, column4) FROM table");
    }

    @Test
    public void selectWithJoin(){
        String expected =
"([SQL]:SQL\n" +
"  ([SELECT]:SELECT\n" +
"    [C120.idcomercial]:IDENTIFIER\n" +
"    [C120.idnome]:IDENTIFIER\n" +
"    [X040.razsoc]:IDENTIFIER\n"  +
"    ([as]:ALIAS [X040.docto1]:IDENTIFIER [cnpj]:IDENTIFIER)\n"  +
"    ([AS]:ALIAS [X030.nomcid]:IDENTIFIER [municipio]:IDENTIFIER)\n" +
"    ([AS]:ALIAS [X030.uf]:IDENTIFIER [uf]:IDENTIFIER)\n" +
"    ([=]:ALIAS [chave_acesso]:IDENTIFIER ['                              ']:STRING)\n" +
"    ([=]:ALIAS [data_acesso]:IDENTIFIER ['00/00/0000 00:00:00']:STRING)\n" +
"    ([AS]:ALIAS [X040.docto2]:IDENTIFIER [inscricao]:IDENTIFIER))\n" +
"  ([FROM]:FROM\n" +
"    ([AS]:ALIAS [ACT12000]:IDENTIFIER [C120]:IDENTIFIER)\n" +
"    ([INNER JOIN]:JOIN\n" +
"      ([AS]:ALIAS [X4000]:IDENTIFIER [X040]:IDENTIFIER)\n"  +
"      ([ON]:ON ([=]:OPERATOR [X040.idnome]:IDENTIFIER [C120.idnome]:IDENTIFIER)))\n" +
"    ([INNER JOIN]:JOIN\n" +
"      ([AS]:ALIAS [X2000]:IDENTIFIER [X020A]:IDENTIFIER)\n" +
"      ([ON]:ON ([=]:OPERATOR [X020A.idparametro]:IDENTIFIER [C120.sitsis]:IDENTIFIER)))\n" +
"    ([INNER JOIN]:JOIN\n" +
"      ([AS]:ALIAS [X2000]:IDENTIFIER [X020B]:IDENTIFIER)\n" +
"      ([ON]:ON ([=]:OPERATOR [X020B.idparametro]:IDENTIFIER [C120.sitcom]:IDENTIFIER)))\n" +
"    ([INNER JOIN]:JOIN\n" +
"      ([AS]:ALIAS [X2000]:IDENTIFIER [X020C]:IDENTIFIER)\n" +
"      ([ON]:ON ([=]:OPERATOR [X020C.idparametro]:IDENTIFIER [C120.sitlas]:IDENTIFIER)))\n" +
"    ([INNER JOIN]:JOIN\n" +
"      ([AS]:ALIAS [X3000]:IDENTIFIER [X030]:IDENTIFIER)\n" +
"      ([ON]:ON ([=]:OPERATOR [X030.idcidade]:IDENTIFIER [X040.idcidade]:IDENTIFIER)))))";

        Pattern spaces = Pattern.compile("\n\\s+", Pattern.MULTILINE);
        assertParsing(spaces.matcher(expected).replaceAll(" "), " SELECT C120.idcomercial, "
            + "        C120.idnome, "
            + "        X040.razsoc, "
            + "        X040.docto1 as cnpj,  "
            + "        X030.nomcid AS municipio,  "
            + "        X030.uf AS uf, "
            + "        chave_acesso = '                              ', "
            + "        data_acesso = '00/00/0000 00:00:00', "
            + "        X040.docto2 AS inscricao "
            + " FROM ACT12000 AS C120 "
            + " INNER JOIN X4000 AS X040 ON X040.idnome = C120.idnome "
            + "   INNER JOIN X2000 AS X020A ON X020A.idparametro = C120.sitsis "
            + "   INNER JOIN X2000 AS X020B ON X020B.idparametro = C120.sitcom "
            + "   INNER JOIN X2000 AS X020C ON X020C.idparametro = C120.sitlas "
            + "   INNER JOIN X3000 AS X030  ON X030.idcidade     = X040.idcidade ");
     }

     @Test
    public void selectOrder(){
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER) ([WHERE]:WHERE ([=]:OPERATOR [column]:IDENTIFIER [2]:NUMBER)) ([ORDER BY]:ORDER [column2]:IDENTIFIER))", "SELECT  * FROM table WHERE column = 2 ORDER BY column2");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER) ([WHERE]:WHERE ([=]:OPERATOR [column]:IDENTIFIER [2]:NUMBER)) ([ORDER BY]:ORDER [column2]:IDENTIFIER [column3]:IDENTIFIER [column4]:IDENTIFIER))", "SELECT  * FROM table WHERE column = 2 ORDER BY column2, column3, column4");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER) ([ORDER BY]:ORDER [column2]:IDENTIFIER ([DESC]:ORDERING [column3]:IDENTIFIER) ([ASC]:ORDERING [column4]:IDENTIFIER)))", "SELECT  * FROM table ORDER BY column2, column3 DESC, column4 ASC");
    }

    @Test
    public void selectLimit(){
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER) ([WHERE]:WHERE ([=]:OPERATOR [column]:IDENTIFIER [2]:NUMBER)) ([LIMIT]:LIMIT [10]:NUMBER))", "SELECT  * FROM table WHERE column = 2  LIMIT 10");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER) ([WHERE]:WHERE ([=]:OPERATOR [column]:IDENTIFIER [2]:NUMBER)) ([ORDER BY]:ORDER [column2]:IDENTIFIER) ([LIMIT]:LIMIT [10]:NUMBER ([OFFSET]:OFFSET [0]:NUMBER)))", "SELECT  * FROM table WHERE column = 2 ORDER BY column2 LIMIT 10 OFFSET 0");
    }

     @Test
    public void TestCase(){
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([CASE]:CASE [column]:IDENTIFIER ([WHEN]:WHEN [1]:NUMBER ([THEN]:THEN ['waiting']:STRING)) ([WHEN]:WHEN [2]:NUMBER ([THEN]:THEN ['ok']:STRING)) ([ELSE]:ELSE ['']:STRING) [END]:END)) ([FROM]:FROM ([as]:ALIAS [sat00100]:IDENTIFIER [sa001]:IDENTIFIER)))",
              " SELECT  CASE column"
            + " WHEN 1  THEN  'waiting'"
            + " WHEN 2  THEN  'ok'"
            + " ELSE ''  END "
            + " FROM sat00100 as sa001");

        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([CASE]:CASE ([*]:ARITHMETIC [x]:IDENTIFIER [5]:NUMBER) ([WHEN]:WHEN [1]:NUMBER ([THEN]:THEN ([=]:OPERATOR [msg]:IDENTIFIER ['one or two']:STRING))) ([ELSE]:ELSE ([=]:OPERATOR [msg]:IDENTIFIER ['other value than one or two']:STRING)) [END]:END)) ([FROM]:FROM [table]:IDENTIFIER))",
                "SELECT CASE x*5 "
                + "WHEN 1 THEN msg = 'one or two' "
                + "ELSE msg = 'other value than one or two'"
                + "END "
                + "FROM table");

    }

     @Test
    public void subSelect(){
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [column]:IDENTIFIER) ([FROM]:FROM ([(]:PARENTHESIS ([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER)))))",
                "SELECT column FROM (SELECT * FROM table)");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [column]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER) ([WHERE]:WHERE ([EXISTS]:FUNCTION ([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER)))))",
                "SELECT column FROM table WHERE EXISTS(SELECT * FROM table)");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT ([CASE]:CASE [column]:IDENTIFIER ([WHEN]:WHEN ([EXISTS]:FUNCTION ([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [table]:IDENTIFIER))) ([THEN]:THEN ['ok']:STRING)) [END]:END)) ([FROM]:FROM [table]:IDENTIFIER))",
                "SELECT CASE column WHEN EXISTS(SELECT * FROM table) THEN 'ok' END FROM table");


    }

     @Test
     public void SelectGroup(){
          assertParsing("([SQL]:SQL ([SELECT]:SELECT [DISTINCT]:DISTINCT [ax050.idinterno]:IDENTIFIER [ax050.descricao]:IDENTIFIER ([||]:CONCAT ([=]:OPERATOR ['sistema']:STRING ([RTRIM]:FUNCTION [ax050.idinterno]:IDENTIFIER)) [' - ']:STRING ([RTRIM]:FUNCTION [ax050.descricao]:IDENTIFIER))) ([FROM]:FROM ([AS]:ALIAS [X5000]:IDENTIFIER [ax050]:IDENTIFIER)) ([WHERE]:WHERE ([like]:LIKE [ax050.Descricao]:IDENTIFIER ['TI%']:STRING)) ([GROUP BY]:GROUP [column1]:IDENTIFIER [column2]:IDENTIFIER))",
                "SELECT DISTINCT ax050.idinterno, ax050.descricao,    "
            + "                        'sistema' = RTRIM(ax050.idinterno) || ' - ' || RTRIM(ax050.descricao)    "
            + "           FROM X5000 AS ax050    "
            + "            WHERE ax050.Descricao like 'TI%'  "
            + " GROUP BY column1, column2");
     }

     @Test
     public void notIs(){
         assertParsing("([NOT]:NOT [1]:NUMBER)", "NOT 1");
         assertParsing("([NOT]:NOT ([abc]:FUNCTION [1]:NUMBER))", "NOT abc(1)");
         assertParsing("([NOT  LIKE]:LIKE [column]:IDENTIFIER ['teste']:STRING)", "column NOT  LIKE 'teste'");
         assertParsing("([is]:IS [column]:IDENTIFIER [null]:IDENTIFIER)", " column is null");
         assertParsing("([IS]:IS [column]:IDENTIFIER ([NOT]:NOT [NULL]:IDENTIFIER))", "column IS NOT NULL");
     }

    @Test
    public void exceptions() {
        // TODO Launch specific exception to specific problems
        // TODO Add more and more contextual information to error messages
        try {
            parse("1 +");
            fail("Expected not able to parse an incomplete expression \"1 +\"");
        } catch (Exception e) {
            // success
        }
    }

    // == Helpers ==

    private void assertParsing(String expected, String source) {
        assertParsing(null, expected, source);
    }

    private void assertParsing(String message, String expected, String source) {
        Expression expression = parse(source);
        assertEquals(message, expected, expression.toString());
    }

    public Expression parse(String source) {
        Parser parser = new Parser(source);
        configureParser(parser);
        Expression expression = parser.parse();
        return expression;
    }

    private void configureParser(Parser parser) {
        // PRECEDENCE (What to parse first. Higher numbers means more precedence)
        int x = 1;
        final int ATOM = x++;
        final int OR = x++;
        final int AND = x++;
        final int NOT = x++;
        final int IS = x++;
        final int LIKE = x++;
        final int POS = x++;
        final int IN = x++;
        final int LOGICOPER = x++;
        final int EQUALS = x++;
        final int MULTIPLY = x++;
        final int SUM = x++;
        final int GROUPING = x++;
        final int GROUP = x++;
        final int FUNCTION = x++;
        final int CASE = x++;
        final int SELECT = x++;

        // SQL
        parser.register(new Parselet(SELECT) {
            @Override
            public boolean isPrefixParselet() {
                return true;
            }

            @Override
            public String startingRegularExpression() {
                return word("SELECT");
            }

            @Override
            public Expression parse() {
                Expression result = new Expression("SQL", "SQL");

                Expression columns = new Expression("SELECT", "SELECT");

                if(canConsume(word("DISTINCT"))){
                    columns.addChildren(new Expression("DISTINCT", lastMatch()));
                }

                do {
                    Expression column = nextExpression();
                    if (column.isType("OPERATOR")) {
                        Expression substitute = new Expression("ALIAS", column.printValue());
                        substitute.addChildren(column.getChildren());
                        column = substitute;
                    }
                    columns.addChildren(column);
                } while (canConsume(","));

                consumeIf(word("FROM"));

                Expression from = new Expression("FROM", lastMatch());
                do {
                    from.addChildren(nextExpression());
                } while(canConsume(","));

                while (canConsume(word("(?:INNER|RIGHT|LEFT)?\\s+JOIN|JOIN"))) {
                    Expression join = new Expression("JOIN", lastMatch());
                    join.addChildren(nextExpression());
                    consumeIf(word("ON"));
                    Expression on = new Expression("ON", lastMatch());
                    on.addChildren(nextExpression());
                    join.addChildren(on);
                    from.addChildren(join);
                }
                result.addChildren(columns, from);

                if(canConsume(word("WHERE"))){
                    Expression where = new Expression("WHERE", lastMatch());
                    where.addChildren(nextExpression());
                   result.addChildren(where);
                }

                if(canConsume(word("GROUP BY"))){
                    Expression group = new Expression("GROUP", lastMatch());
                    do{
                        group.addChildren(nextExpression());
                    }while(canConsume(","));
                    result.addChildren(group);
                }

               // result.addChildren(nextExpression());

               /* if(canConsume("\\b((?i)HAVING)\\b")){
                    Expression having = new Expression("HAVING", "HAVING");
                    do{
                        having.addChildren(nextExpression());
                    }while(canConsume("\\,"));
                    result.addChildren(having);
                }*/

                if(canConsume(word("ORDER BY"))){
                    Expression order = new Expression("ORDER", lastMatch());
                    do {
                        Expression descOrder = nextExpression();
                        //if(canConsume("\\b((?i)ASC|DESC)\\b"))
                         //   descOrder.addChildren(new Expression("ORDERING", lastMatch()));
                        order.addChildren(descOrder);
                    } while(canConsume(","));

                    result.addChildren(order);
                }

                if(canConsume(word("LIMIT"))){
                    Expression limit = new Expression("LIMIT", lastMatch());
                    limit.addChildren(nextExpression());
                    if(canConsume(word("OFFSET"))){
                        Expression offset = new Expression("OFFSET", lastMatch());
                        offset.addChildren(nextExpression());
                        limit.addChildren(offset);
                    }
                    result.addChildren(limit);
                }

                return result;
            }
        });

         //CASE
         parser.register(new Parselet(CASE) {

            @Override
            public boolean isPrefixParselet() {
                return true;
            }

            @Override
            public String startingRegularExpression() {
                return word("CASE");
            }

            @Override
            protected Expression parse() {
                Expression ecase = new Expression("CASE", lastMatch());

                do{
                    if(canConsume(word("WHEN"))){
                        Expression when = new Expression("WHEN", lastMatch());
                        when.addChildren(nextExpression());

                        consumeIf(word("THEN"));
                        Expression then = new Expression("THEN", lastMatch());
                        then.addChildren(nextExpression());
                        when.addChildren(then);
                        ecase.addChildren(when);
                    }else if(canConsume(word("ELSE"))){
                        Expression eelse = new Expression("ELSE", lastMatch());
                        eelse.addChildren(nextExpression());
                        ecase.addChildren(eelse);
                    }else
                        ecase.addChildren(nextExpression());
                }while(cannotConsume(word("END")));
                ecase.addChildren(new Expression("END", lastMatch()));

                return ecase;
            }
        });

        // BOOLEAN
        parser.register(new InfixParselet(OR, word("OR"), "BOOLEAN"));
        parser.register(new InfixParselet(AND, word("AND"), "BOOLEAN"));
        parser.register(new PrefixParselet(NOT, word("NOT"), "NOT"));

        //LIKE
        parser.register(new InfixParselet(LIKE, word("NOT\\s+LIKE|LIKE"), "LIKE"));

        // ARITHMETIC
        parser.register(new InfixParselet(MULTIPLY, "(\\*|/|%)", "ARITHMETIC"));
        parser.register(new InfixParselet(SUM, "(\\+|-)", "ARITHMETIC"));

        //ALIAS
        parser.register(new InfixParselet(ATOM, word("AS"), "ALIAS"));

        //EQUALS (OPERATOR)
        parser.register(new InfixParselet(LOGICOPER, "\\>\\=", "OPERATOR"));
        parser.register(new InfixParselet(LOGICOPER, "\\<\\=", "OPERATOR"));
        parser.register(new InfixParselet(LOGICOPER, "\\<\\>", "OPERATOR"));
        parser.register(new InfixParselet(EQUALS, "\\=", "OPERATOR"));
        parser.register(new InfixParselet(EQUALS, "\\>", "OPERATOR"));
        parser.register(new InfixParselet(EQUALS, "\\<", "OPERATOR"));

        //IS
        parser.register(new InfixParselet(IS, word("IS"), "IS"));
        
        //CONCAT
        parser.register(new BeforeMiddleAfterParselet(ATOM, null, "\\|\\|", null, "CONCAT"));

        //GROUP BY
        parser.register(new BeforeMiddleAfterParselet(GROUP, word("GROUP BY"), "\\,", null, "GROUPBY"));
        //parser.register(new BeforeMiddleAfterParselet(ORDER, "\\b((?i)HAVING)\\b", "\\,", null, "HAVING"));

        // GROUPING (parenthesis)
        //parser.register(new GroupingParselet(GROUPING, "\\(", "\\)"));
        parser.register(new BeforeMiddleAfterParselet(GROUPING, "\\(", null, "\\)", "PARENTHESIS"));

        // FUNCTION
        parser.register(new BeforeMiddleAfterParselet(FUNCTION, "(\\w+)\\s*\\(", "\\,|"+word("IN"), "\\)", "FUNCTION"));

        //ORDER BY
        //parser.register(new BeforeMiddleAfterParselet(ORDER, "\\b((?i)ORDER\\s+BY)\\b", ",", "(\\b((?i)ASC)\\b|\\b((?i)DESC)\\b)?", "ORDER BY"));

        //POSTFIX ASC DESC
        parser.register(new PostfixParselet(POS, word("ASC|DESC"), "ORDERING"));
        //NUMBER
        parser.register(new AtomParselet(ATOM, "\\d+(?:\\.\\d+)?", "NUMBER"));

        //STRING
        parser.register(new AtomParselet(ATOM, "\\'[^\\']*?\\'", "STRING"));

        //IDENTIFIER
        parser.register(new AtomParselet(ATOM, "(\\w+\\.\\w+|\\w+|\\*)", "IDENTIFIER"));

        //PARAMETER
        parser.register(new AtomParselet(ATOM, "(\\:\\w+)", "PARAMETER"));
    }
}
