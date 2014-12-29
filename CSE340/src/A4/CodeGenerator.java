package A4;

import java.util.Vector;

/**
 *
 * @author javiergs
 */
public class CodeGenerator {
  
  private static final Vector<String> variables = new Vector<>();  
  private static final Vector<String> labels = new Vector<>();  
  private static final Vector<String> instructions = new Vector<>();
  public static int instructionCounter =0;

  static void addInstruction(String instruction, String p1, String p2) {
    instructions.add(instruction + " " + p1 + ", " + p2);
    instructionCounter++;
  }

  static void addLabel(String name, int value) {
    labels.add("#"+name + ", int, " + value);
  }
    
  static void addVariable(String type, String name) {
    variables.add(name + ", " + type + ", global, 0" );
  }

  static void writeCode(Gui gui) {
    for (String variable : variables) {
      gui.writeCode(variable);    
    }
    for (String label : labels) {
      gui.writeCode(label);    
    }
    gui.writeCode("@");
    for (String instruction : instructions) {
      gui.writeCode(instruction);    
    }

  }
  
  static void clear(Gui gui) {
    variables.clear();
    instructions.clear();
    labels.clear(); 
  }  
}
