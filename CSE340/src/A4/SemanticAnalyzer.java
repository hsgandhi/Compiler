package A4;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

public class SemanticAnalyzer {
  
  public static final Hashtable<String, Vector<SymbolTableItem>> symbolTable = new Hashtable<String, Vector<SymbolTableItem>>();
  public static final Stack<String> stack = new Stack<String>();
  
  private static final int INTEGER  =  0;
  private static final int FLOAT  =  1;
  private static final int CHARACTER  =  2;
  private static final int STRING  =  3;
  private static final int BOOLEAN  =  4;
  private static final int VOID  =  5;
  private static final int ERROR  =  6;
  private static final int OK  =  7;
  
  
  private static final int[][][] binaryCube={
	  {//(-,*,/)
		  {INTEGER,	FLOAT,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {FLOAT,	FLOAT,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR}
	  },
	  {//(+)
		  {INTEGER,	FLOAT,	ERROR,	STRING,	ERROR,	ERROR,	ERROR},
		  {FLOAT,	FLOAT,	ERROR,	STRING,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	STRING,	ERROR,	ERROR,	ERROR},
		  {STRING,	STRING,	STRING,	STRING,	STRING,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	STRING,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	STRING,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	STRING,	ERROR,	ERROR,	ERROR}
	  },
	  {//(>,<)
		  {BOOLEAN,	BOOLEAN,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {BOOLEAN,	BOOLEAN,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR}
	  },
	  {//(!=,==)
		  {BOOLEAN,	BOOLEAN,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {BOOLEAN,	BOOLEAN,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	BOOLEAN,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	BOOLEAN,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	BOOLEAN,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR}
	  },
	  {//(&,!)
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	BOOLEAN,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR}
	  },
	  {//(=)
		  {OK,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {OK,	OK,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	OK,	ERROR,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	OK,	ERROR,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	OK,	ERROR,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	OK,	ERROR},
		  {ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR,	ERROR}
	  }
  };
  
  
		  
  
  private static Gui gui;
  public static void setGui(Gui guiV)
  {
	  gui=guiV;
  }
  
  // create here a data structure for the cube of types
  
  public static Hashtable<String, Vector<SymbolTableItem>> getSymbolTable() {
    return symbolTable;
  }
  
  public static void checkVariable(String type, String id, int lineNo) {
   
   boolean idExists=false;
    // A. search the id in the symbol table
   if(symbolTable.containsKey(id))
	   idExists=true;
   
    // B. if !exist then insert: type, scope=global, value={0, false, "", '')
   if(!idExists)
   {
    Vector<SymbolTableItem> v = new Vector<SymbolTableItem>();
    v.add(new SymbolTableItem(type,"global", ""));
    symbolTable.put(id, v);
   }
    // C. else error: â€œvariable id is already definedâ€?
   else
   {
	   error(gui,1,lineNo,id);
   }
  }

  public static void pushStack(String type) {
    // push type in the stack
	  stack.push(type);
  }
  
  public static String popStack() {
    String result="";
    // pop a value from the stack
    result=stack.pop();
    return result;
  }
  
  
  public static String calculateCube(String type, String operator) {
    String result="";
    // unary operator ( - and !)
    if(operator.equals("-"))
    {
    if(type.equals("int") || type.equals("INTEGER"))
    	result="INTEGER";
    else if(type.equals("float") || type.equals("FLOAT"))
    	result="FLOAT";
    else 
    	result = "ERROR";
    }
    
    if(operator.equals("!"))
    {
    if(type.equals("boolean") || type.equals("BOOLEAN"))
    	result="BOOLEAN";
    else 
    	result = "ERROR";
    }
    
    return result;
  }

  public static String calculateCube(String type1, String type2, String operator) {
    // binary operator ( - and !)
    int firstParam=getFirstParam(operator);
    int secondParam=getSecondParam(type1);
    int thirdParam=getThirdParam(type2);
    int intResult=0;
    try
    {
    	intResult=binaryCube[firstParam][secondParam][thirdParam];
    }
    catch(Exception e)
    {
    	System.out.println(operator + "-" + type1 + "-" +type2 );
    	intResult = 0;
    }
    
    String result=getResultOfTypeMatching(intResult);
    
    return result;
  }
  
  private static String getResultOfTypeMatching(int intResult) {
	  Integer result = new Integer(intResult);
	  Map<Integer,String> typeMap=new HashMap<Integer,String>();
	  typeMap.put(0, "INTEGER");
	  typeMap.put(1, "FLOAT");
	  typeMap.put(2, "CHARACTER");
	  typeMap.put(3, "STRING");
	  typeMap.put(4, "BOOLEAN");
	  typeMap.put(5, "VOID");
	  typeMap.put(6, "ERROR");
	  typeMap.put(7, "OK");
	  
	return typeMap.get(result);
}

private static int getThirdParam(String type2) {
	return getSecondParam(type2);
}

private static int getSecondParam(String type1) {
	if(type1.equals("int") || type1.equals("INTEGER"))
		return 0;
	else if(type1.equals("float") || type1.equals("FLOAT"))
		return 1;
	else if(type1.equals("char") || type1.equals("CHARACTER"))
		return 2;
	else if(type1.equals("string") || type1.equals("STRING"))
		return 3;
	else if(type1.equals("boolean") || type1.equals("BOOLEAN"))
		return 4;
	else if(type1.equals("VOID")  || type1.equals("void"))
	//else if(type1.equals("void"))TODO
		return 5;
	else if(type1.equals("ERROR"))
		return 6;
	else
		return -1;
}

private static int getFirstParam(String operator) {
	if(operator.equals("-") || operator.equals("*") || operator.equals("/"))
		return 0;
	else if(operator.equals("+"))
		return 1;
	else if(operator.equals(">") || operator.equals("<"))
		return 2;
	else if(operator.equals("!=") || operator.equals("=="))
		return 3;
	else if(operator.equals("&") || operator.equals("|"))
		return 4;
	else if(operator.equals("="))
		return 5;
	else
		return -1;
}

public static void error(Gui gui, int err, int n,String id) {
    switch (err) {
      case 1: 
        gui.writeConsole("Line" + n + ": variable <" + id + ">  is already defined"); 
        break;
      case 2: 
        gui.writeConsole("Line" + n + ": incompatible types: type mismatch"); 
        break;
      case 3: 
        gui.writeConsole("Line" + n + ": incompatible types: expected boolean"); 
        break;
      case 4: 
          gui.writeConsole("Line" + n + ": variable <" + id +  "> not found"); 
          break;

    }
  }
  
}
