package org.processmining.models.cnet.concepts;

import java.util.HashMap;
import java.util.Map;

import org.processmining.models.cnet.CNetNode;

/**
 * This class stores the number of obligations from one node to another in a
 * cnet.
 * 
 * @author aadrians Oct 7, 2011
 * 
 */
public class CNetObligation extends HashMap<CNetNode, Map<CNetNode, Integer>> {
	private static final long serialVersionUID = 866072866122831878L;

	/**
	 * Add obligations between a source node and a target node
	 * 
	 * @param source
	 * @param target
	 * @param numObligations
	 * @return
	 */
	public int addObligations(CNetNode source, CNetNode target, int numObligations) {
		Map<CNetNode, Integer> map = get(source);
		if (map == null) {
			map = new HashMap<CNetNode, Integer>();
			put(source, map);
		}
		Integer oldValue = map.get(target);
		if (oldValue == null) {
			map.put(target, numObligations);
			return numObligations;
		} else {
			map.put(target, numObligations + oldValue);
			return (numObligations + oldValue);
		}
	}

	/**
	 * remove obligations from source node to target node. Return the number of
	 * remaining obligation from the source to the target node (can be negative
	 * if the number of removed obligations is higher than the number of
	 * dangling obligations
	 * 
	 * @param source
	 * @param target
	 * @param numObligations
	 * @return
	 */
	public int removeObligations(CNetNode source, CNetNode target, int numObligations) {
		Map<CNetNode, Integer> map = get(source);
		if (map != null) {
			map = new HashMap<CNetNode, Integer>();
			put(source, map);
		}
		Integer num = map.get(target);
		if (num == null) {
			map.put(target, -1 * numObligations);
			return (-1 * numObligations);
		} else {
			num = num - numObligations;
			return num;
		}
	}

	/**
	 * Get the number of obligations from a source node to a target node. Return
	 * 0 if there is no such obligations.
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public int getNumObligations(CNetNode source, CNetNode target) {
		Map<CNetNode, Integer> map = get(source);
		if (map != null) {
			Integer num = map.get(target);
			return (num == null) ? 0 : num;
		}
		return 0;
	}
}
