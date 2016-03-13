package backend.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import backend.instruction.Instruction;
import backend.value.FunctionValue;

public class VizObjectsTest {
	@Test
	public void simple() {
		assertVizObjects("@rect();", Arrays.asList(
			Arrays.asList("rect()")
		));
		
		assertVizObjects("@rect(x: 1);", Arrays.asList(
			Arrays.asList("rect(x: 1)")
		));
		
		assertVizObjects("@rect(x: 1, y: 2);", Arrays.asList(
			Arrays.asList("rect(x: 1, y: 2)")
		));
	}
	
	@Test
	public void readVariable() {
		assertVizObjects("var a = 2; @rect(x: a);", Arrays.asList(
			Arrays.asList("rect(x: 2)")
		));
	}
	
	@Test
	public void readFunction() {
		assertVizObjects("function f() { return 5; }; @rect(x: f());", Arrays.asList(
			Arrays.asList("rect(x: 5)")
		));
	}
	
	@Test
	public void observeVariable() {
		assertVizObjects("var a = 2; @rect(x: a); a = 3;", Arrays.asList(
			Arrays.asList("rect(x: 2)"),
			Arrays.asList("rect(x: 3)")
		));
	}
	
	@Test
	public void multipleObjects() {
		assertVizObjects("@rect(x: 1); @rect(x: 2); @rect(x: 3);", Arrays.asList(
			Arrays.asList("rect(x: 1)"),
			Arrays.asList("rect(x: 1)", "rect(x: 2)"),
			Arrays.asList("rect(x: 1)", "rect(x: 2)", "rect(x: 3)")
		));
	}
		
	@Test
	public void forLoop() {
		assertVizObjects("var a = [1, 2, 3]; @for(x <- a) rect(x: x);", Arrays.asList(
			Arrays.asList("rect(x: 1)", "rect(x: 2)", "rect(x: 3)")
		));
	}
	
	@Test
	public void forLoopWithConditional() {
		assertVizObjects("var a = [1, 2, 3]; @for(x <- a, x >= 2) rect(x: x);", Arrays.asList(
			Arrays.asList("rect(x: 2)", "rect(x: 3)")
		));
	}
	
	@Test
	public void nestedForLoops() {
		assertVizObjects("var a = [[1, 2], [3, 4]]; @for(x <- a, y <- x) rect(x: y);", Arrays.asList(
			Arrays.asList("rect(x: 1)", "rect(x: 2)", "rect(x: 3)", "rect(x: 4)")
		));
	}
	
	@Test
	public void multipleForLoops() {
		assertVizObjects("var a = [1, 2]; var b = [3, 4]; @for(x <- a, y <- b) rect(x: x, y: y);", Arrays.asList(
			Arrays.asList("rect(x: 1, y: 3)", "rect(x: 1, y: 4)", "rect(x: 2, y: 3)", "rect(x: 2, y: 4)")
		));
	}
	
	@Test
	public void observeArray() {
		assertVizObjects("var a = [1, 2, 3]; @for(x <- a) rect(x: x); a.push(4);", Arrays.asList(
			Arrays.asList("rect(x: 1)", "rect(x: 2)", "rect(x: 3)"),
			Arrays.asList("rect(x: 1)", "rect(x: 2)", "rect(x: 3)", "rect(x: 4)")
		));
	}
	
	@Test
	public void observeArrayElement() {
		assertVizObjects("var a = [1, 2, 3]; @for(x <- a) rect(x: x); a[0] = 4;", Arrays.asList(
			Arrays.asList("rect(x: 1)", "rect(x: 2)", "rect(x: 3)"),
			Arrays.asList("rect(x: 4)", "rect(x: 2)", "rect(x: 3)")
		));
	}
	
	@Test
	public void observeObjectProperty() {
		assertVizObjects("var a = { b: 2 }; @rect(x: a.b); a.b = 3;", Arrays.asList(
			Arrays.asList("rect(x: 2)"),
			Arrays.asList("rect(x: 3)")
		));
	}
	
	@Test
	public void withinFunction() {
		assertVizObjects("@rect(x: 1); function f() { @rect(x: 2); }; f();", Arrays.asList(
			Arrays.asList("rect(x: 1)"),
			Arrays.asList("rect(x: 1)", "rect(x: 2)"),
			Arrays.asList("rect(x: 1)")
		));
	}
	
	@Test
	public void multipleFunctionCalls() {
		assertVizObjects("function f(x) { @rect(x: x); }; f(1); f(2);", Arrays.asList(
			Arrays.asList("rect(x: 1)"),
			Arrays.asList("rect(x: 2)")
		));
	}
	
	@Test
	public void vizObjectWithConditional() {
		assertVizObjects("@rect(x: true ? 1 : 2);", Arrays.asList(
			Arrays.asList("rect(x: 1)")
		));
	}
	
	@Test
	public void multipleObjects_forwardAndBackward() {
		assertVizObjectsForwardAndBackward("@rect(x: 1); @rect(x: 2); @rect(x: 3);", Arrays.asList(
			Arrays.asList("rect(x: 1)"),
			Arrays.asList("rect(x: 1)", "rect(x: 2)"),
			Arrays.asList("rect(x: 1)", "rect(x: 2)", "rect(x: 3)"),
			Arrays.asList("rect(x: 1)", "rect(x: 2)"),
			Arrays.asList("rect(x: 1)")
		));
	}
	
	@Test
	public void multipleFunctionCalls_forwardAndBackward() {
		assertVizObjectsForwardAndBackward("function f(x) { @rect(x: x); }; f(1); f(2); f(3);", Arrays.asList(
			Arrays.asList("rect(x: 1)"),
			Arrays.asList("rect(x: 2)"),
			Arrays.asList("rect(x: 3)"),
			Arrays.asList("rect(x: 2)"),
			Arrays.asList("rect(x: 1)")
		));
	}
	
	@Test
	public void vizObjectsDoNotChangeState() {
		assertStateNotChanged("@rect();");
		assertStateNotChanged("@rect(x: 1);");
		assertStateNotChanged("@for(true) rect(x: 1);");
		assertStateNotChanged("@for(x <- [1]) rect(x: 1);");
		assertStateNotChanged("@for(x <- [1]) rect();");
		assertStateNotChanged("@for(x <- [1], y <- [2]) rect();");
		assertStateNotChanged("@for(x <- [y = 1]) rect();");
		assertStateNotChanged("@for(x <- [print()]) rect();");
	}
	
	private static void assertVizObjects(String program, List<List<String>> expectedObjects) {
		assertVizObjects(program, expectedObjects, false);
	}
	
	private static void assertVizObjectsForwardAndBackward(String program, List<List<String>> expectedObjects) {
		assertVizObjects(program, expectedObjects, true);
	}
	
	private static void assertVizObjects(String program, List<List<String>> expectedObjects, boolean stepBackwardsToo) {
		Runtime runtime = new Runtime();
		List<Instruction> instructions = Engine.compile(program);
		Engine engine = new Engine(runtime, instructions);
		
		List<List<String>> actualObjects = new ArrayList<>();
		
		// Step forwards to end of program.
		while(runtime.getCurrentStackFrame() != null) {
			engine.stepForward();
			if(!runtime.getVizObjects().isEmpty()) {
				actualObjects.add(toStringList(runtime.getVizObjects()));
			}
		}
		
		// Step backwards to beginning of program.
		if(stepBackwardsToo) {
			while(runtime.getUndoStack().getSize() > 0) {
				engine.stepBackward();
				if(!runtime.getVizObjects().isEmpty()) {
					actualObjects.add(toStringList(runtime.getVizObjects()));
				}
			}
		}
		
		System.out.println("ACTUAL: " + actualObjects);
		System.out.println("EXPECTED: " + expectedObjects);
		
		collapseDuplicates(actualObjects);
		
		assertEquals(expectedObjects, actualObjects);
		
		assertTrue(runtime.getOutput().isEmpty());
		assertTrue(runtime.getErrors().isEmpty());
		assertTrue(runtime.getStack().isEmpty());
	}

	/**
	 * Removes consecutive duplicate items in the given list.
	 */
	private static void collapseDuplicates(List<? extends Object> list) {
		Iterator<? extends Object> iterator = list.iterator();
		Object lastValue = null;
		while(iterator.hasNext()) {
			Object value = iterator.next();
			if(value.equals(lastValue)) {
				iterator.remove();
			}
			lastValue = value;
		}
	}

	/**
	 * Converts a list of Objects into a list of Strings.
	 */
	private static List<String> toStringList(List<? extends Object> list) {
		List<String> stringList = new ArrayList<>();
		for(Object x:list) {
			stringList.add(x.toString());
		}
		return stringList;
	}
	
	/**
	 * Asserts that the state before and after vizObjectInstructions are run is the same.
	 */
	private void assertStateNotChanged(String vizObjectInstructions) {
		Runtime runtime = new Runtime();
		List<Instruction> instructions = Engine.compile(vizObjectInstructions);
		instructions = instructions.subList(1, instructions.size() - 2);
		runtime.addStackFrame(new FunctionValue(new GlobalScope(runtime.getUndoStack()), 0, new ArrayList<Instruction>()));
		
		// Add some dummy values to the undo stack so that it's not empty.
		runtime.getUndoStack().addCommandUndo(null);
		runtime.getUndoStack().addPopStackFrameUndo(null);
		runtime.getUndoStack().addPopValueUndo(null);
		
		// Save initial state.
		String initialState = runtime.getState();
		String initialUndoStack = runtime.getUndoStack().getState();
		
		// Run viz object instructions.
		new VizObjectInstructions(runtime, instructions).updateObjects();
		
		// Check that the state is the same.
		assertEquals(initialUndoStack, runtime.getUndoStack().getState());
		assertEquals(initialState, runtime.getState());
	}
}