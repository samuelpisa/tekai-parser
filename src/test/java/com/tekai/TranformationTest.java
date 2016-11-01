package com.tekai;

import static org.junit.Assert.assertEquals;
import static com.tekai.Expression.e;
import static com.tekai.standard.CommonTransformation.from;
import static com.tekai.standard.CommonTransformation.fromType;

import org.junit.Test;
import com.tekai.Expression;
import com.tekai.Transformation;

import com.tekai.standard.MultiTransformation;

public class TranformationTest {

    @Test
    public void rename() throws Exception {
        Expression expression = e("SUBSTRING", "FUNCTION",
                        e("'Some text'", "STRING" ),
                        e("0", "NUMBER"),
                        e("4", "NUMBER"));

        Transformation transformation = from("SUBSTRING", "FUNCTION").toValue("SUBSTR");
        Expression result = transformation.applyOn(expression);

        assertEquals("([SUBSTR]:FUNCTION ['Some text']:STRING [0]:NUMBER [4]:NUMBER)", result.toString());
    }

    @Test
    public void changeParameterOrder() throws Exception {
        Expression expression = e("SUBSTRING", "FUNCTION",
                        e("'Some text'", "STRING" ),
                        e("0", "NUMBER"),
                        e("4", "NUMBER"));

        Transformation transformation = from("SUBSTRING", "FUNCTION").toParamOrder(1, 3, 2);
        Expression result = transformation.applyOn(expression);

        assertEquals("([SUBSTRING]:FUNCTION ['Some text']:STRING [4]:NUMBER [0]:NUMBER)", result.toString());
    }

    @Test
    public void remove() throws Exception {
        Expression expression = e("SUBSTRING", "FUNCTION",
                        e("'Some text'", "STRING" ),
                        e("0", "NUMBER"),
                        e("4", "NUMBER"));

        Transformation transformation = fromType("STRING").toNothing();
        Expression result = transformation.applyOn(expression);

        assertEquals("([SUBSTRING]:FUNCTION [0]:NUMBER [4]:NUMBER)", result.toString());
    }

    /**
     * Converte a Função CAST(campo as VARCHAR) do Postgres para Oracle
     * o VARCHAR:DATATYPE do Postgres deve passar para VARCHAR2:DATATYPE
     */
    @Test
    public void cast(){

       Expression expression = e("CAST", "FUNCTION",
               e("campo", "IDENTIFIER"),
               e("VARCHAR", "DATATYPE"));


       Transformation transformation = from("VARCHAR", "DATATYPE").toValue("VARCHAR2");
       Expression result = transformation.applyOn(expression);

       assertEquals("([CAST]:FUNCTION [campo]:IDENTIFIER [VARCHAR2]:DATATYPE)", result.toString());
    }

    /**
     * Converte a função CAST do Oracle em CONVERT do SqlServer
     */
    @Test
    public void castToConvert() {
        MultiTransformation t = new MultiTransformation();
        t.register(from("CAST", "FUNCTION").toValue("CONVERT").toParamOrder(2, 1));
        t.register(from("VARCHAR2", "DATATYPE").toValue("VARCHAR"));

        Expression expression = e("CAST", "FUNCTION",
                        e("campo", "IDENTIFIER"),
                        e("VARCHAR2", "DATATYPE"));

        Expression result = t.applyOn(expression);

        assertEquals("([CONVERT]:FUNCTION [VARCHAR]:DATATYPE [campo]:IDENTIFIER)", result.toString());
    }

    @Test
    public void concat(){
         Expression expression = e("||", "CONCAT",
               e("campo", "IDENTIFIER"),
               e("campo2", "IDENTIFIER"));


       Transformation transformation = from("\\|\\|", "CONCAT").toValue("CONCAT").toType("FUNCTION");
       Expression result = transformation.applyOn(expression);

       assertEquals("([CONCAT]:FUNCTION [campo]:IDENTIFIER [campo2]:IDENTIFIER)", result.toString());
    }
}
