# tekai-parser

A Pratt Parser implementation with no Lexer
===========================================

Tekai is based on [this text](http://journal.stuffwithstuff.com/2011/03/19/pratt-parsers-expression-parsing-made-easy) by [Bob Nystrom](http://www.stuffwithstuff.com/bob-nystrom.html) (a.k.a. ["munificent"](https://github.com/munificent)), author of [Magpie](http://github.com/munificent/magpie). He describes how Pratt Parsers works and he shows, step by step, how to program your own parser (the article is awesome!). Since we need a simple parser for our own purposes ([Fuser project](https://github.com/ruliana/Fuser)), I gave it a try and the results are pretty good.

As we need something a bit simpler to program, instead of using a Lexer to generate tokens, I used regular expressions right in the "parselets" (little components that parse a single "rule"), what makes Tekai a parser with no lexer phase. I'm not sure that fully classifies it as a ["Scannerless Parser"](http://en.wikipedia.org/wiki/Scannerless_parsing), but it serves well to its purpose.

Here is an example:

```java
import tekai.Parser;
import tekai.standard.AtomParselet;
import tekai.standard.InfixParselet;
import tekai.standard.PostfixParselet;
.
.
.
// Precedence: Higher means grouped before others
int x = 1;
int ATOM_PRECENDENCE = x++;
int SUM_PRECENDENCE = x++;
int MULT_PRECENDENCE = x++;
int POSTFIX_PRECENDENCE = x++;

Parser parser = new Parser("1 + 2++ * 3");

// Rules: Tries to parse in this order, more specific rules comes first.
// Change the order of "PLUSONE" and "PLUS" and see what happens
parser.register(new PostfixParselet(POSTFIX_PRECENDENCE, "\\+{2}", "PLUSONE"));
parser.register(new InfixParselet(MULT_PRECENDENCE, "\\*", "MULT"));
parser.register(new InfixParselet(SUM_PRECENDENCE, "\\+", "PLUS"));
parser.register(new AtomParselet(ATOM_PRECENDENCE, "\\d+", "NUMBER"));

Expresssion result = parser.parse();
```

The code above results in the following AST:

    +               (PLUS)
    |__ 1           (NUMBER)
    |__ *           (MULT)
        |__ ++      (PLUSONE)
        |   |__ 2   (NUMBER)
        |__ 3       (NUMBER)

This tree is represented by the following string when using the "toString" method of "Expression" (that makes tests easier, however, it's not easy to read):

```java
"([+]:PLUS [1]:NUMBER ([*]:MULT ([++]:PLUSONE [2]:NUMBER) [3]:NUMBER))"
```