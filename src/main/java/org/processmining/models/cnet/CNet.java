package org.processmining.models.cnet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.processmining.models.cnet.CNetBinding.Type;

import com.google.common.collect.ImmutableSet;

/**
 * Clean implementation of cnet
 * 
 * @author aadrians Oct 6, 2011
 * 
 */
public class CNet implements CausalNet {

	/**
	 * label of the CNet
	 */
	protected final String label;

	/**
	 * The nodes in this CNet
	 */
	protected final Set<CNetNode> nodes;

	/**
	 * The input bindings in the CNet
	 */
	protected final Map<CNetNode, Set<CNetBinding>> inputBindings;

	/**
	 * The output bindings in the CNet
	 */
	protected final Map<CNetNode, Set<CNetBinding>> outputBindings;

	/**
	 * The startNode of the CNet
	 */
	protected CNetNode startNode = null;

	/**
	 * The endNode of the CNet;
	 */
	protected CNetNode endNode = null;

	public CNet(String label) {
		this.label = label;
		this.nodes = new HashSet<CNetNode>();
		this.inputBindings = new HashMap<CNetNode, Set<CNetBinding>>();
		this.outputBindings = new HashMap<CNetNode, Set<CNetBinding>>();
	}

	/**
	 * check if this cnet is consistent with the formal definitions:
	 * 
	 * - one start node
	 * 
	 * - one end node
	 * 
	 * - dependencies between nodes are reflected in input/output bindings
	 * 
	 * * @return
	 */
	public boolean isConsistent() {
		if (endNode == null) {
			return false;
		} else if (!getOutputBindings(endNode).isEmpty()) {
			return false;
		}
		if (startNode == null) {
			return false;
		} else if (!getInputBindings(startNode).isEmpty()) {
			return false;
		}
		for (CNetNode node : nodes) {
			if (getInputBindings(node).isEmpty() && !node.equals(startNode)) {
				return false;
			}
			if (getOutputBindings(node).isEmpty() && !node.equals(endNode)) {
				return false;
			}
			for (CNetBinding binding : getInputBindings(node)) {
				nn: for (CNetNode n2 : binding.getBoundNodes()) {
					for (CNetBinding b2 : getOutputBindings(n2)) {
						if (b2.getBoundNodes().contains(node)) {
							continue nn;
						}
					}
					// No binding found 
					return false;
				}
			}
			for (CNetBinding binding : getOutputBindings(node)) {
				nn: for (CNetNode n2 : binding.getBoundNodes()) {
					for (CNetBinding b2 : getInputBindings(n2)) {
						if (b2.getBoundNodes().contains(node)) {
							continue nn;
						}
					}
					// No binding found 
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the nodes
	 */
	public Set<CNetNode> getNodes() {
		return ImmutableSet.copyOf(nodes);
	}

	/**
	 * @return the startNode
	 */
	public CNetNode getStartNode() {
		return startNode;
	}

	/**
	 * @param startNode
	 *            the startNode to set
	 */
	public void setStartNode(CNetNode startNode) {
		assert (nodes.contains(startNode));
		this.startNode = startNode;
	}

	/**
	 * @return the endNode
	 */
	public CNetNode getEndNode() {
		return endNode;
	}

	/**
	 * @param endNode
	 *            the endNode to set
	 */
	public void setEndNode(CNetNode endNode) {
		this.endNode = endNode;
	}

	/**
	 * Add a node
	 * 
	 * @param node
	 */
	public CNetNode addNode(CNetNode node) {
		this.nodes.add(node);
		if (!inputBindings.containsKey(node)) {
			inputBindings.put(node, new HashSet<CNetBinding>());
		}
		if (!outputBindings.containsKey(node)) {
			outputBindings.put(node, new HashSet<CNetBinding>());
		}
		return node;
	}

	/**
	 * Remove a node
	 * 
	 * @param node
	 */
	public void removeNode(CNetNode node) {
		this.nodes.remove(node);
		inputBindings.remove(node);
		outputBindings.remove(node);
		if (node.equals(startNode)) {
			startNode = null;
		}
		if (node.equals(endNode)) {
			endNode = null;
		}
	}

	/**
	 * Add an input binding
	 * 
	 * @param node
	 */
	public CNetBinding addInputBinding(CNetNode node, CNetNode... nodes) {
		CNetBinding binding = new CNetBinding(Type.INPUT, node, nodes);
		inputBindings.get(node).add(binding);
		return binding;
	}

	/**
	 * Add an output binding
	 * 
	 * @param node
	 */
	public CNetBinding addOutputBinding(CNetNode node, CNetNode... nodes) {
		CNetBinding binding = new CNetBinding(Type.OUTPUT, node, nodes);
		outputBindings.get(node).add(binding);
		return binding;
	}

	/**
	 * Add an input binding
	 * 
	 * @param node
	 */
	public CNetBinding addInputBinding(CNetNode node, Collection<? extends CNetNode> nodes) {
		CNetBinding binding = new CNetBinding(Type.INPUT, node, nodes);
		inputBindings.get(node).add(binding);
		return binding;
	}

	/**
	 * Add an output binding
	 * 
	 * @param node
	 */
	public CNetBinding addOutputBinding(CNetNode node, Collection<? extends CNetNode> nodes) {
		CNetBinding binding = new CNetBinding(Type.OUTPUT, node, nodes);
		outputBindings.get(node).add(binding);
		return binding;
	}

	/**
	 * removes a binding
	 * 
	 * @param binding
	 */
	public void removeBinding(CNetBinding binding) {
		switch (binding.getType()) {
			case INPUT :
				inputBindings.get(binding.getNode()).remove(binding);
				break;
			case OUTPUT :
				outputBindings.get(binding.getNode()).remove(binding);
				break;
		}
	}

	/**
	 * Returns the input bindings of the given node
	 * 
	 * @param node
	 * @return a (possibly empty) set
	 */
	public Set<CNetBinding> getInputBindings(CNetNode node) {
		Set<CNetBinding> bindings = inputBindings.get(node);
		if (bindings != null) {
			return ImmutableSet.copyOf(bindings);	
		} else {
			return ImmutableSet.of();
		}		
	}

	/**
	 * Returns the output bindings of the given node
	 * 
	 * @param node
	 * @return a (possibly empty) set
	 */
	public Set<CNetBinding> getOutputBindings(CNetNode node) {
		Set<CNetBinding> bindings = outputBindings.get(node);
		if (bindings != null) {
			return ImmutableSet.copyOf(bindings);	
		} else {
			return ImmutableSet.of();
		}
	}

	/**
	 * Returns the bindings of the given node
	 * 
	 * @param node
	 * @return a (possibly empty) set
	 */
	public Set<CNetBinding> getBindings(CNetNode node) {
		Set<CNetBinding> set = new HashSet<CNetBinding>();
		set.addAll(getInputBindings(node));
		set.addAll(getOutputBindings(node));
		return ImmutableSet.copyOf(set);
	}

	/**
	 * Returns the input bindings of this net
	 * 
	 * @param node
	 * @return a (possibly empty) set
	 */
	public Set<CNetBinding> getInputBindings() {
		Set<CNetBinding> set = new HashSet<CNetBinding>();
		for (CNetNode node : nodes) {
			set.addAll(inputBindings.get(node));
		}
		return ImmutableSet.copyOf(set);
	}

	/**
	 * Returns the output bindings of this net
	 * 
	 * @param node
	 * @return a (possibly empty) set
	 */
	public Set<CNetBinding> getOutputBindings() {
		Set<CNetBinding> set = new HashSet<CNetBinding>();
		for (CNetNode node : nodes) {
			set.addAll(outputBindings.get(node));
		}
		return ImmutableSet.copyOf(set);
	}

	/**
	 * Returns the bindings of this net
	 * 
	 * @param node
	 * @return a (possibly empty) set
	 */
	public Set<CNetBinding> getBindings() {
		Set<CNetBinding> set = new HashSet<CNetBinding>();
		for (CNetNode node : nodes) {
			set.addAll(inputBindings.get(node));
			set.addAll(outputBindings.get(node));
		}
		return ImmutableSet.copyOf(set);
	}

	/**
	 * Returns the successors of a CNet node, i.e. each of these nodes is in an
	 * output binding of the given node. If the net is consistent, then the
	 * given node is also in at least one input binding of each node in the
	 * returned set.
	 * 
	 * @param node
	 * @return
	 */
	public Set<CNetNode> getSuccessors(CNetNode node) {
		Set<CNetNode> set = new HashSet<CNetNode>();
		for (CNetBinding binding : outputBindings.get(node)) {
			set.addAll(binding.getBoundNodes());
		}
		return ImmutableSet.copyOf(set);
	}

	/**
	 * Returns the predecessors of a CNet node, i.e. each of these nodes is in
	 * an input binding of the given node. If the net is consistent, then the
	 * given node is also in at least one output binding of each node in the
	 * returned set.
	 * 
	 * @param node
	 * @return
	 */
	public Set<CNetNode> getPredecessors(CNetNode node) {
		Set<CNetNode> set = new HashSet<CNetNode>();
		for (CNetBinding binding : inputBindings.get(node)) {
			set.addAll(binding.getBoundNodes());
		}
		return ImmutableSet.copyOf(set);
	}
	
	/**
	 * Print the current CNet
	 */
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for (Entry<CNetNode, Set<CNetBinding>> entry : inputBindings.entrySet()){
			sb.append("--- INPUT BINDING --- ");
			sb.append('\n');
			sb.append("Node ");
			sb.append(entry.getKey().getLabel());
			sb.append('\n');
			sb.append("Input: ");
			for (CNetBinding binding : entry.getValue()){
				sb.append("{");
				for (CNetNode node : binding.getBoundNodes()){
					sb.append(node.getLabel());
					sb.append(',');
				}
				sb.append("}");
				sb.append("\n       ");
			}
		}
		for (Entry<CNetNode, Set<CNetBinding>> entry : outputBindings.entrySet()){
			sb.append("--- OUTPUT BINDING --- ");
			sb.append('\n');
			sb.append("Node ");
			sb.append(entry.getKey().getLabel());
			sb.append('\n');
			sb.append("Output: ");
			for (CNetBinding binding : entry.getValue()){
				sb.append("{");
				for (CNetNode node : binding.getBoundNodes()){
					sb.append(node.getLabel());
					sb.append(',');
				}
				sb.append("}");
				sb.append("\n       ");
			}
		}
		return sb.toString();
	}
}
