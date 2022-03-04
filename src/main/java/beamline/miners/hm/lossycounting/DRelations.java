package beamline.miners.hm.lossycounting;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;

/**
 * This data structure is used to manage the dependency relations for the Lossy
 * Counting algorithm.
 * 
 * @author Andrea Burattin
 */
public class DRelations extends HashMap<Pair<String, String>, Pair<Integer, Integer>> {

	private static final long serialVersionUID = 2976660878859351556L;
	private int size = 0;

	/**
	 * 
	 * @param sourceActivity
	 * @param destinationActivity
	 * @param currentBucket
	 */
	public void addRelationObservation(String sourceActivity, String destinationActivity, Integer currentBucket) {
		Pair<String, String> relationName = Pair.of(sourceActivity, destinationActivity);
		
		if (containsKey(relationName)) {
			Pair<Integer, Integer> v = get(relationName);
			put(relationName, Pair.of(v.getLeft() + 1, v.getRight()));
		} else {
			put(relationName, Pair.of(1, currentBucket - 1));
			size++;
		}
	}
	
	/**
	 * 
	 * @param currentBucket
	 */
	public void cleanup(Integer currentBucket) {
		for (Iterator<Pair<String, String>> it = keySet().iterator(); it.hasNext();) {
			Pair<String, String> relationName = it.next();
			Pair<Integer, Integer> v = get(relationName);
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
	public HashMap<Pair<String, String>, Double> getRelations() {
		HashMap<Pair<String, String>, Double> relations = new HashMap<Pair<String, String>, Double>();
		for (Iterator<Pair<String, String>> it = keySet().iterator(); it.hasNext();) {
			Pair<String, String> relationName = it.next();
			relations.put(relationName, (double)get(relationName).getLeft());
		}
		return relations;
	}
	
	/**
	 * 
	 * @return
	 */
	public Integer getSize() {
		return size;
	}
}
