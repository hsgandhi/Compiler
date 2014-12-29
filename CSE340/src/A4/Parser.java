package A4;

import java.util.Arrays;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author javiergs
 */
public class Parser {

  private static DefaultMutableTreeNode root;
  private static Vector<Token> tokens;
  private static int currentToken;
  private static Gui gui;
  private static boolean isLineNoToBeDecremented=true;
  private static int labelCounter=0;
  private static int switchCounter=0;
  

  public static DefaultMutableTreeNode run(Vector<Token> t, Gui gui) {
	Parser.gui=gui;
	SemanticAnalyzer.setGui(gui);
	SemanticAnalyzer.symbolTable.clear();
	SemanticAnalyzer.stack.clear();
    tokens = t;
    currentToken = 0;
    labelCounter  = 0;
    switchCounter = 0;
    
    root = new DefaultMutableTreeNode("program");
    
    CodeGenerator.clear(gui);
    CodeGenerator.instructionCounter = 0;
    
    rule_program(root);
    
    CodeGenerator.addInstruction("OPR", "0", "0");
    //show
    CodeGenerator.writeCode(gui);
    gui.writeSymbolTable(SemanticAnalyzer.getSymbolTable());
    return root;
  }
  
  
  private static boolean rule_program(DefaultMutableTreeNode parent) {
	  boolean error;
	  DefaultMutableTreeNode node;
	  
	  if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("{")) {
	        node = new DefaultMutableTreeNode("{");
	        parent.add(node);
	        currentToken++;
	    }
	    else
	    {
	    	//isLineNoToBeDecremented=false;
	    	error(1);
	    	errorRecovery(1);
	    	error=true;
	    }
	  
	  node = new DefaultMutableTreeNode("body");
      parent.add(node);
      error=rule_body(node);
	  
	  if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("}")) {
	        node = new DefaultMutableTreeNode("}");
	        parent.add(node);
	        currentToken++;
	    }
	    else
	    {
	    	error(2);
	    }
	  
	  return error;
  }
  
  private static boolean rule_body(DefaultMutableTreeNode parent) {
	  boolean error=false;
	  DefaultMutableTreeNode node;
	  
	  while(currentToken < tokens.size() && !tokens.get(currentToken).getWord().equals("}"))
	  {
		  if(currentToken < tokens.size())
		  {
			  if(tokens.get(currentToken).getWord().equals("print"))
			  {
				  node = new DefaultMutableTreeNode("print");
				  parent.add(node);
				  error=rule_print(node);
				  
				  error=checkDelimiterInBody(node, parent);
			  }
			  else if(tokens.get(currentToken).getToken().equals("IDENTIFIER"))
			  {
				  node = new DefaultMutableTreeNode("assignment");
				  parent.add(node);
				  error=rule_assignment(node);
				  
				  error=checkDelimiterInBody(node, parent);
			  }
			  else if(tokens.get(currentToken).getWord().equals("int")
					  || tokens.get(currentToken).getWord().equals("float")
					  || tokens.get(currentToken).getWord().equals("boolean")
					  || tokens.get(currentToken).getWord().equals("char")
					  || tokens.get(currentToken).getWord().equals("string")
					  || tokens.get(currentToken).getWord().equals("void"))
			  {
				  node = new DefaultMutableTreeNode("variable");
				  parent.add(node);
				  error=rule_bariable(node);
				  
				  error=checkDelimiterInBody(node, parent);
			  }
			  else if (tokens.get(currentToken).getWord().equals("return"))
			  {
				  node = new DefaultMutableTreeNode("return");
				  parent.add(node);
				  error=rule_return(node);
				  
				  error=checkDelimiterInBody(node, parent);
			  }
			  else if (tokens.get(currentToken).getWord().equals("while"))
			  {
				  node = new DefaultMutableTreeNode("while");
				  parent.add(node);
				  error=rule_while(node);
			  }
			  else if (tokens.get(currentToken).getWord().equals("if"))
			  {
				  node = new DefaultMutableTreeNode("if");
				  parent.add(node);
				  error=rule_if(node);
			  }
			  else if (tokens.get(currentToken).getWord().equals("switch"))
			  {
				  node = new DefaultMutableTreeNode("switch");
				  parent.add(node);
				  error=rule_switch(node);
			  }
			  else
			  {
				  isLineNoToBeDecremented=false;
				  error(4);
				  errorRecovery(1);
				  error=true;
			  }
		  }
	  }
	  
	  return error;
  }
  
  private static boolean checkDelimiterInBody(DefaultMutableTreeNode node,
		  DefaultMutableTreeNode parent) {
	  if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(";"))
	  {
		  node = new DefaultMutableTreeNode(";");
	      parent.add(node);
	      currentToken++; 
	      return false;
	  }
	  else
	  {
		  error(3);
		  return true;
	  }
}


private static boolean rule_assignment(DefaultMutableTreeNode parent) {
	  boolean error;
	  DefaultMutableTreeNode node;
	  String[] followExprWords={")",";"};
	  
	  String assignmentTo = tokens.get(currentToken).getWord();
	    
	  node = new DefaultMutableTreeNode("identifier" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      	pushIdentifierTypeToStack();
      	
      currentToken++;
      
      if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("=")) {
	        node = new DefaultMutableTreeNode("=");
	        parent.add(node);
	        currentToken++;
	    }
	    else
	    {
	    	error(5);
	    	errorRecovery(2);
	    	error=true;
	    	if(currentToken < tokens.size() && Arrays.asList(followExprWords).contains(tokens.get(currentToken).getWord()))
    			return true;
	    	if(endOfFile())
	    		return true;
	    }
	  
      node = new DefaultMutableTreeNode("expression");
      parent.add(node);
      error=rule_expression(node);
      
      CodeGenerator.addInstruction("STO", assignmentTo, "0");
      
      //checking result of assignment
      String x = SemanticAnalyzer.popStack();
	  String y = SemanticAnalyzer.popStack();
	  String result = SemanticAnalyzer.calculateCube(y, x, "=" );
	  
	  if(result != "OK")
		  SemanticAnalyzer.error(gui, 2, tokens.get(currentToken).getLine(), tokens.get(currentToken).getWord());
      
	  return error;
  }
  
  private static boolean endOfFile() {
	  if(currentToken >= tokens.size())
			  return true;
	return false;
}


private static boolean rule_bariable(DefaultMutableTreeNode parent) {
	  boolean error;
	    DefaultMutableTreeNode node;
	    
	    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("int")) {
	        node = new DefaultMutableTreeNode("int");
	        parent.add(node);
	        currentToken++;
	    }
	    else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("float")) {
	        node = new DefaultMutableTreeNode("float");
	        parent.add(node);
	        currentToken++;
	    }
	    else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("boolean")) {
	        node = new DefaultMutableTreeNode("boolean");
	        parent.add(node);
	        currentToken++;
	    }else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("char")) {
	        node = new DefaultMutableTreeNode("char");
	        parent.add(node);
	        currentToken++;
	    }else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("string")) {
	        node = new DefaultMutableTreeNode("string");
	        parent.add(node);
	        currentToken++;
	    }else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("void")) {
	        node = new DefaultMutableTreeNode("void");
	        parent.add(node);
	        currentToken++;
	    }
	    
	    if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("IDENTIFIER")) {
	        node = new DefaultMutableTreeNode("identifier" + "(" + tokens.get(currentToken).getWord() + ")");
	        parent.add(node);
	        
	        
	        SemanticAnalyzer.checkVariable(tokens.get(currentToken-1).getWord(), tokens.get(currentToken).getWord(),tokens.get(currentToken).getLine());
	        
	        currentToken++;
	        
	        CodeGenerator.addVariable(tokens.get(currentToken-2).getWord(), tokens.get(currentToken-1).getWord());
	    }
	    else
	    {
	    	error(6);
	    	return true;
	    }
	    
	    error = false;
	    return error;
  }
  
  private static boolean rule_while(DefaultMutableTreeNode parent) {
	  boolean error;
	  boolean skipExpression=false;
	    DefaultMutableTreeNode node;
	    
	    node = new DefaultMutableTreeNode("while");
	    parent.add(node);
	    currentToken++;
	    
	    int jumpToLine = CodeGenerator.instructionCounter +1;
	    
	    //
	    if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(")) {
		    node = new DefaultMutableTreeNode("(");
		    parent.add(node);
		    currentToken++;
	    }
	    else{
	    	error(7);
	    	errorRecovery(3);
	    	error=true;
	    	if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")")) {
	    		skipExpression=true;
			    }
	    	if(endOfFile())
	    		skipExpression=true;
	    }
	    //
	    if(!skipExpression){
		    node = new DefaultMutableTreeNode("expression");
		    parent.add(node);
		    error = rule_expression(node);
		    
		    checkBooleanInStack();
	    }
	    //
	    if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")")) {
		    node = new DefaultMutableTreeNode(")");
		    parent.add(node);
		    currentToken++;
	    }
	    else{
	    	error(8);
	    	errorRecovery(4);
	    	error=true;
	    	if(endOfFile())
	    		return true;
	    } 
	    
	    labelCounter++;
	    CodeGenerator.addInstruction("JMC", "#e"+labelCounter, "false");
	    int jumpConditionLabelCounter = labelCounter;
	    
	    //
	    node = new DefaultMutableTreeNode("program");
	    parent.add(node);
	    error = rule_program(node);
	    
	    labelCounter++;
	    CodeGenerator.addInstruction("JMP", "#e"+labelCounter, "0");
	    int jumpLabelCounter = labelCounter;
	    CodeGenerator.addLabel("e"+jumpLabelCounter, jumpToLine);
	    
	    int jumpConditionToLine = CodeGenerator.instructionCounter +1;
	    CodeGenerator.addLabel("e"+jumpConditionLabelCounter, jumpConditionToLine);
	    
	    return error;
  }
  
  
  private static boolean rule_if(DefaultMutableTreeNode parent) {
	  boolean error=false;
	  boolean skipExpression=false;
	  boolean skipProgram=false;
	    DefaultMutableTreeNode node;
	    
	    node = new DefaultMutableTreeNode("if");
	    parent.add(node);
	    currentToken++;
	    //
	    if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(")) {
		    node = new DefaultMutableTreeNode("(");
		    parent.add(node);
		    currentToken++;
	    }
	    else{
	    	error(7);
	    	errorRecovery(3);
	    	error=true;
	    	if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")")) {
	    		skipExpression=true;
			    }
	    	if(endOfFile())
	    		skipExpression=true;
	    }
	    //
	    if(!skipExpression)
	    {
		    node = new DefaultMutableTreeNode("expression");
		    parent.add(node);
		    error = rule_expression(node);
		    
		    checkBooleanInStack();
	    }
	    //
	    if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")")) {
		    node = new DefaultMutableTreeNode(")");
		    parent.add(node);
		    currentToken++;
	    }
	    else{
	    	error(8);
	    	errorRecovery(5);
	    	error=true;
	    	 if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("else")){
	    		 skipProgram=true;
	    	    } 
	    	 if(endOfFile())
	    		 skipProgram=true;
	    }  
	    
	    labelCounter++;
	    CodeGenerator.addInstruction("JMC", "#e"+labelCounter, "false");
	    int jumpConditionLabelCounter = labelCounter;
	    
	    //
	    if(!skipProgram){
		    node = new DefaultMutableTreeNode("program");
		    parent.add(node);
		    error = rule_program(node);
	    }
	    //
	    labelCounter++;
	    CodeGenerator.addInstruction("JMP", "#e"+labelCounter, "0");
	    int jumpLabelCounter = labelCounter;
	    
	    int jumpConditionToLine = CodeGenerator.instructionCounter +1;
	    CodeGenerator.addLabel("e"+jumpConditionLabelCounter, jumpConditionToLine);
	    
	    if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("else")){
		    node = new DefaultMutableTreeNode("else");
		    parent.add(node);
		    currentToken++; 
		    //
		    node = new DefaultMutableTreeNode("program");
		    parent.add(node);
		    error = rule_program(node);
		    
		    int jumpToLine = CodeGenerator.instructionCounter +1;
		    CodeGenerator.addLabel("e"+jumpLabelCounter, jumpToLine);
	    }
	    else
	    {
	    	CodeGenerator.addLabel("e"+jumpLabelCounter, jumpConditionToLine);
	    }
	    
	    return error;
  }
  

  private static boolean rule_switch(DefaultMutableTreeNode parent) {
	  boolean error;
	  String identifierValue=null;
	  DefaultMutableTreeNode node;
	  switchCounter++;
	  int localSwitchCounter = switchCounter;
	  
	  node = new DefaultMutableTreeNode("switch");
	  parent.add(node);
	  currentToken++;
	    
	  if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(")) {
		    node = new DefaultMutableTreeNode("(");
		    parent.add(node);
		    currentToken++;
	    }
	  
	  if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("IDENTIFIER")) {
		  node = new DefaultMutableTreeNode("identifier" + "(" + tokens.get(currentToken).getWord() + ")");
      	  parent.add(node);
      	  currentToken++;
      	  
      	identifierValue = tokens.get(currentToken-1).getWord() ;
	    }
	  
	  if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")")) {
		    node = new DefaultMutableTreeNode(")");
		    parent.add(node);
		    currentToken++;
	    }
	  
	  
	  
	  if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("{")) {
	        node = new DefaultMutableTreeNode("{");
	        parent.add(node);
	        currentToken++;
	    }
	  
	  node = new DefaultMutableTreeNode("cases");
	  parent.add(node);
	  error=rule_cases(node,identifierValue,localSwitchCounter);
	  
	  
	  
	  if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("default")) {
		  node = new DefaultMutableTreeNode("default");
		  parent.add(node);
		  error=rule_default(node);
	  }
	  
	  if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("}")) {
	        node = new DefaultMutableTreeNode("}");
	        parent.add(node);
	        currentToken++;
	    }
	  
	  int jumpToLine = CodeGenerator.instructionCounter +1;
	  CodeGenerator.addLabel("swe"+localSwitchCounter, jumpToLine);
	  
	return false;
}

private static boolean rule_cases(DefaultMutableTreeNode parent, String identifierValue, int localSwitchCounter) {
	boolean error;
	DefaultMutableTreeNode node;
	String type;
	
	while(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("case")){
		
	  node = new DefaultMutableTreeNode("case");
	  parent.add(node);
	  currentToken++;
	  
	  if(currentToken < tokens.size() && 
			  (tokens.get(currentToken).getToken().equals("INTEGER")
				|| tokens.get(currentToken).getToken().equals("OCTAL")
				|| tokens.get(currentToken).getToken().equals("HEXADECIMAL")
				|| tokens.get(currentToken).getToken().equals("BINARY"))) {
		  
		  if(tokens.get(currentToken).getToken().equals("INTEGER"))
			  type="integer";
		  else if(tokens.get(currentToken).getToken().equals("OCTAL"))
			  type="octal";
		  else if(tokens.get(currentToken).getToken().equals("HEXADECIMAL"))
			  type="hexadecimal";
		  else if(tokens.get(currentToken).getToken().equals("BINARY"))
			  type="binary";
		  else 
			  type ="identifier";
			
		  node = new DefaultMutableTreeNode(type + "(" + tokens.get(currentToken).getWord() + ")");
      	  parent.add(node);
      	  currentToken++;
      	  
      	CodeGenerator.addInstruction("LOD", identifierValue, "0");  
      	CodeGenerator.addInstruction("LIT", tokens.get(currentToken-1).getWord() , "0");
	    }
	  
	  if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(":")) {
		    node = new DefaultMutableTreeNode(":");
		    parent.add(node);
		    currentToken++;
	    }
	  
	  CodeGenerator.addInstruction("OPR", "15" , "0");
	  
	  labelCounter++;
	  CodeGenerator.addInstruction("JMC", "#se"+labelCounter, "false");
	  int jumpConditionLabelCounter = labelCounter;
	    
	  node = new DefaultMutableTreeNode("program");
	  parent.add(node);
	  error = rule_program(node);
	  
	  CodeGenerator.addInstruction("JMP", "#swe"+localSwitchCounter, "0");
	  
	  int jumpConditionToLine = CodeGenerator.instructionCounter +1;
	  CodeGenerator.addLabel("se"+jumpConditionLabelCounter, jumpConditionToLine);
	}
	
	return false;
}


private static boolean rule_default(DefaultMutableTreeNode parent) {
	boolean error;
	DefaultMutableTreeNode node;
	
	node = new DefaultMutableTreeNode("default");
	parent.add(node);
	currentToken++;
	
	if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(":")) {
	    node = new DefaultMutableTreeNode(":");
	    parent.add(node);
	    currentToken++;
    }
	
	node = new DefaultMutableTreeNode("program");
	parent.add(node);
	error = rule_program(node);
	
	return false;
}

private static boolean rule_return(DefaultMutableTreeNode parent) {
	  boolean error;
	    DefaultMutableTreeNode node;
	    
	    node = new DefaultMutableTreeNode("return");
	    parent.add(node);
	    currentToken++;
	    
	    CodeGenerator.addInstruction("OPR", "1", "0");
	    
	    error=false;
	    return error;
  }
  
  private static boolean rule_print(DefaultMutableTreeNode parent) {
	  boolean error=false;
	  boolean skipExpression=false;
	    DefaultMutableTreeNode node;
	    node = new DefaultMutableTreeNode("print");
	    parent.add(node);
	    currentToken++;
	    //
	    if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(")) {
		    node = new DefaultMutableTreeNode("(");
		    parent.add(node);
		    currentToken++;
	    }
	    else{
	    	error(7);
	    	errorRecovery(3);
	    	error=true;
	    	if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")")) {
	    		skipExpression=true;
			    }
	    	 if(endOfFile())
	    		 skipExpression=true;
	    }
	    //
	    if(!skipExpression){
		    node = new DefaultMutableTreeNode("expression");
		    parent.add(node);
		    error = rule_expression(node);
	    }
	    //
	    if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")")) {
		    node = new DefaultMutableTreeNode(")");
		    parent.add(node);
		    currentToken++;   
	    }
	    else{
	    	error(8);
	    	return true;
	    }
	    
	    CodeGenerator.addInstruction("OPR", "21", "0");
	    
	    return error;
  }
  
  private static boolean rule_expression(DefaultMutableTreeNode parent) {
	    boolean error;
	    DefaultMutableTreeNode node;
	    node = new DefaultMutableTreeNode("X");
	    parent.add(node);
	    error = rule_X(node);
	    while (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("|")) {
	      if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("|")) {
	        node = new DefaultMutableTreeNode("|");
	        parent.add(node);
	        currentToken++;
	        node = new DefaultMutableTreeNode("X");
	        parent.add(node);
	        error = rule_X(node);
	        
	        checkCubeForBinaryOpertor("|");
	        CodeGenerator.addInstruction("OPR", "8", "0");
	      	}
	      }
	    return error;
	  }
  
  private static boolean rule_X(DefaultMutableTreeNode parent) {
	    boolean error;
	    DefaultMutableTreeNode node;
	    node = new DefaultMutableTreeNode("Y");
	    parent.add(node);
	    error = rule_Y(node);
	    while (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("&")) {
	      if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("&")) {
	        node = new DefaultMutableTreeNode("&");
	        parent.add(node);
	        currentToken++;
	        node = new DefaultMutableTreeNode("Y");
	        parent.add(node);
	        error = rule_Y(node);
	        
	        checkCubeForBinaryOpertor("&");
	        CodeGenerator.addInstruction("OPR", "9", "0");
	      	}
	      }
	    return error;
	  }
  
  private static boolean rule_Y(DefaultMutableTreeNode parent){
	  boolean error;
	    DefaultMutableTreeNode node;
	    boolean operatorUsed=false;
	    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("!")) {
	        node = new DefaultMutableTreeNode("!");
	        parent.add(node);
	        operatorUsed=true;
	        currentToken++;
	      }
	      node = new DefaultMutableTreeNode("R");
	      parent.add(node);
	      error = rule_R(node);
	
	      if(operatorUsed)
	      {
	    	  checkCubeForUnaryOpertor("!");
	    	  CodeGenerator.addInstruction("OPR", "10", "0");
	      }
	    	  
	    return error;
  }

  private static boolean rule_R(DefaultMutableTreeNode parent){
	  boolean error;
	    DefaultMutableTreeNode node;
	    node = new DefaultMutableTreeNode("E");
	    parent.add(node);
	    error = rule_E(node);
	    while(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("<") || currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(">") 
	    		||	currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("==") || currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("!="))
	    {
	    	if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("<"))
	    	{
	    		node = new DefaultMutableTreeNode("<");
    	        parent.add(node);
    	        currentToken++;
    	        node = new DefaultMutableTreeNode("E");
    	        parent.add(node);
    	        error = rule_E(node);
    	        
    	        checkCubeForBinaryOpertor("<");
    	        CodeGenerator.addInstruction("OPR", "12", "0");
	    	}
	    	else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(">"))
	    	{
	    		node = new DefaultMutableTreeNode(">");
    	        parent.add(node);
    	        currentToken++;
    	        node = new DefaultMutableTreeNode("E");
    	        parent.add(node);
    	        error = rule_E(node);
    	        
    	        checkCubeForBinaryOpertor(">");
    	        CodeGenerator.addInstruction("OPR", "11", "0");
	    	}
	    	else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("=="))
	    	{
	    		node = new DefaultMutableTreeNode("==");
    	        parent.add(node);
    	        currentToken++;
    	        node = new DefaultMutableTreeNode("E");
    	        parent.add(node);
    	        error = rule_E(node);
    	        
    	        checkCubeForBinaryOpertor("==");
    	        CodeGenerator.addInstruction("OPR", "15", "0");
	    	}
	    	else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("!="))
	    	{
	    		node = new DefaultMutableTreeNode("!=");
    	        parent.add(node);
    	        currentToken++;
    	        node = new DefaultMutableTreeNode("E");
    	        parent.add(node);
    	        error = rule_E(node);
    	        
    	        checkCubeForBinaryOpertor("!=");
    	        CodeGenerator.addInstruction("OPR", "16", "0");
	    	}
	    }
	    return error;
  }
  
  private static boolean rule_E(DefaultMutableTreeNode parent) {
    boolean error;
    DefaultMutableTreeNode node;
    node = new DefaultMutableTreeNode("A");
    parent.add(node);
    error = rule_A(node);
    while (currentToken < tokens.size() && (tokens.get(currentToken).getWord().equals("+") ||  tokens.get(currentToken).getWord().equals("-"))) {
      if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("+")) {
        node = new DefaultMutableTreeNode("+");
        parent.add(node);
        currentToken++;
        node = new DefaultMutableTreeNode("A");
        parent.add(node);
        error = rule_A(node);
        
        checkCubeForBinaryOpertor("+");
        CodeGenerator.addInstruction("OPR", "2", "0");
      } 
      else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("-")) {
        node = new DefaultMutableTreeNode("-");
        parent.add(node);
        currentToken++;
        node = new DefaultMutableTreeNode("A");
        parent.add(node);
        error = rule_A(node);
        
        checkCubeForBinaryOpertor("-");
        CodeGenerator.addInstruction("OPR", "3", "0");
      }
    }
    return error;
  }

  private static boolean rule_A(DefaultMutableTreeNode parent) {
    boolean error;
    boolean twiceHere=false;
    DefaultMutableTreeNode node = new DefaultMutableTreeNode("B");
    parent.add(node);
    error = rule_B(node);
    while (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("*") || currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("/")) {
      if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("*")) {
        node = new DefaultMutableTreeNode("*");
        parent.add(node);
        currentToken++;
        node = new DefaultMutableTreeNode("B");
        parent.add(node);
        error = rule_B(node);
        
        checkCubeForBinaryOpertor("*");
        CodeGenerator.addInstruction("OPR", "4", "0");

      } else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("/")) {
        node = new DefaultMutableTreeNode("/");
        parent.add(node);
        currentToken++;
        node = new DefaultMutableTreeNode("B");
        parent.add(node);
        error = rule_B(node);
        
        checkCubeForBinaryOpertor("/");
        CodeGenerator.addInstruction("OPR", "5", "0");
      }
    }
    return error;
  }

  private static boolean rule_B(DefaultMutableTreeNode parent) {
    boolean error;
    DefaultMutableTreeNode node;
    boolean operatorUsed=false;
    if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("-")) {
      node = new DefaultMutableTreeNode("-");
      parent.add(node);
      operatorUsed=true;
      CodeGenerator.addInstruction("LIT", "0", "0");
      currentToken++;
    }
    node = new DefaultMutableTreeNode("C");
    parent.add(node);
    error = rule_C(node);
    
    if(operatorUsed)
    {
    	checkCubeForUnaryOpertor("-");
    	CodeGenerator.addInstruction("OPR", "3", "0");
    }
    
    return error;
  }


private static boolean rule_C(DefaultMutableTreeNode parent) {
    boolean error = false;
    DefaultMutableTreeNode node;
    boolean isExpression = false;
    boolean isIdentifier = false;
    if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("INTEGER")) {
      node = new DefaultMutableTreeNode("integer" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      SemanticAnalyzer.pushStack(tokens.get(currentToken).getToken());
      currentToken++;
    } else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("IDENTIFIER")) {
    	isIdentifier = true;
      node = new DefaultMutableTreeNode("identifier" + "(" + tokens.get(currentToken).getWord() + ")");
      parent.add(node);
      	pushIdentifierTypeToStack();
	      
      currentToken++;
    } 
    else if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("OCTAL")) {
        node = new DefaultMutableTreeNode("octal" + "(" + tokens.get(currentToken).getWord() + ")");
        parent.add(node);
        SemanticAnalyzer.pushStack("INTEGER");
        currentToken++;
    }
    else if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("HEXADECIMAL")) {
        node = new DefaultMutableTreeNode("hexadecimal" + "(" + tokens.get(currentToken).getWord() + ")");
        parent.add(node);
        SemanticAnalyzer.pushStack("INTEGER");
        currentToken++;
    }
    else if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("BINARY")) {
        node = new DefaultMutableTreeNode("binary" + "(" + tokens.get(currentToken).getWord() + ")");
        parent.add(node);
        SemanticAnalyzer.pushStack("INTEGER");
        currentToken++;
    }
    else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("true")) {
        node = new DefaultMutableTreeNode("true");
        parent.add(node);
        SemanticAnalyzer.pushStack("BOOLEAN");
        currentToken++;
    }
    else if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("false")) {
        node = new DefaultMutableTreeNode("false");
        parent.add(node);
        SemanticAnalyzer.pushStack("BOOLEAN");
        currentToken++;
    }
    else if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("STRING")) {
        node = new DefaultMutableTreeNode("string" + "(" + tokens.get(currentToken).getWord() + ")");
        parent.add(node);
        SemanticAnalyzer.pushStack(tokens.get(currentToken).getToken());
        currentToken++;
    }
    else if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("CHARACTER")) {
        node = new DefaultMutableTreeNode("char" + "(" + tokens.get(currentToken).getWord() + ")");
        parent.add(node);
        SemanticAnalyzer.pushStack(tokens.get(currentToken).getToken());
        currentToken++;
    }
    else if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("FLOAT")) {
        node = new DefaultMutableTreeNode("float" + "(" + tokens.get(currentToken).getWord() + ")");
        parent.add(node);
        SemanticAnalyzer.pushStack(tokens.get(currentToken).getToken());
        currentToken++;
    }else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(")) {
    	isExpression = true;
      node = new DefaultMutableTreeNode("(");
      parent.add(node);
      currentToken++;
      //
      node = new DefaultMutableTreeNode("expression");
      parent.add(node);
      error = rule_expression(node);
      //
      if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")")) {
	      node = new DefaultMutableTreeNode(")");
	      parent.add(node);
	      currentToken++;
      }
      else{
    	  error(8);
    	  return true;
      }
    }
    else {
    	error(9);
    	if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR")) {
    		 node = new DefaultMutableTreeNode("error" + "(" + tokens.get(currentToken).getWord() + ")");
    	     parent.add(node);
    	     currentToken++;
    	}
      return true;
    }
    
    if(!isExpression)
    {
    	if(isIdentifier)
    		CodeGenerator.addInstruction("LOD", tokens.get(currentToken-1).getWord(), "0");
    	else
    		CodeGenerator.addInstruction("LIT", tokens.get(currentToken-1).getWord(), "0");
    }
    
    return false;
  }
  
  
  private static void pushIdentifierTypeToStack() {
	  if(SemanticAnalyzer.symbolTable.containsKey(tokens.get(currentToken).getWord()))
      {
    	  Vector<SymbolTableItem>  symbolTableItemVector= SemanticAnalyzer.symbolTable.get(tokens.get(currentToken).getWord());
    	  SymbolTableItem symbolTableItem = symbolTableItemVector.firstElement();
    	  SemanticAnalyzer.pushStack(symbolTableItem.getType());
      }
      else
      {
    	  SemanticAnalyzer.pushStack("ERROR");
    	  SemanticAnalyzer.error(gui, 4, tokens.get(currentToken).getLine(),tokens.get(currentToken).getWord());
      }
}


public static void error(int err) {  
	  
	  int n;
	  
	  /*
	  if(currentToken < tokens.size())
	  {
		  if(tokens.get(currentToken).getWord().equals(""))
			  System.out.println("Error found");
	  }
	  */
	  if(currentToken < tokens.size())
		  n= tokens.get(currentToken).getLine();
	  else
		  n=tokens.get(currentToken - 1).getLine();
	  
	  if(n!=1 && currentToken!=0 && (n != tokens.get(currentToken - 1).getLine()) && isLineNoToBeDecremented)
	  {
		  n--;
	  }
	  isLineNoToBeDecremented=true;
	  //else
		//  currentToken++;
		 /* 
	  switch (err) {
	    case 1: gui.writeConsole("Line" + n + ": expected {"); break; 
	    case 2: 
	    	n=n+1;
	    	gui.writeConsole("Line" + n + ": expected }"); break;
	    case 3: gui.writeConsole("Line" + n + ": expected ;"); break; 
	    case 4: 
	      gui.writeConsole("Line" +n+": expected identifier or keyword");
	      break; 
	    case 5: 
	      gui.writeConsole("Line" +n+": expected ="); break;
	    case 6:  
	      gui.writeConsole("Line" +n+": expected identifier"); break;
	    case 7:  
	      gui.writeConsole("Line" +n+": expected ("); break;
	    case 8:
	      gui.writeConsole("Line" +n+": expected )"); break; 
	    case 9: 
	      gui.writeConsole("Line" +n+": expected value, identifier, ("); 
	      break;
	  } 
	  */
	} 
  
  public static void errorRecovery(int recNo) { 
	  /*
	  int n = tokens.get(currentToken).getLine(); 
	  if(n!=1 && (n != tokens.get(currentToken - 1).getLine()))
		  return;
	  */
	  String[] firstFollowBody={"print","int","float","boolean","char","string","void","while","if","return","}"};
	  String[] firstExprWords={"!","-","true","false","("};
	  String[] followExprWords={")",";"};
	  String[] firstExprTokens={"INTEGER","FLOAT","HEXADECIMAL","OCTAL","BINARY","STRING","CHARACTER","IDENTIFIER"};
		  
	  switch (recNo) {
	    case 1: 
	    	while(currentToken < tokens.size()
	    			&& !Arrays.asList(firstFollowBody).contains(tokens.get(currentToken).getWord())
	    			&& !tokens.get(currentToken).getToken().equals("IDENTIFIER"))
	    	{
	    		currentToken++;
	    	}
	    	break; 
	    case 2:
	    	while(currentToken < tokens.size()
	    			&& !Arrays.asList(firstExprWords).contains(tokens.get(currentToken).getWord())
	    			&& !Arrays.asList(followExprWords).contains(tokens.get(currentToken).getWord())
	    			&& !Arrays.asList(firstExprTokens).contains(tokens.get(currentToken).getToken()))
	    	{
	    		currentToken++;
	    	}
	    	break;
	    case 3: 
	    	while(currentToken < tokens.size()
	    			&& !Arrays.asList(firstExprWords).contains(tokens.get(currentToken).getWord())
	    			&& !tokens.get(currentToken).getWord().equals(")")
	    			&& !Arrays.asList(firstExprTokens).contains(tokens.get(currentToken).getToken()))
	    	{
	    		currentToken++;
	    	}
	    	break; 
	    case 4: 
	    	while(currentToken < tokens.size()
	    			&& !tokens.get(currentToken).getWord().equals("{"))
	    	{
	    		currentToken++;
	    	}
	    	break; 
	    case 5: 
	    	while(currentToken < tokens.size()
	    			&& !tokens.get(currentToken).getWord().equals("{")
	    			&& !tokens.get(currentToken).getWord().equals("else"))
	    	{
	    		currentToken++;
	    	}
	    	break;
	  } 
	}
  
  private static void checkCubeForUnaryOpertor(String operator) {
	  String x = SemanticAnalyzer.popStack();
	  String result = SemanticAnalyzer.calculateCube(x, operator);
	  SemanticAnalyzer.pushStack(result);
}
  
  private static void checkCubeForBinaryOpertor(String operator) {
	  String x = SemanticAnalyzer.popStack();
	  String y = SemanticAnalyzer.popStack();
	  String result = SemanticAnalyzer.calculateCube(y, x, operator );
	  SemanticAnalyzer.pushStack(result);
}

  private static void checkBooleanInStack() {
	  String x = SemanticAnalyzer.popStack();
	  if(x.equals("boolean") || x.equals("BOOLEAN")){}
	  else
		  SemanticAnalyzer.error(gui, 3, tokens.get(currentToken).getLine(), tokens.get(currentToken).getWord());
	}
}
