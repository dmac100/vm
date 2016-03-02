package instruction;

import runtime.ExecutionException;
import runtime.Runtime;
import runtime.Stack;
import value.DoubleValue;
import value.Value;

public class UnaryMinusInstruction implements Instruction {
	public UnaryMinusInstruction() {
	}
	
	public static Instruction UnaryMinus() {
		return new UnaryMinusInstruction();
	}
	
	public void execute(Runtime runtime) throws ExecutionException {
		Stack stack = runtime.getStack();
		DoubleValue value = runtime.checkDoubleValue(stack.popValue(true));
		stack.push(DoubleValue.Value(-value.getValue()), false);
	}
	
	public void undo(Runtime runtime) {
		runtime.getStack().popValue(false);
	}
	
	public String toString() {
		return "UNARYMINUS";
	}
}