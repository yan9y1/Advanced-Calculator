package project;

import java.util.*;
import java.io.*;

/**
 * This class is the model in the MVC architecture.
 * The model performs computations and notifies the view
 * with the result. The model should use your code to
 * evaluate SMAC commands and compute the result
 */
public class SMACmodel extends Observable {

	/**
	 * evaluates the input and notify
	 * the view with the result (a String)
	 */
	private Evaluator ev = new Evaluator();
   	public void eval(String input) {
   		ev.sets(input);
   		String r = "";
   		
   		if(ev.ifLog()) {     //If the current state is log state 
   			r = ">>" + input + "\n";
   			ev.addLogContent(">>" + input + "\r\n");
   		}else r = ">" + input + "\n";   
   		
   		//If the input is a mathematical expression 
   		if(ev.isMath()) {
   			//Check whether parentheses are matched in mathematical expressions 
   			if(ev.checkDelimiter()) {
   				//Check the mathematical expression
   				if(ev.checkMathExpression()) {
   					MathematicalEvaluator e = new MathematicalEvaluator(input,ev.precision);
   					String result = e.evaluate();
   					setLast(result,ev);
   					r += result;
   					if(ev.ifLog()) ev.addLogContent(result + "\r\n\r\n");
   				}else {    //The mathematical expression is wrong
   					r += "Syntax error: malformed expression";
   				}
   			}else {    //The parentheses in the mathematical expression are wrong 
   				r += "Syntax error: delimiters not balance";
   			}
   		}else {     //If the input is not a mathematical expression 
   			String handleI = ev.handleIdentifier();
   			r += handleI;
   			if(ev.ifLog()) ev.addLogContent(handleI + "\r\n\r\n");
   		}
   		setChanged();
		notifyObservers(r);
   	}
   	
   	//Store the value of last
   	 void setLast(String last,Evaluator ee) {
   		 try {
   			 Tokenizer t = new Tokenizer(last);
   			 if(t.peekNextToken().isOperator() && t.peekNextToken().getOperator().equals("~")) {
   				 t.readNextToken();
   				 ee.last = 0 - t.peekNextToken().getNumber();
   			 }else {
   				ee.last = t.peekNextToken().getNumber();
   			 } 
   		 }catch(Exception e) {
   			 System.out.println("ERROR: " + e.getMessage());
   		 }
   	 }
   	 
}
