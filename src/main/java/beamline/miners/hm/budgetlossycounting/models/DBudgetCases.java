package beamline.miners.hm.budgetlossycounting.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.processmining.framework.util.Pair;

public class DBudgetCases extends HashMap<String, Pair<Pair<String, Integer>, Integer>> {

	private static final long serialVersionUID = 6845190327962344569L;
	private HashMap<String, Integer> startingActivities;
	private HashMap<String, Integer> finishingActivities;
	private SharedDelta delta;
	
	public DBudgetCases(SharedDelta delta, HashMap<String, Integer> startingActivities, HashMap<String, Integer> finishingActivities) {
		this.startingActivities = startingActivities;
		this.finishingActivities = finishingActivities;
		this.delta = delta;
		delta.cases = this;
	}
	
	public String addObservation(String caseId, String latestActivity) {
		if (containsKey(caseId)) {
			Pair<Pair<String, Integer>, Integer> v = get(caseId);
			put(caseId, new Pair<Pair<String, Integer>, Integer>(new Pair<String, Integer>(latestActivity, v.getFirst().getSecond() + 1), v.getSecond()));
			return v.getFirst().getFirst();
		} else {
			if (delta.getSize() == delta.budget) {
				cleanup();
			}
			incrementIntHashMap(startingActivities, latestActivity);
			put(caseId, new Pair<Pair<String, Integer>, Integer>(new Pair<String, Integer>(latestActivity, 1), delta.currentBucket));
		}
		return null;
	}
	
	public boolean removeBelowDelta() {
		boolean removedOneItem = false;
		Iterator<Map.Entry<String, Pair<Pair<String, Integer>, Integer>>> iter = entrySet().iterator();
		while (iter.hasNext()) {
			Pair<Pair<String, Integer>, Integer> pair = iter.next().getValue();
			if (pair.getFirst().getSecond() + pair.getSecond() <= delta.currentBucket) {
				incrementIntHashMap(finishingActivities, pair.getFirst().getFirst());
				iter.remove();
				removedOneItem = true;
			}
		}
		return removedOneItem;
	}
	
	public HashMap<String, Integer> getFinishingActivities() {
		HashMap<String, Integer> tmp = new HashMap<String, Integer>();
		for (Iterator<String> it = keySet().iterator(); it.hasNext();) {
			String caseId = it.next();
			Pair<Pair<String, Integer>, Integer> v = get(caseId);
			String activity = v.getFirst().getFirst();
			if (tmp.containsKey(activity)) {
				tmp.put(activity, tmp.get(activity) + 1);
			} else {
				tmp.put(activity, 1);
			}
		}
		return tmp;
	}
	
	private void cleanup() {
		delta.currentBucket++;
		
		boolean removedOneItem = removeBelowDelta();
		
		if (!removedOneItem) {
			Integer minValue = Integer.MAX_VALUE;
			Iterator<Map.Entry<String, Pair<Pair<String, Integer>, Integer>>> iter = entrySet().iterator();
			while (iter.hasNext()) {
				Pair<Pair<String, Integer>, Integer> pair = iter.next().getValue();
				if (pair.getFirst().getSecond() + pair.getSecond() < minValue) {
					minValue = pair.getFirst().getSecond() + pair.getSecond();
				}
			}
			delta.currentBucket = minValue;
			delta.removeFromAll();
		}
	}
	
	private void incrementIntHashMap(HashMap<String, Integer> hm, String key) {
		Integer freq = hm.get(key);
		if (freq == null) {
			hm.put(key, 1);
		} else {
			hm.put(key, freq + 1);
		}
	}
	
	public void print() {
		for (String k : keySet()) {
			System.out.println(k + " : " + get(k).getFirst() + ", " + get(k).getSecond());
		}
	}
	
	public void lastActivityObserved(String activityName) {
		remove(activityName);
		incrementIntHashMap(finishingActivities, activityName);
	}
}
