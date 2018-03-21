package project;

public class FunOp {
	private String name;
	private int arty;
	private int prority;
	public FunOp(String name) {
		this.name = name;
	}
	
	//Define operator operands 
	public int getArity() {
		if(name.equals("+") || name.equals("-") ||  name.equals("*") 
				|| name.equals("/") || name.equals("^")) arty = 2;
		if(name.equals("sin") || name.equals("cos") || name.equals("~")) arty = 1;
		return arty;
	}
	
	//Define operator precedence 
	public int getPrority() {
		if(name.equals("+") || name.equals("-")) prority = 1;
		if(name.equals("*") || name.equals("/")) prority = 2;
		if(name.equals("^")) prority = 4;
		if(name.equals("~") || name.equals("sin") || name.equals("cos")) prority = 3;
		return prority;
	}
	
	public String getName() {
		return this.name;
	}
}
