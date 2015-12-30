package instruction;

import runtime.ExecutionException;
import runtime.Runtime;
import runtime.Stack;
import value.DoubleValue;

public class BitwiseAndInstruction implements Instruction {
	public BitwiseAndInstruction() {
	}
	
	public static Instruction BitwiseAnd() {
		return new BitwiseAndInstruction();
	}
	
	public void execute(Runtime runtime) throws ExecutionException {
		Stack stack = runtime.getStack();
		DoubleValue value2 = runtime.popCheckedDoubleValue();
		DoubleValue value1 = runtime.popCheckedDoubleValue();
		stack.push(DoubleValue.Value((int)value1.getValue() & (int)value2.getValue()));
	}
	
	public String toString() {
		return "BITWISEAND";
	}
}