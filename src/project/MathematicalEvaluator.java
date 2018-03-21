package project;

import java.util.*;

public class MathematicalEvaluator {
	private String s;
	private int precision;
	private Stack<Double> operands;
	private Stack<String> operators;
	public MathematicalEvaluator(String s,int precision){
		this.s = s;
		this.precision = precision;;
		operands = new Stack<Double>();
		operators = new Stack<String>();
	}
	//Calculate the result of expressions
	public String evaluate() {
		double result = 0.0;
		String error = "";
		try {
			Tokenizer tokenizer = new Tokenizer(s);
			while(tokenizer.hasNextToken()) {				
				Token t = tokenizer.peekNextToken();
				if(t.isNumber()) {
					operands.push(t.getNumber());
				}else if(t.isOperator()) {
					handleOperators(t.getOperator());
				}else if(t.isDelimiter()) {
					handleDelimiter(t.getDelimiter());
				}else if (t.isIdentifier()) {
					String key = t.getIdentifier();
					if(Evaluator.map.containsKey(key)) {
						double value = Evaluator.map.get(key);
						operands.push(value);
					}else if(key.equals("last")) {
						operands.push(Evaluator.last);
					}else if(key.equals("sin") || key.equals("cos")) {
						handleOperators(key);
					}else{
						error = "Error: " + t.getIdentifier() + " is not a variable";
						throw new GeneralErrorException(t.getIdentifier() + " is not a variable");
					}
				}
				tokenizer.readNextToken();
			}
			//Deal with the rest of the stack
			if(!operands.isEmpty()) result = operands.peek();
			while(!operators.isEmpty()) {
				FunOp opt = new FunOp(operators.pop());
				if(opt.getArity() == 1) {
					double num = operands.pop();
					result = compute(0,num,opt.getName());
					operands.push(result);
				}else if(opt.getArity() == 2) {
					double num2 = operands.pop();
					double num1 = operands.pop();
					if(num2 == 0 && opt.getName().equals("/")) {
						error = "Error: division by zero";
						throw new GeneralErrorException("division by zero");
					}else{
						result = compute(num1,num2,opt.getName());
						operands.push(result);
					}
				}
			}
			
		}catch(Exception e) {
			System.out.println("\nERROR: " + e.getMessage());
		}
		if(!error.equals("")) return error;
		else if(precision == -1) return result + "";
		else return String.format("%." + precision + "f",result);
	}
	
	//Process operator
	public void handleOperators(String opt) throws GeneralErrorException{
		if(operators.isEmpty()) {
			operators.push(opt);
		}else {
			do {
				String topOpt = operators.peek();
				if(topOpt.equals("(")) {
					break;
				}else {
					FunOp opt1 = new FunOp(operators.peek());
					FunOp opt2 = new FunOp(opt);
					if(opt1.getPrority() < opt2.getPrority() || (opt1.getPrority() == opt2.getPrority() && opt1.getArity() == 1)) {
						break;
					}else if(opt1.getArity() == 2) {    //二元操作符
						Double num2 = operands.pop();
						Double num1 = operands.pop();
						Double result = compute(num1,num2,opt1.getName());
						operands.push(result);
						operators.pop();
					}else if(opt1.getArity() == 1) {    //一元操作符
						Double num = operands.pop();
						Double result = compute(0,num,opt1.getName());
						operands.push(result);
						operators.pop();
					}
				}
			}while(!operators.isEmpty());
			operators.push(opt);
		}
	}
	
	//Process parentheses
	public void handleDelimiter(String del) throws GeneralErrorException{
		if(del.equals("(")) operators.push(del);   //left parentheses
		else {      //right parentheses
			while(!operators.peek().equals("(")) {
				String opt = operators.pop();
				FunOp op = new FunOp(opt);
				if(op.getArity() == 2) {
					double num2 = operands.pop();
					double num1 = operands.pop();
					double result = compute(num1,num2,opt);
					operands.push(result);
				}else if(op.getArity() == 1) {
					double num = operands.pop();
					double result = compute(0,num,opt);
					operands.push(result);
				}
			}
			operators.pop();
		}
	}
	
	//Evaluation operation
	public double compute(double num1,double num2,String opt) throws GeneralErrorException{
		double result = 0.0;
		if(opt.equals("+")) result = num1 + num2;
		if(opt.equals("-") || opt.equals("~")) result = num1 - num2;
		if(opt.equals("*")) result = num1 * num2;
		if(opt.equals("/")) result = num1 / num2;
		if(opt.equals("^")) result = Math.pow(num1, num2);
		if(opt.equals("sin")) result = Math.sin(num2);
		if(opt.equals("cos")) result = Math.cos(num2);
		return result;
	}

}
