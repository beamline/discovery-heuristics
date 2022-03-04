package org.processmining.models.cnet;

import java.util.UUID;

/**
 * @author aadrians Oct 6, 2011
 * 
 */
public class CNetNode implements Comparable<CNetNode> {

	private final UUID id = UUID.randomUUID();

	/**
	 * The label of the node
	 */
	protected final String label;

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	public CNetNode(String label) {
		this.label = label;

	}

	/**
	 * comparison by label
	 */
	public int compareTo(CNetNode other) {
		int c = this.label.compareTo(other.getLabel());
		if (c != 0) {
			return c;
		} else {
			return (id.compareTo(other.id));
		}
	}

	/**
	 * Compares IDs
	 */
	public boolean equals(Object o) {
		return (o instanceof CNetNode) && id.equals(((CNetNode) o).id);
	}

	/**
	 * Nodes with the same ID needs to have the same hash code (Java contract)
	 */
	public int hashCode() {
		return id.hashCode();
	}

	public String toString() {
		return label;
	}
}
