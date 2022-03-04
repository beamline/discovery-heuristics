package org.processmining.models.cnet;

import java.util.Set;

/**
 * A causal net with a unique {@link #getStartNode()}, a unique
 * {@link #getEndNode()} and efficient means to query the structure of the net.
 * <p>
 * To create a {@link CausalNet} use the {@link CNet} implementation. The
 * interface deliberately does not specify mutating methods to be kept simple.
 * 
 * @author F. Mannhardt
 *
 */
public interface CausalNet {

	public final class Factory {

		private Factory() {
		}

		public static CNet toCNet(CausalNet causalNet) {

			CNet cnet = new CNet(causalNet.getLabel());
			for (CNetNode node : causalNet.getNodes()) {
				cnet.addNode(node);
				for (CNetBinding binding : causalNet.getOutputBindings(node)) {
					cnet.addOutputBinding(node, binding.getBoundNodes());
				}
				for (CNetBinding binding : causalNet.getInputBindings(node)) {
					cnet.addInputBinding(node, binding.getBoundNodes());
				}
			}
			cnet.setStartNode(causalNet.getStartNode());
			cnet.setEndNode(causalNet.getEndNode());			
			return cnet;

		}

	}

	String getLabel();

	CNetNode getStartNode();

	CNetNode getEndNode();

	Set<CNetNode> getNodes();

	Set<CNetNode> getSuccessors(CNetNode node);

	Set<CNetNode> getPredecessors(CNetNode node);

	Set<CNetBinding> getOutputBindings(CNetNode node);

	Set<CNetBinding> getInputBindings(CNetNode node);

}