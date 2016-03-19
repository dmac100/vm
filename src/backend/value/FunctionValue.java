package backend.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import backend.instruction.Instruction;
import backend.runtime.HasState;
import backend.runtime.Scope;
import backend.runtime.VizObject;
import backend.runtime.VizObjectInstructions;

public class FunctionValue extends Value implements HasState {
	private final List<Instruction> instructions;
	private final int paramCount;
	
	private Scope parentScope;
	
	public FunctionValue(Scope parentScope, int paramCount, List<Instruction> instructions) {
		this.parentScope = parentScope;
		this.paramCount = paramCount;
		this.instructions = instructions;
	}
	
	public Scope getParentScope() {
		return parentScope;
	}
	
	public List<Instruction> getInstructions() {
		return instructions;
	}
	
	public int getParamCount() {
		return paramCount;
	}
	
	public String toString(Set<Value> used) {
		return "[Function]";
		//return "[" + instructions + "]";
	}
	
	public String getState(String prefix, Set<Object> used) {
		if(used.contains(this)) return prefix + "[CYCLIC]";
		used.add(this);
		
		StringBuilder s = new StringBuilder();
		s.append(prefix + "Instructions: " + instructions).append("\n");
		s.append(prefix + "ParentScope: ").append("\n");
		s.append(parentScope.getState(prefix + "  ", used));
		
		used.remove(this);
		
		return s.toString();
	}
	
	@Override
	public Object getKey() {
		return this;
	}
}
