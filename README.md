# peggen-calc
Simple example using peggen grammar and peggen-maven-plugin.

Grammar
=======

The grammar defines a very simple expression syntax involving numbers, parenthesis
and the operators - (unary and binary), +, *, /, and ^ (exponentiation), allowing
whitespace between terms.

```
#=====================================================================
# Expression grammar example using simple syntax
#
# The grammar illustrates the "Fordian" style of whitespace
# handling. While no distinction is made in PEG parsers
# between lexical and grammatical rules (the uppercase
# rule names affect only the human reader) separating
# out the "lexical" rules allows a uniform treatment of
# whitepace, and removes the clutter from the "grammatical"
# rules. Note that S appears only once above the break,
# at the start of the grammar.
#
# Matched rules are Node values in the output except:
#
#   - Rules with name followed by ~, like Unary
#   - Rules with name followed by ~n where the body does not
#     match at least n rules, like Sum, Prod and Power.
#
# Thus, the only Nodes that appear in the parser output
# unconditionally are Unary, MUL, DIV, ADD, SUB, MINUS and NUMBER.
# Sum, Prod and Power may appear if they have more than
# one child.
#
# The . term matches any character. !. does not match any
# character, and hence matches the end of input.
#=====================================================================

Grammar~  =  S? Sum !.
Sum~2     =  Prod ((ADD | SUB) Prod)*
Prod~2    =  Power ((MUL | DIV) Power)*
Power~2   =  Unary (POW Power)*
Unary~2   =  MINUS? Term
Term~     =  NUMBER | LEFT Sum RIGHT

POW~      =  '^' S?
LEFT~     =  '(' S?
RIGHT~    =  ')' S?
MUL       =  '*' S?
DIV       =  '/' S?
ADD       =  '+' S?
SUB       =  '-' S?
MINUS     =  '-' S?
NUMBER    =  DIGIT+ ('.' DIGIT+ | [eE] [-+]? DIGIT+ )? S?
DIGIT~    =  [0-9]
S~        =  [ \t\r\n]+
```

Interpreter
===========

The parser generates a Node tree that must be traversed to produce further results.
For this example, a simple interpreter walks the tree and evaluates an expression
read from the command line arguments.

The interpreter source is in org.genantics.calc.Calc.

Build
=====

Clone project and
```
mvn clean install
```

Run
===

For example:
```
java -jar target/peggen-calc-1.0-SNAPSHOT.jar 1+2*3
```

Notes
=====

Java developers should not freak out at the following code sequence in the interpreter.
```
  private double eval(Node node) throws Exception {
    String name = node.name;
    if (name == "NUMBER")
      return evalNUMBER(node);
    if (name == "Sum")
      return evalSum(node);
    if (name == "Prod")
      return evalProd(node);
    if (name == "Power")
      return evalPower(node);
    if (name == "Unary")
      return evalUnary(node);
    throw new Exception("Unexpected Node type "+name);
  }
```
Yes, == is used to compare node names with string literals. This works because all
Node names are produced from string literals. As you know, literals are interned,
so this is equivalent to an equals check but faster.

It's a feature. The last thing you want in a compiler is more per-character processing.
