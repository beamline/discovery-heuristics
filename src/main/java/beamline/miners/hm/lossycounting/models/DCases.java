package beamline.miners.hm.lossycounting.models;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;

/**
 * This data structure is used to manage the different cases for the Lossy
 * Counting algorithm.
 * 
 * @author Andrea Burattin
 */
public class DCases extends HashMap<String, Pair<Pair<String, Integer>, Integer>> {

	private static final long serialVersionUID = 2639809234632036510L;
	private int size = 0;
	private HashMap<String, Integer> startingActivities;
	private HashMap<String, Integer> finishingActivities;
	
	/**
	 * 
	 * @param startingActivities
	 * @param finishingActivities
	 */
	public DCases(HashMap<String, Integer> startingActivities, HashMap<String, Integer> finishingActivities) {
		this.startingActivities = startingActivities;
		this.finishingActivities = finishingActivities;
	}

	/**
	 * 
	 * @param caseId
	 * @param latestActivity
	 * @param currentBucket
	 * @return
	 */
	public String addCaseObservation(String caseId, String latestActivity, Integer currentBucket) {
		if (containsKey(caseId)) {
			Pair<Pair<String, Integer>, Integer> v = get(caseId);
			put(caseId, Pair.of(Pair.of(latestActivity, v.getLeft().getRight() + 1), v.getRight()));
			return v.getLeft().getLeft();
		} else {
			put(caseId, Pair.of(Pair.of(latestActivity, 1), currentBucket - 1));
			
			// starting activity
			incrementIntHashMap(startingActivities, latestActivity);
			size++;
		}
		return null;
	}
	
	public HashMap<String, Integer> getFinishingActivities() {
		HashMap<String, Integer> tmp = new HashMap<String, Integer>();
		for (Iterator<String> it = keySet().iterator(); it.hasNext();) {
			String caseId = it.next();
			Pair<Pair<String, Integer>, Integer> v = get(caseId);
			String activity = v.getLeft().getLeft();
			if (tmp.containsKey(activity)) {
				tmp.put(activity, tmp.get(activity) + 1);
			} else {
				tmp.put(activity, 1);
			}
		}
		return tmp;
	}
	
	/**
	 * 
	 * @param currentBucket
	 */
	public void cleanup(Integer currentBucket) {
		for (Iterator<String> it = keySet().iterator(); it.hasNext();) {
			String caseId = it.next();
			Pair<Pair<String, Integer>, Integer> v = get(caseId);
			Integer age = v.getLeft().getRight() + v.getRight();
			if (age <= currentBucket) {

				// update finishing act
				incrementIntHashMap(finishingActivities, v.getLeft().getLeft());

				it.remove();
				size--;
			}
		}
	}
	
	/**
	 * 
	 * @param hm
	 * @param key
	 */
	private void incrementIntHashMap(HashMap<String, Integer> hm, String key) {
		Integer freq = hm.get(key);
		if (freq == null) {
			hm.put(key, 1);
		} else {
			hm.put(key, freq + 1);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public Integer getSize() {
		return size;
	}
}
