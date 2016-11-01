package com.tekai;

import static org.junit.Assert.assertEquals;
import static com.tekai.Expression.e;
import static com.tekai.Helpers.word;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.tekai.Expression;

public class PrinterTest {

    ParserTest p;
    String sql;

    public PrinterTest(){
        p = new ParserTest();
        sql = "";
    }

    @Test
    public void basicPrint() throws Exception {
        assertEquals("1 + 2", print(e(" +", "ARITHMETIC", e("1", "NUMBER"), e(" 2", "NUMBER"))));
        assertEquals("ABC(A, B)", print(e("ABC", "FUNCTION", e("A", "IDENTIFIER"), e(" B", "IDENTIFIER"))));
        assertEquals("ABC(A, DEF(1, 2, 3))",
         print(e("ABC", "FUNCTION", e("A", "IDENTIFIER"), e(" DEF", "FUNCTION", e("1", "NUMBER"), e(" 2", "NUMBER"), e(" 3", "NUMBER")))));
    }

    @Test
    public void testFrom(){
        sql = "SELECT campo FROM tabela";
        assertEquals(sql, print(p.parse(sql)));
        sql = "SELECT tabela.xx10 AS campo1, campo2,  campo3  FROM  tabela as tb1, tabela3";
        assertEquals(sql, print(p.parse(sql)));
        sql = "SELECT DISTINCT campo FROM tabela";
        assertEquals(sql, print(p.parse(sql)));
    }

    @Test
    public void functions(){
        sql = "SELECT getdate(), campo, campo2,  campo3  FROM  tabela, tabela3";
        assertEquals(sql, print(p.parse(sql)));
        sql = "SELECT POSITION('abc' IN campo2) FROM  tabela";
        assertEquals(sql, print(p.parse(sql)));
        sql = "SELECT cast(campo3 as VARCHAR) FROM  tabela";
        assertEquals(sql, print(p.parse(sql)));
    }

    @Test
    public void testWhere(){
        sql = "SELECT campo FROM tabela where campo = 2";
        assertEquals(sql, print(p.parse(sql)));
        sql = "SELECT trim(campo), campo FROM tabela, tabela2 WHERE tabela.campo = 2 AND tabela.id = 3";
        assertEquals(sql, print(p.parse(sql)));
    }

    @Test
    public void testConcat(){
        sql = "SELECT campo1 || 'string' || abc(campo3, campo4) FROM tabela";
        assertEquals(sql, print(p.parse(sql)));
    }

    @Test
    public void testOrder(){
        sql = "SELECT  * FROM tabela WHERE campo = 2 AND campo2 = campo1 ORDER BY campo2, campo3, campo4";
        assertEquals(sql, print(p.parse(sql)));
        sql = "SELECT  * FROM tabela WHERE campo = 2 AND campo2 = campo1 ORDER BY campo2, campo3 DESC, campo4 DESC";
        assertEquals(sql, print(p.parse(sql)));
    }

    @Test
    public void testLimit(){
        sql = "SELECT  * FROM tabela WHERE campo = 2  LIMIT 10";
        assertEquals(sql, print(p.parse(sql)));
        sql = "SELECT  * FROM tabela WHERE campo = :id ORDER BY campo2 LIMIT 10 OFFSET 0";
        assertEquals(sql, print(p.parse(sql)));
    }

    @Test
    public void testGroup(){
        sql = "SELECT ax050.idinterno, ax050.descricao,    "
            + "                        'sistema' = RTRIM(ax050.idinterno) || ' - ' || RTRIM(ax050.descricao)    "
            + "           FROM AXT05000 AS ax050    "
            + "            WHERE ax050.Descricao like 'SERVIÇOS DE TI%'  "
            + " GROUP BY campo1, campo2";
        assertEquals(sql, print(p.parse(sql)));
    }

    @Test
    public void testCase(){
        sql = "SELECT  CASE campo"
            + " WHEN 1  THEN  'Aguarda DistribuiÃ§Ã£o'"
            + " WHEN 2  THEN  'Em AnÃ¡lise'"
            + " ELSE ''  END "
            + " FROM sat00100 as sa001";
        assertEquals(sql, print(p.parse(sql)));
        sql = "SELECT CASE x*5 "
                + "WHEN 1 THEN msg = 'one or two' "
                + "ELSE msg = 'other value than one or two'"
                + "END "
                + "FROM TABELA";
        assertEquals(sql, print(p.parse(sql)));
    }

    @Test
    public void testJoin(){
        sql = "SELECT C120.idcomercial, "
            + "        C120.idnome, "
            + "        X040.razsoc, "
            + "        X040.docto1 as cnpj,  "
            + "        X030.nomcid AS municipio,  "
            + "        X030.uf AS uf, "
            + "        chave_acesso = '                              ', "
            + "        data_acesso = '00/00/0000 00:00:00', "
            + "        X040.docto2 AS inscricao "
            + " FROM ACT12000 AS C120"
            + " INNER JOIN AXT04000 AS X040 ON X040.idnome = C120.idnome "
            + "   INNER JOIN AXT02000 AS X020A ON X020A.idparametro = C120.sitsis "
            + "   INNER JOIN AXT02000 AS X020B ON X020B.idparametro = C120.sitcom "
            + "   INNER JOIN AXT02000 AS X020C ON X020C.idparametro = C120.sitlas "
            + "   INNER JOIN AXT03000 AS X030  ON X030.idcidade     = X040.idcidade";
        assertEquals(sql, print(p.parse(sql)));
    }

    @Test
    public void testParenthesis(){
        sql = "1 + (2 * (3 - 1))";
        assertEquals(sql, print(p.parse(sql)));
    }

    @Test
    public void subselect(){
        sql = "SELECT * from tabela where not exists(SELECT * from tabela)";
        assertEquals(sql, print(p.parse(sql)));
    }

    private String print(Expression e) {
        if (e.isType("SQL")) {
            return printChildren(e.getChildren(), "");
        } else if (e.isType("SELECT")) {
            if (e.getChild(0).isType("DISTINCT")) {
                return e.printValue() + print(e.getChildren().remove(0)) + printChildren(e.getChildren());
            } else {
                return e.printValue() + printChildren(e.getChildren());
            }
        } else if(e.isType("FROM")) {
            return e.printValue() + printFrom(e.getChildren());
        } else if (e.isType("GROUP") || e.isType("ORDER")) {
            return e.printValue() + printChildren(e.getChildren());
        } else if (e.isType("LIMIT")
                        || e.isType("OFFSET")
                        || e.isType("CASE")
                        || e.isType("WHEN")
                        || e.isType("WHERE")
                        || e.isType("THEN")
                        || e.isType("ELSE")
                        || e.isType("NOT")
                        || e.isType("JOIN")
                        || e.isType("ON")) {
            return e.printValue() + printChildren(e.getChildren(), "");
        } else if (e.isType("CONCAT")) {
            return printChildren(e.getChildren(), e.printValue());
        } else if (e.isType("PARENTHESIS")) {
            return e.printValue() + printChildren(e.getChildren()) + ")";
        } else if (e.isType("FUNCTION")) {
            StringBuilder result = new StringBuilder();
            String separator = (e.hasValue(word("POSITION")) ? " IN" : ",");
            result.append(e.printValue()).append("(");
            result.append(printChildren(e.getChildren(), separator));
            return result.append(")").toString();
        } else if (e.isType("ARITHMETIC")
                        || e.isType("BOOLEAN")
                        || e.isType("LIKE")
                        || e.isType("ALIAS")
                        || e.isType("OPERATOR")
                        || e.isType("IS")) {
            return print(e.getChild(0)) + e.printValue() + print(e.getChild(1));
        } else if (e.isType("ORDERING")) {
            return print(e.getChild(0)) + e.printValue();
        } else {
            return e.printValue();
        }
    }

     protected String printFrom(List<Expression> e) {
        StringBuilder result = new StringBuilder();

        Iterator<Expression> iterator = e.iterator();
        if (iterator.hasNext())
            result.append(print(iterator.next()));

        while (iterator.hasNext()) {
            Expression exp = iterator.next();
            result.append(exp.isType("JOIN") ? "" : ",");
            result.append(print(exp));
        }

        return result.toString();
    }

    private String printChildren(List<Expression> e) {
        return printChildren(e, ",");
    }

    private String printChildren(List<Expression> e, String separator) {
        StringBuilder result = new StringBuilder();

        Iterator<Expression> iterator = e.iterator();
        if (iterator.hasNext())
            result.append(print(iterator.next()));

        while (iterator.hasNext()) {
            result.append(separator);
            result.append(print(iterator.next()));
        }

        return result.toString();
    }
}