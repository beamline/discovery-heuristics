package org.processmining.models.cnet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * @author aadrians Oct 6, 2011
 * 
 */
public class CNetBinding {

	public static enum Type {
		/**
		 * represents an input binding
		 */
		INPUT,
		/**
		 * represents an output binding
		 */
		OUTPUT
	}

	/**
	 * Contains the bound activities of this binding
	 */
	private final Set<? extends CNetNode> boundNodes;

	/**
	 * Points to the activity this binding belongs to
	 */
	private final CNetNode node;

	/**
	 * The type of the binding
	 */
	private final Type type;

	/**
	 * Instantiates a binding using CNetNodes.
	 * 
	 * @param net
	 * @param type
	 * @param node
	 * @param boundNodes
	 */
	public CNetBinding(Type type, CNetNode node, CNetNode... boundNodes) {
		this(type, node, Arrays.asList(boundNodes));
	}

	/**
	 * Instantiates a binding using CNetNodes.
	 * 
	 * @param net
	 * @param type
	 * @param node
	 * @param boundNodes
	 */
	public CNetBinding(Type type, CNetNode node, Collection<? extends CNetNode> boundNodes) {
		this.type = type;
		this.node = node;
		this.boundNodes = new HashSet<CNetNode>(boundNodes);
	}

	/**
	 * Instantiates a binding using CNetNodes.
	 * 
	 * @param net
	 * @param type
	 * @param node
	 * @param boundNodes
	 */
	public CNetBinding(Type type, CNetNode node, Set<? extends CNetNode> boundNodes) {
		this.type = type;
		this.node = node;
		this.boundNodes = boundNodes;
	}

	/**
	 * Returns the node to which this binding belongs
	 * 
	 * @return
	 */
	public CNetNode getNode() {
		return node;
	}

	/**
	 * returns the set of nodes bound by this binding
	 * 
	 * @return
	 */
	public Set<CNetNode> getBoundNodes() {
		return ImmutableSet.copyOf(boundNodes);
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((boundNodes == null) ? 0 : boundNodes.hashCode());
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CNetBinding))
			return false;
		CNetBinding other = (CNetBinding) obj;
		if (boundNodes == null) {
			if (other.boundNodes != null)
				return false;
		} else if (!boundNodes.equals(other.boundNodes))
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public Type getType() {
		return type;
	}

	public String toString() {
		return type.toString() + " of " + node.toString() + " : " + boundNodes.toString();
	}
}
