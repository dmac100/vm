package instruction.function;

import instruction.Instruction;
import runtime.ExecutionException;
import runtime.Runtime;
import value.DoubleValue;
import value.FunctionValue;
import value.NativeFunctionValue;
import value.NullValue;
import value.Value;

public class CallInstruction implements Instruction {
	public CallInstruction() {
	}
	
	public static Instruction Call() {
		return new CallInstruction();
	}
	
	public void execute(Runtime runtime) throws ExecutionException {
		Value value = runtime.getStack().popValue(true, false);
		if(value instanceof NativeFunctionValue) {
			((NativeFunctionValue)value).execute(runtime);
		} else if(value instanceof FunctionValue) {
			FunctionValue function = (FunctionValue) value;
			fixParamCount(runtime, function);
			runtime.addStackFrame(function);
		} else {
			throw new ExecutionException("TypeError: Not a function: " + value);
		}
	}

	/**
	 * If too few params are passed, add null values. If too many, remove these values.
	 */
	private void fixParamCount(Runtime runtime, FunctionValue function) throws ExecutionException {
		int passedParams = (int) runtime.checkDoubleValue(runtime.getStack().popValue(true, false)).getValue();
		int functionParams = function.getParamCount();
		
		for(int x = passedParams; x < functionParams; x++) {
			runtime.getStack().push(new NullValue(), true);
		}
		
		for(int x = functionParams; x < passedParams; x++) {
			runtime.getStack().popValue(true, false);
		}
		
		runtime.getStack().push(new DoubleValue(passedParams), true);
	}
	
	public void undo(Runtime runtime) {
	}

	public String toString() {
		return "CALL";
	}
}