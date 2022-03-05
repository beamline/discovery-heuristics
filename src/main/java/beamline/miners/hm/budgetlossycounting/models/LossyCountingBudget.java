package beamline.miners.hm.budgetlossycounting.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.processmining.framework.util.Pair;

public abstract class LossyCountingBudget<V> extends HashMap<V, Pair<Integer, Integer>> {

	private static final long serialVersionUID = -5610289457434845311L;
	private SharedDelta delta;
	
	public LossyCountingBudget(SharedDelta delta) {
		this.delta = delta;
	}
	
	public void addObservation(V newValue) {
		if (containsKey(newValue)) {
			Pair<Integer, Integer> v = get(newValue);
			put(newValue, new Pair<Integer, Integer>(v.getFirst() + 1, v.getSecond()));
		} else {
			if (delta.getSize() == delta.budget) {
				cleanup();
			}
			put(newValue, new Pair<Integer, Integer>(1, delta.currentBucket));
		}
	}
	
	public boolean removeBelowDelta() {
		boolean removedOneItem = false;
		Iterator<Map.Entry<V, Pair<Integer, Integer>>> iter = entrySet().iterator();
		while (iter.hasNext()) {
			Pair<Integer, Integer> pair = iter.next().getValue();
			if (pair.getFirst() + pair.getSecond() <= delta.currentBucket) {
				iter.remove();
				removedOneItem = true;
			}
		}
		return removedOneItem;
	}
	
	private void cleanup() {
		delta.currentBucket++;
		
		boolean removedOneItem = removeBelowDelta();
		
		if (!removedOneItem) {
			Integer minValue = Integer.MAX_VALUE;
			Iterator<Map.Entry<V, Pair<Integer, Integer>>> iter = entrySet().iterator();
			while (iter.hasNext()) {
				Pair<Integer, Integer> pair = iter.next().getValue();
				if (pair.getFirst() + pair.getSecond() < minValue) {
					minValue = pair.getFirst() + pair.getSecond();
				}
			}
			delta.currentBucket = minValue;
			delta.removeFromAll();
		}
	}
	
	public void print() {
		for (V k : keySet()) {
			System.out.println(k + " : " + get(k).getFirst() + ", " + get(k).getSecond());
		}
	}
}
