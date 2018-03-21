package project;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;

public class Evaluator {
	private String s;
	public int precision;
	private ArrayList<String> commandList;  //command list
	private boolean log;
	private String logContent;
	private String logFileName;
	public static double last;
	public static Map<String,Double> map = new HashMap<String,Double>();
	public Evaluator() {
		precision = -1;
		log = false;
		logContent = "";
		commandList = new ArrayList<String>();
		commandList.add("setprecision");
		commandList.add("let");
		commandList.add("reset");
		commandList.add("last");
		commandList.add("save");
		commandList.add("load");
		commandList.add("saved");
		commandList.add("log");
		commandList.add("logged");
	}
	
	public void sets(String s) {
		this.s =  s;
	}
	
	public boolean ifLog() {
		return log;
	}
	public String getLogFileName() {
		return logFileName;
	}
	public void addLogContent(String content) {
		this.logContent += content;
	}
	//Judge mathematical expression
	public boolean isMath() {
		try {
			Tokenizer tokenizer = new Tokenizer(s);
			if(tokenizer.hasNextToken()) {
				Token t = tokenizer.peekNextToken();
				if(t.isOperator() || t.isNumber() || t.isDelimiter() || t.isEqual()) return true;
				else if(t.isIdentifier()) {
					String key = t.getIdentifier();
					if(map.containsKey(key) || key.equals("sin") || key.equals("cos")) return true;
					if(key.equals("last")) {   
						tokenizer.readNextToken();
						if(tokenizer.hasNextToken()) return true;
					}	
				}
			}
		}catch(Exception e) {
			System.out.println("\nERROR: " + e.getMessage());
		}
		return false;
	}
	
	//Judge mathematical expressions
	//The premise is that parentheses match correctly
	public boolean checkMathExpression() {
		try {
			Tokenizer tokenizer = new Tokenizer(s);
			int flag = 1;
			while(tokenizer.hasNextToken()) {
				Token t1 = tokenizer.peekNextToken();
				tokenizer.readNextToken();
				if(tokenizer.hasNextToken()) {					
					Token t2 = tokenizer.peekNextToken();
					if(t1.isNumber()) {				//Numbers don't follow numbers.
						if(t2.isNumber()) return false;
					}else if(t1.isOperator()) { 		//The operator cannot be the operator or the right parentheses
						if(flag == 1 && (!t1.getOperator().equals("~"))) return false;  		//Except minus operator cannot be the first expression 
						if(t2.isOperator() && !t2.getOperator().equals("~")) return false;  	//In addition to the minus operator, not behind other operators 
						else if(t2.isDelimiter() && t2.getDelimiter().equals(")")) return false; 
					}else if(t1.isDelimiter()) {
						//The left parentheses can not be the operator and the right parentheses, the right parentheses can not be left parentheses
						if(t1.getDelimiter().equals("(")) {
							if(t2.isOperator() && !t2.getOperator().equals("~")) return false;
							else if(t2.isDelimiter() && t2.getDelimiter().equals(")")) return false;
						}else if(t1.getDelimiter().equals(")")) {
							if(t2.isDelimiter() && t2.getDelimiter().equals("(")) return false;
						}
					}else if(t1.isEqual()) return false;
				}else {    		//Expressions cannot end with operators or equal signs 
					if(t1.isOperator() || t1.isEqual()) return false;
				}
				flag = 2;;
			}
		}catch(Exception e) {
			System.out.println("\nERROR£º" + e.getMessage());
			return false;
		}
		return true;
	}
	
	//Check parenthesis matching
	public boolean checkDelimiter() {
		try {
			Stack <String> ss = new Stack<String>();  
			Tokenizer tokenizer = new Tokenizer(s);
			while(tokenizer.hasNextToken()) {
				Token t = tokenizer.peekNextToken();
				if(t.isDelimiter()) {
					String del = t.getDelimiter();
					if(del.equals("(")) {
						ss.push(del);
					}else if(del.equals(")")){
						if(ss.isEmpty()) return false;
						else ss.pop();
					}
				}
				tokenizer.readNextToken();
			}
			if(!ss.isEmpty()) return false;
		}catch(Exception e) {
			System.out.println("\nERROR£º" + e.getMessage());
			return false;
		}
		return true;
	}
	
	//Deal with non-mathematical sentences
	public String handleIdentifier() {
		String iden = "";
		try {
			Tokenizer tokenizer = new Tokenizer(s);
			if(tokenizer.hasNextToken()) {
				Token t = tokenizer.peekNextToken();
				if(commandList.indexOf(t.getIdentifier()) != -1) {
					iden = handleCommand(t.getIdentifier());
				}else {
					iden = "Error: " + t.getIdentifier() + " is not a variable";
					throw new GeneralErrorException(t.getIdentifier() + " is not a variable");
				}
			}
		}catch(Exception e) {
			if(iden.equals("")) iden = "Error: " + e.getMessage();
			System.out.println("\nERROR£º" + e.getMessage());
		}
		return iden;
	}
	
	//Process command
	public String handleCommand(String command) {
		String q = "";
		if(command.equals("setprecision")) q = handleSetPrecision();
		else if(command.equals("let")) q = handleLet();
		else if(command.equals("reset")) q = handleReset();
		else if(command.equals("last")) q = handleLast();
		else if(command.equals("save")) q = handleSave();
		else if(command.equals("load")) q = handleLoad();
		else if(command.equals("saved")) q = handleSaved();
		else if(command.equals("log")) q = handleLog();
		else if(command.equals("logged")) q = handleLogged();
		return q;
	}
	
	//Process the setprecision command
	public String handleSetPrecision() {
		String q = "";
		try {
			Tokenizer tokenizer = new Tokenizer(s);
			tokenizer.readNextToken();
			if(tokenizer.hasNextToken() && tokenizer.peekNextToken().isNumber()) {
				int p = (int)tokenizer.peekNextToken().getNumber();
				tokenizer.readNextToken();
				if(tokenizer.hasNextToken()) {
					q = "Syntax error: setprecision allow one parameter";
					throw new SyntaxErrorException("Syntax error: setprecision allow one parameter"); 
				}else {
					precision = p;
					q = "precision set to " + precision;
				}
			}else if(!tokenizer.hasNextToken()){
				q = "current precision is " + precision;
			}else if(tokenizer.hasNextToken() && !tokenizer.peekNextToken().isNumber()) {
				q = "Syntax error: setprecision must be followed by a number";
				throw new SyntaxErrorException("Syntax error: setprecision must be followed by a number"); 
			}
		}catch(Exception e) {
			if(q.equals("")) q = "ERROR£º" + e.getMessage();
			System.out.println("\nERROR£º" + e.getMessage());
		}
		return q;
	}
	
	//Process the let command
	public String handleLet() {
		String q = "";
		try {
			Tokenizer tokenizer = new Tokenizer(s);
			tokenizer.readNextToken();
			if(tokenizer.hasNextToken()) {
				Token t = tokenizer.peekNextToken();
				if(t.isIdentifier()) {
					String name = t.getIdentifier();
					//The variable name behind let cannot be a command name or sin, cos 
					if(commandList.contains(name) || name.equals("sin") || name.equals("cos")) {
						q = "Lexical error: illegal variale name," + name + " is a command";
						throw new LexicalErrorException("Lexical error: illegal variable name");
					}else {
						tokenizer.readNextToken();
						if(!tokenizer.hasNextToken() || !tokenizer.peekNextToken().isEqual()) {
							q = "Syntax error: malformed expression";
							throw new SyntaxErrorException("Syntax error: malformed expression");
						}else {
							//Evaluate the string after "="
							int index = s.indexOf("=");
							String value = s.substring(index + 1);
							if(value.equals("")) {
								q = "Syntax error: malformed expression";
								throw new SyntaxErrorException("Syntax error: malformed expression");
							}else {
								MathematicalEvaluator eval = new MathematicalEvaluator(value,this.precision);
								String result = eval.evaluate();
								Tokenizer tt = new Tokenizer(result);
								double v = tt.peekNextToken().getNumber();
								map.put(name, v);
								q += result;
							}
						}
					}
				}else {
					q = "Error: illegal variable name";
					throw new LexicalErrorException("Lexical error: illegal variable name");
				}
			}else {    //The let command does not follow anything, traverse the map 
				if(map.isEmpty()) q += "no variable defined";
				else {
					for(Entry<String,Double> entry:map.entrySet()) {
						q += entry.getKey() + " = " + entry.getValue() + '\n';
					}
				}
			}
		}catch(Exception e){
			if(q.equals("")) q = "Error£º" + e.getMessage();
			System.out.println("\nERROR£º" + e.getMessage());
		}
		return q;
	}

	
	//Process the reset command
	public String handleReset() {
		String q = "";
		try {
			Tokenizer tokenizer = new Tokenizer(s);
			tokenizer.readNextToken();
			if(tokenizer.hasNextToken()) {
				while(tokenizer.hasNextToken()) {
					Token t = tokenizer.peekNextToken();
					if(t.isIdentifier() && map.containsKey(t.getIdentifier())) {
						map.remove(t.getIdentifier());
						q += t.getIdentifier() + " has been reset\n";
					}else if(t.isIdentifier() && !map.containsKey(t.getIdentifier()) && !t.getIdentifier().equals("last")){
						q += t.getIdentifier() + " is not defined\n";
					}else if(t.isIdentifier() && !map.containsKey(t.getIdentifier()) && t.getIdentifier().equals("last")) {
						q += "Syntax error: last is not a variable";
					}else if(!t.isIdentifier()){
						q += tokenizer.peekNextToken() + " is illegal\n";
					}
					tokenizer.readNextToken();
				}			
			}else { //Only one reset command
				for(Entry<String,Double> entry:map.entrySet()) {
					q += entry.getKey() + " has been reset" + '\n';
				}
				map.clear();
			}
			
		}catch(Exception e){
			if(q.equals("")) q = "Error£º" + e.getMessage();
			System.out.println("\nERROR£º" + e.getMessage());
		}
		return q;
	}

	//Process the last command
	public String handleLast() {
		return last + "";
	}
	
	//Process the save command
	public String handleSave() {
		String q = "";
		try {
			Tokenizer tokenizer = new Tokenizer(s);
			tokenizer.readNextToken();
			if(tokenizer.hasNextToken() && tokenizer.peekNextToken().isString()) {
				String fileName = tokenizer.peekNextToken().getString() + ".txt";
				PrintStream output = new PrintStream(new File("dataFile/" + fileName));
				tokenizer.readNextToken();
				if(!tokenizer.hasNextToken()) {      //Only one file name after the Save command 
					for(Entry<String,Double> entry:map.entrySet()) {
						 output.println("let " + entry.getKey() + " = " + entry.getValue());
					}
					q = "variables saved in " + fileName;
				}else {   //File name with parameters
					do {
						if(!tokenizer.peekNextToken().isIdentifier()) {
							q = "Lexical Error: illegal variable name";
							throw new LexicalErrorException("illegal variable name");
						}else if(!map.containsKey(tokenizer.peekNextToken().getIdentifier())){
							q = "Lexical Error: " + tokenizer.peekNextToken().getIdentifier() + "is not defined";
							throw new LexicalErrorException(tokenizer.peekNextToken().getIdentifier() + "is not defined");
						}else {
							String key = tokenizer.peekNextToken().getIdentifier();
							q += "varible " + key + " saved in " + fileName;
							output.println(key + " = " + map.get(key));
						}
						tokenizer.readNextToken();
					}while(tokenizer.hasNextToken());
				}
			}else if(tokenizer.hasNextToken() && !tokenizer.peekNextToken().isString()) {
				q = "Lexical Error: illegal file name";
				throw new LexicalErrorException("illegal file name");
			}else {  //Only one save command
				q = "Syntex Error: sava must be followed by a file name";
				throw new SyntaxErrorException("save must be followed by a file name");
			}
		}catch(Exception e) {
			if(q.equals("")) q = "Error£º" + e.getMessage();
			System.out.println("\nERROR£º" + e.getMessage()); 
		}
		return q;
	}
	
	//Process the load command
	public String handleLoad() {
		String q = "";
		try {
			Tokenizer tokenizer = new Tokenizer(s);
			tokenizer.readNextToken();
			if(tokenizer.hasNextToken() && tokenizer.peekNextToken().isString()) {
				String fileName = tokenizer.peekNextToken().getString();
				tokenizer.readNextToken();
				if(tokenizer.hasNextToken()) {  
					q = "Syntex Error: load allows one file name";
					throw new SyntaxErrorException("load allows one file name");
				}else {  //Only one file name after the load command
					File f = new File("dataFile/" + fileName + ".txt");
					if(f.exists()) {
						Scanner input = new Scanner(f);
						while(input.hasNextLine()) {
							String line = input.nextLine();
							this.s = line;
							this.handleLet();
						}
						q = fileName + " loaded";
					}else {
						q = "Lexical Error: " + fileName + " not exists";
						throw new LexicalErrorException(fileName + " not exists");
					}
				}
			}else if(tokenizer.hasNextToken() && !tokenizer.peekNextToken().isString()){
				q = "Lexical Error: illegal file name";
				throw new LexicalErrorException("illegal file name");
			}else {  //Only one load command
				q = "Syntex Error: load must be followed by a file name";
				throw new SyntaxErrorException("load must be followed by a file name");
			}
		}catch(Exception e){
			if(q.equals("")) q = "Error£º" + e.getMessage();
			System.out.println("\nERROR£º" + e.getMessage());
		}
		return q;
	}

	//Process the saved command
	public String handleSaved() {
		String q = "";
		File f = new File("dataFile");
		File fa[] = f.listFiles();
		if(fa.length == 0) q = "no file be saved";
		else {
			for(int i = 0; i < fa.length; i++) {
				File fs = fa[i];
				q += fs.getName() + '\n';
			}
		}
		return q;
	}

	//Process the log command
	public String handleLog() {
		String q = "";
		try {
			Tokenizer tokenizer = new Tokenizer(s);
			tokenizer.readNextToken();
			if(tokenizer.hasNextToken()) {
				if(tokenizer.peekNextToken().isString()) {
					String logName = tokenizer.peekNextToken().getString();
					tokenizer.readNextToken();
					if(tokenizer.hasNextToken()) {
						q = "Syntax Error: Syntax Error: log must be followed by a file name or end";
					}else {
						this.log = true;
						this.logFileName = logName;
						q = "logging session to " + logName;
					}
				}else if(tokenizer.peekNextToken().isIdentifier() && tokenizer.peekNextToken().getIdentifier().equals("end")) {
					tokenizer.readNextToken();
					if(tokenizer.hasNextToken()) {
						q = "Syntax Error: Syntax Error: log must be followed by a file name or end";
						throw new SyntaxErrorException("log must be followed by a file name or end");
					}else {
						this.log = false;
						q = "session was logged to " + this.logFileName;
						PrintStream out = new PrintStream("logFile/" + logFileName + ".txt");
						out.println(logContent);
					}
				}else {
					q = "Syntax Error: log must be followed by a file name or end";
					throw new SyntaxErrorException("log must be followed by a file name or end");
				}
			}else { //Only one load command with no parameters
				if(this.log == true) q = this.getLogFileName();
				else q = "Syntax Error: log must be followed by a file name or end";
			}
		}catch(Exception e) {
			System.out.println("\nERROR£º" + e.getMessage());
		}
		return q;
	}

	//Process the logged command 
	public String handleLogged() {
		String q = "";
		File f = new File("logFile");
		File fa[] = f.listFiles();
		if(fa.length == 0) q = "no file be saved";
		else {
			for(int i = 0; i < fa.length; i++) {
				File fs = fa[i];
				q += fs.getName() + '\n';
			}
		}
		return q;
	}
}
