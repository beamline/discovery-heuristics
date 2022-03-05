package beamline.miners.hm.lossycounting.models;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;

/**
 * This data structure is used to manage the activity for the Lossy Counting
 * algorithm.
 * 
 * @author Andrea Burattin
 */
public class DActivities extends HashMap<String, Pair<Integer, Integer>> {

	private static final long serialVersionUID = -1093789267479291150L;
	private int size = 0;

	/**
	 * 
	 * @param activityName
	 * @param currentBucket
	 */
	public void addActivityObservation(String activityName, Integer currentBucket) {
		if (containsKey(activityName)) {
			Pair<Integer, Integer> v = get(activityName);
			put(activityName, Pair.of(v.getLeft() + 1, v.getRight()));
		} else {
			put(activityName, Pair.of(1, currentBucket - 1));
			size++;
		}
	}
	
	/**
	 * 
	 * @param currentBucket
	 */
	public void cleanup(Integer currentBucket) {
		for (Iterator<String> it = keySet().iterator(); it.hasNext();) {
			String activity = it.next();
			Pair<Integer, Integer> v = get(activity);
			Integer age = v.getLeft() + v.getRight();
			if (age <= currentBucket) {
				it.remove();
				size--;
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public HashMap<String, Double> getActivities() {
		HashMap<String, Double> activities = new HashMap<String, Double>();
		for (Iterator<String> it = keySet().iterator(); it.hasNext();) {
			String activity = it.next();
			activities.put(activity, (double)get(activity).getLeft());
		}
		return activities;
	}
	
	/**
	 * 
	 * @return
	 */
	public Integer getSize() {
		return size;
	}
}
