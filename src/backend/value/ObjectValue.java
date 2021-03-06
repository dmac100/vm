package backend.value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import backend.observer.ValueObserverList;
import backend.observer.ValueReadObserver;
import backend.runtime.ExecutionException;
import backend.runtime.HasState;
import backend.runtime.UndoStack;

public class ObjectValue extends Value implements HasState, HasPropertiesObject {
	private final SortedMap<String, Value> values = new TreeMap<>();
	private final UndoStack undoStack;
	private final ValueObserverList<String> valueObserverList = new ValueObserverList<>();
	
	public ObjectValue(UndoStack undoStack) {
		this.undoStack = undoStack;
	}
	
	public Value get(String name, ValueReadObserver valueReadObserver) {
		valueObserverList.onReadValue(valueReadObserver, null);
		
		if(values.containsKey(name)) {
			return values.get(name);
		} else if(values.get("prototype") instanceof ObjectValue) {
			return ((ObjectValue)values.get("prototype")).get(name, valueReadObserver);
		} else {
			return new NullValue();
		}
	}
	
	public void set(final String name, final Value value) throws ExecutionException {
		if(values.containsKey(name)) {
			final Value oldValue = values.get(name);
			undoStack.addCommandUndo(new Runnable() {
				public void run() {
					values.put(name, oldValue);
					valueObserverList.onChangeValue(null);
				}
				
				public String toString() {
					return "[OBJECT SET]";
				}
			});
		} else {
			undoStack.addCommandUndo(new Runnable() {
				public void run() {
					values.remove(name);
					valueObserverList.onChangeValue(null);
				}
				
				public String toString() {
					return "[OBJECT SET]";
				}
			});
		}
		
		values.put(name, value);
		checkCyclicPrototype(new HashSet<>());
		
		valueObserverList.onChangeValue(null);
	}

	private void checkCyclicPrototype(HashSet<Object> used) throws ExecutionException {
		if(used.contains(this)) throw new ExecutionException("TypeError: Cyclic prototype");
		used.add(this);
		
		if(values.get("prototype") instanceof ObjectValue) {
			((ObjectValue)values.get("prototype")).checkCyclicPrototype(used);
		}
	}

	public List<String> keys(ValueReadObserver valueReadObserver) {
		valueObserverList.onReadValue(valueReadObserver, null);
		return new ArrayList<>(values.keySet());
	}
	
	public String toString() {
		return toString(new HashSet<Value>());
	}
	
	public String toString(Set<Value> used) {
		if(used.contains(this)) return "[CYCLIC]";
		used.add(this);
		
		StringBuilder s = new StringBuilder();
		for(String key:values.keySet()) {
			if(!key.equals("prototype")) {
				if(s.length() != 0) {
					s.append(", ");
				}
				s.append(key + ":" + values.get(key).toString(used));
			}
		}
		
		used.remove(this);
		
		return "{" + s.toString() + "}";
	}

	@Override
	public String inspect(String indent, Set<Value> used) {
		if(used.contains(this)) return indent + "[CYCLIC]";
		used.add(this);
		
		List<String> keys = new ArrayList<>();
		for(String key:values.keySet()) {
			if(!key.equals("prototype")) {
				keys.add(key);
			}
		}
		
		StringBuilder s = new StringBuilder();
		s.append(indent).append("{").append("\n");
		for(int i = 0; i < keys.size(); i++) {
			s.append(indent);
			s.append("    ").append(keys.get(i)).append(":");
			s.append(" ").append(values.get(keys.get(i)).inspect(indent + "    ", used).trim());
			if(i < values.size()) {
				s.append(",");
			}
			s.append("\n");
		}
		s.append(indent).append("}");
		
		used.remove(this);
		
		return s.toString();
	}
	
	public String getState(String prefix, Set<Object> used) {
		if(used.contains(this)) return prefix + "[CYCLIC]";
		used.add(this);
		
		StringBuilder s = new StringBuilder();
		s.append(prefix + "Object:").append("\n");
		for(String key:values.keySet()) {
			s.append(prefix + "  Key: " + key).append("\n");
			s.append(prefix + "  Value:").append("\n");
			s.append(values.get(key).getState(prefix + "    ", used)).append("\n");
		}
		
		used.remove(this);
		
		return s.toString().replaceAll("\\s+$", "");
	}
	
	@Override
	public Object getKey() {
		Value key = values.get("key");
		return (key == null || key == this) ? this : key.getKey();
	}

	@Override
	public ObjectValue getPropertiesObject() {
		return this;
	}
}
