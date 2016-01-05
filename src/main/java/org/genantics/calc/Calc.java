/**
 * Copyright (C) Bob Foster 2012. All rights reserved.
 * 
 * This software is provided under the terms of the Apache License, Version 2.0
 * A copy of the license is available at http://www.apache.org/licenses/LICENSE-2.0.html.
 * 
 * Contributors:
 * 
 *    Bob Foster, author.
 */
package org.genantics.calc;

import java.util.List;
import org.genantics.peggen.Node;

public class Calc {
  
  public static void main(String[] args) {
    // Just catenate all the args together with spaces in between.
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < args.length; i++) {
      if (i > 0)
        sb.append(" ");
      sb.append(args[i]);
    }
    String arg = sb.toString().trim();
    Calc calc = new Calc(arg);
    if (arg.length() > 0) {
      ExprSimple parser = new ExprSimple();
      Node[] tree = parser.parseLanguage(sb.toString());
      if (tree == null) {
        List errors = parser.getErrors();
        for (Object err : errors)
          System.err.println(err.toString());
        System.exit(1);
      }
      try {
        prettyPrint(calc.eval(tree[0]));
      } catch (Exception e) {
        System.err.println("Evaluation error: "+e.getMessage());
      }
    }
  }
  
  private static void prettyPrint(double d) {
    // Could do fancier formatting, but just want integers to
    // look like integers.
    long ld = (long) d;
    if ((double)ld == d)
      System.out.println(ld);
    else
      System.out.println(d);
  }
  
  private Calc(String expr) {
    this.expr = expr;
  }
  
  private String expr;
  
  //========================== eval ==================================
  
  /**
   * Evaluate a Node of unknown type.
   * 
   * Since Nodes are generic, evaluating them is not object-oriented
   * or as fast as it might be, but it's probably as fast as it needs
   * to be for most applications. Note the use of == to compare node
   * names. This is valid in Java as long as all values have been
   * set to and compared with String constants, as they are interned.
   */
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


  private double evalUnary(Node node) throws Exception {
    // Unary only appears if MINUS was specified
    return eval(node.child.next);
  }
  
  private double evalPower(Node node) throws Exception {
    // Power only appears if there are exactly two operands.
    return Math.pow(eval(node.child), eval(node.child.next));
  }
  
  private double evalProd(Node node) throws Exception {
    double result = eval(node.child);
    // Start with second child and process two at a time.
    for (Node child = node.child.next; child != null; child = child.next.next) {
      if (child.name == "MUL")
        result *= eval(child.next);
      else
        result /= eval(child.next);
    }
    return result;
  }

  private double evalSum(Node node) throws Exception {
    // Just like evalProd with different operations.
    double result = eval(node.child);
    for (Node child = node.child.next; child != null; child = child.next.next) {
      if (child.name == "ADD")
        result += eval(child.next);
      else
        result -= eval(child.next);
    }
    return result;
  }

  private double evalNUMBER(Node node) throws Exception {
    // Whitespace in this grammar can be easily trimmed.
    // Whitespace that includes comments would require more thought.
    String num = expr.substring(node.offset, node.offset+node.length).trim();
    return Double.parseDouble(num);
  }
}
