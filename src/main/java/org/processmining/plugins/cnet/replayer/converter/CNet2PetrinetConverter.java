package org.processmining.plugins.cnet.replayer.converter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.cnet.CNet;
import org.processmining.models.cnet.CNetBinding;
import org.processmining.models.cnet.CNetNode;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;

@Plugin(name = "Convert CNet to Petrinet (BVD)", returnLabels = { "Petrinet", "Marking" }, returnTypes = {
		Petrinet.class, Marking.class }, parameterLabels = { "CNet" }, userAccessible = false)
public class CNet2PetrinetConverter {

	protected Map<CNetNode, Place> node2places;
	protected Map<CNetBinding, Transition> binding2trans;
	protected Map<Transition, CNetBinding> trans2binding;
	protected Marking initialMarking;
	protected Marking finalMarking;
	protected Petrinet net;
	protected Transition finalTransition;
	protected Map<Pair<CNetNode, CNetNode>, Place> edges2place;
	protected Map<Place, Pair<CNetNode, CNetNode>> place2edges;

	public CNet2PetrinetConverter() {

	}

	public Map<CNetNode, Place> getNode2Places() {
		return node2places;
	}

	public Map<CNetBinding, Transition> getBinding2Transition() {
		return binding2trans;
	}

	public Map<Transition, CNetBinding> getTransition2Binding() {
		return trans2binding;
	}

	public Transition getFinalTransition() {
		return finalTransition;
	}

	public Marking getInitialMarking() {
		return initialMarking;
	}

	public Marking getFinalMarking() {
		return finalMarking;
	}

	public Petrinet getPetrinet() {
		return net;
	}

	public Map<Pair<CNetNode, CNetNode>, Place> getEdges2Places() {
		return edges2place;
	}

	public Map<Place, Pair<CNetNode, CNetNode>> getPlaces2Edges() {
		return place2edges;
	}

	public void convert(CNet cnet, boolean reduceNet) {

		assert cnet.isConsistent();

		node2places = new HashMap<CNetNode, Place>();
		binding2trans = new HashMap<CNetBinding, Transition>();
		trans2binding = new HashMap<Transition, CNetBinding>();

		net = new PetrinetImpl(cnet.getLabel());
		int i = 0;

		for (CNetNode node : cnet.getNodes()) {
			Place p = net.addPlace("p " + node.getLabel());
			node2places.put(node, p);
		}

		edges2place = new HashMap<Pair<CNetNode, CNetNode>, Place>();
		place2edges = new HashMap<Place, Pair<CNetNode, CNetNode>>();
		for (CNetBinding b : cnet.getInputBindings()) {
			Transition t = net.addTransition(b.getType().toString() + " " + b.getNode().getLabel());
			Place pin = node2places.get(b.getNode());
			t.setInvisible(true);
			net.addArc(t, pin);

			binding2trans.put(b, t);
			trans2binding.put(t, b);

			for (CNetNode node : b.getBoundNodes()) {
				Pair<CNetNode, CNetNode> edge = new Pair<CNetNode, CNetNode>(node, b.getNode());
				Place p;
				if (edges2place.containsKey(edge)) {
					p = edges2place.get(edge);
				} else {
					p = net.addPlace("p" + i++);
					edges2place.put(edge, p);
					place2edges.put(p, edge);
				}
				net.addArc(p, t);
			}
		}
		for (CNetBinding b : cnet.getOutputBindings()) {
			Transition t = net.addTransition(b.getNode().getLabel());
			Place pout = node2places.get(b.getNode());
			t.setInvisible(false);
			net.addArc(pout, t);

			binding2trans.put(b, t);
			trans2binding.put(t, b);

			for (CNetNode node : b.getBoundNodes()) {
				Pair<CNetNode, CNetNode> edge = new Pair<CNetNode, CNetNode>(b.getNode(), node);
				Place p;
				if (edges2place.containsKey(edge)) {
					p = edges2place.get(edge);
				} else {
					p = net.addPlace("p" + i++);
					edges2place.put(edge, p);
					place2edges.put(p, edge);
				}
				net.addArc(t, p);
			}
		}
		finalTransition = net.addTransition(cnet.getEndNode().getLabel());
		net.addArc(node2places.get(cnet.getEndNode()), finalTransition);
		Place endPlace = net.addPlace("endPlace");
		net.addArc(finalTransition, endPlace);

		finalMarking = new Marking();
		finalMarking.add(endPlace);

		initialMarking = new Marking();
		initialMarking.add(node2places.get(cnet.getStartNode()));

		if (reduceNet) {
			Set<Transition> toRemove = new HashSet<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.isInvisible()) {
					Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> in = net.getInEdges(t);
					Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> out = net.getOutEdges(t);
					if (in.size() == 1 && out.size() == 1) {
						// remove this invisible transition.
						Place pi = (Place) in.iterator().next().getSource();
						Place po = (Place) out.iterator().next().getTarget();
						if (pi != po) {
							for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getInEdges(pi)) {
								net.addArc((Transition) e.getSource(), po);
							}
							for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getOutEdges(pi)) {
								net.addArc(po, (Transition) e.getTarget());
							}
							net.removePlace(pi);
						}
						toRemove.add(t);

					}
				}
			}
			for (Transition t : toRemove) {
				net.removeTransition(t);
			}
		}

	}

}
