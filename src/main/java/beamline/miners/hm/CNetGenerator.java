package beamline.miners.hm;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.models.cnet.CNet;
import org.processmining.models.cnet.CNetBinding;
import org.processmining.models.cnet.CNetNode;

import beamline.miners.hm.utils.BasicPluginConfiguration;
import beamline.miners.hm.utils.CNetHelper;

/**
 * 
 * @author Andrea Burattin
 */
public class CNetGenerator {

	private HashMap<String, Double> activities;
	private HashMap<Pair<String, String>, Double> relations;
	
	private HashMap<String, Double> activityFrequency;
	private Set<String> startEvents;
	private Set<String> endEvents;
	
	private Map<String, Integer> startingActivities;
	private Map<String, Integer> finishingActivities;
	
	private Double relativeToBestThreshold = 0.05;
	
	private HashMap<String, CNetNode> nodes;
	private HashMap<String, Pair<Double, String>> bestInput;
	private HashMap<String, Pair<Double, String>> bestOutput;
	
	/**
	 * 
	 * @param dActivities
	 * @param dRelations
	 * @param finishingActivities 
	 * @param startingActivities 
	 */
	public CNetGenerator(HashMap<String, Double> dActivities, HashMap<Pair<String, String>, Double> dRelations, 
			Map<String, Integer> startingActivities, Map<String, Integer> finishingActivities) {
		this.activities = dActivities;
		this.relations = dRelations;
		
		this.startingActivities = startingActivities;
		this.finishingActivities = finishingActivities;
	}
	
	/**
	 * 
	 * @param dependencyThreshold
	 * @param positiveObservations
	 * @param andThreshold
	 * @return
	 */
	public CNet generateModel(Double dependencyThreshold, Double positiveObservations, Double andThreshold) {
		
		populateFields();
		updateStartsEnds(startingActivities, finishingActivities, positiveObservations);

		CNet model = new CNet("mined model");
		nodes = new HashMap<String, CNetNode>();
		
		// add all the nodes
		for (String event : activityFrequency.keySet()) {
//			Double observations = activityFrequency.get(event);
			// check that the activity is still alive
			CNetNode n = new CNetNode(event);
			model.addNode(n);
			nodes.put(event, n);
		}
		
		HashMap<String, HashSet<Pair<String, String>>> andSplitRelations = getAndSplits(dependencyThreshold, positiveObservations, andThreshold);
		HashMap<String, HashSet<Pair<String, String>>> andJoinRelations = getAndJoins(dependencyThreshold, positiveObservations, andThreshold);
		
		// add all the and splits
		for (String split : andSplitRelations.keySet()) {
			HashSet<Pair<String, String>> branches = andSplitRelations.get(split);
			for(Pair<String, String> b : branches) {
				HashSet<CNetNode> brancheNodes = new HashSet<CNetNode>();
				brancheNodes.add(nodes.get(b.getLeft()));
				brancheNodes.add(nodes.get(b.getRight()));
				CNetNode[] dests = new CNetNode[brancheNodes.size()];
				brancheNodes.toArray(dests);
				CNetHelper.addAndSplit(model, nodes.get(split), dests);
			}
		}
		
		// add all the and joins
		for (String join : andJoinRelations.keySet()) {
			HashSet<Pair<String, String>> branches = andJoinRelations.get(join);
			for(Pair<String, String> b : branches) {
				HashSet<CNetNode> brancheNodes = new HashSet<CNetNode>();
				brancheNodes.add(nodes.get(b.getLeft()));
				brancheNodes.add(nodes.get(b.getRight()));
				CNetNode[] sources = new CNetNode[brancheNodes.size()];
				brancheNodes.toArray(sources);
				CNetHelper.addAndJoin(model, nodes.get(join), sources);
			}
		}
		
		// add the other edges
		for (Pair<String, String> edge : this.relations.keySet()) {
			if (allowedEdge(edge.getLeft(), edge.getRight(), dependencyThreshold, positiveObservations) /*&&
					!edge.getLeft().equals(edge.getRight())*/) {
				
				CNetNode A = nodes.get(edge.getLeft());
				CNetNode B = nodes.get(edge.getRight());
				
				if (A != null && B != null) {
					if (andSplitRelations.containsKey(A.getLabel()) &&
						inOnePair(andSplitRelations.get(A.getLabel()), B.getLabel())) {
						// this is already part of and-split
					} else if (andJoinRelations.containsKey(B.getLabel()) &&
							inOnePair(andJoinRelations.get(B.getLabel()), A.getLabel())) {
						// this is already part of and-join
					} else if (!inAndRelations(A, B, andSplitRelations.values())) { //&& allowedDependency(edge, dependencyThreshold, activityThreshold)) {
						// we add the edge A -> B there actually is this dependency
						CNetHelper.addConnection(model, A, B);
					}
				}
			}
		}
		
		// add the artificial start and end node to the model
		CNetNode start = new CNetNode(BasicPluginConfiguration.ARTIFICIAL_START_NAME);
		model.addNode(start);
		CNetNode end = new CNetNode(BasicPluginConfiguration.ARTIFICIAL_END_NAME);
		model.addNode(end);
		
		model.setStartNode(start);
		model.setEndNode(end);
		
		// add artificial start and end connections
		String nodeWithLessOutputs = "";
		int counterNodeWithLessOutput = 10000;
		for (CNetNode n : model.getNodes()) {
			if (!n.getLabel().equals(BasicPluginConfiguration.ARTIFICIAL_START_NAME) &&
					!n.getLabel().equals(BasicPluginConfiguration.ARTIFICIAL_END_NAME)) {
				// no input ----------------------------------------------------
				if (model.getInputBindings(n).isEmpty() || startEvents.contains(n.getLabel())) {
					// no input event, it's a start
					CNetHelper.addConnection(model, start, n);
				}
				// no output ---------------------------------------------------
				if (model.getOutputBindings(n).isEmpty() || endEvents.contains(n.getLabel())) {
					// no output, it's an end
					CNetHelper.addConnection(model, n, end);
				}
				if (model.getOutputBindings(n).size() == 1) {
					CNetBinding binding = model.getOutputBindings(n).iterator().next();
					if (binding.getBoundNodes().contains(n)) {
						// the only input event is a self loop
						CNetHelper.addConnection(model, n, end);
					}
				}
				// update the model with less output
				int currentNumberOutputs = model.getOutputBindings(n).size();
				if (currentNumberOutputs < counterNodeWithLessOutput) {
					counterNodeWithLessOutput = currentNumberOutputs;
					nodeWithLessOutputs = n.getLabel();
				}
			}
		}
		// no output set, add an artificial one
		if (model.getInputBindings(end).size() == 0) {
			CNetHelper.addConnection(model, nodes.get(nodeWithLessOutputs), end);
		}
		
		return model;
	}
	
	private void populateFields() {
		this.activityFrequency = new HashMap<String, Double>();
		this.bestInput = new HashMap<String, Pair<Double, String>>();
		this.bestOutput = new HashMap<String, Pair<Double, String>>();
		
		Set<String> activities = this.activities.keySet();
		for (String a : activities) {
			activityFrequency.put(a, this.activities.get(a));
		}
		
		Set<Pair<String, String>> relations = this.relations.keySet();
		for (Pair<String, String> r : relations) {
			Double value = calculateDependencyMeasure(r.getLeft(), r.getRight());
			
			if (!r.getLeft().equals(r.getRight())) {
				
				// update best input
				if (bestInput.containsKey(r.getRight())) {
					Double currentBest = bestInput.get(r.getRight()).getLeft();
					if (currentBest < value) {
						bestInput.put(r.getRight(), Pair.of(value, r.getLeft()));
					}
				} else {
					bestInput.put(r.getRight(), Pair.of(value, r.getLeft()));
				}
			
				// update best output
				if (bestOutput.containsKey(r.getLeft())) {
					Double currentBest = bestOutput.get(r.getLeft()).getLeft();
					if (currentBest < value) {
						bestOutput.put(r.getLeft(), Pair.of(value, r.getRight()));
					}
				} else {
					bestOutput.put(r.getLeft(), Pair.of(value, r.getRight()));
				}
			}
		}
	}
	
	private HashMap<String, HashSet<Pair<String, String>>> getAndSplits(Double dependencyThreshold, Double positiveObservationsThreshold, Double andThreshold) {
		HashMap<String, HashSet<Pair<String, String>>> andSplitRelations = new HashMap<String, HashSet<Pair<String, String>>>();
		for (String split : activityFrequency.keySet()) {
			for (String branch1 : activityFrequency.keySet()) {

				if (!split.equals(branch1) &&
					allowedEdge(split, branch1, dependencyThreshold, positiveObservationsThreshold)) {
					for (String branch2 : activityFrequency.keySet()) {
						
						if (!split.equals(branch2) && !branch1.equals(branch2) &&
							allowedEdge(split, branch2, dependencyThreshold, positiveObservationsThreshold)) {
							
							Double num = (getRelationsCount(branch1, branch2) + getRelationsCount(branch2, branch1));
							Double den = (getRelationsCount(split, branch1) + getRelationsCount(split, branch2) + 1);
							Double andMeasure = num / den;
							
							if (andMeasure >= andThreshold) {
								// the two are actually in and
								HashSet<Pair<String, String>> ands = andSplitRelations.get(split);
								if (ands == null) {
									ands = new HashSet<Pair<String, String>>();
								}
								if (!pairContained(ands, branch1, branch2)) {
									Pair<String, String> p = Pair.of(branch1, branch2);
									ands.add(p);
									andSplitRelations.put(split, ands);
								}
							}
						}
					}
				}
			}
		}
		return andSplitRelations;
	}
	
	private HashMap<String, HashSet<Pair<String, String>>> getAndJoins(Double dependencyThreshold, Double positiveObservationsThreshold, Double andThreshold) {
		HashMap<String, HashSet<Pair<String, String>>> andJoinRelations = new HashMap<String, HashSet<Pair<String, String>>>();
		for (String join : activityFrequency.keySet()) {
			for (String branch1 : activityFrequency.keySet()) {

				if (!join.equals(branch1) &&
					allowedEdge(branch1, join, dependencyThreshold, positiveObservationsThreshold)) {
					for (String branch2 : activityFrequency.keySet()) {
						
						if (!join.equals(branch1) && !branch1.equals(branch2) &&
							allowedEdge(branch2, join, dependencyThreshold, positiveObservationsThreshold)) { 
							
							Double num = (getRelationsCount(branch1, branch2) + getRelationsCount(branch2, branch1));
							Double den = (getRelationsCount(branch1, join) + getRelationsCount(branch2, join) + 1);
							Double andMeasure = num / den;
							
							if (andMeasure >= andThreshold) {
							
								// the two are actually in and
								HashSet<Pair<String, String>> ands = andJoinRelations.get(join);
								if (ands == null) {
									ands = new HashSet<Pair<String, String>>();
								}
								if (!pairContained(ands, branch1, branch2)) {
									Pair<String, String> p = Pair.of(branch1, branch2);
									ands.add(p);
									andJoinRelations.put(join, ands);
								}
							}
						}
					}
				}
			}
		}
		return andJoinRelations;
	}
	
	private Boolean inAndRelations(CNetNode A, CNetNode B, Collection<? extends Set<Pair<String, String>>> andSplitRelations) {
		for (Set<Pair<String, String>> branchesSet : andSplitRelations) {
			if (pairContained(branchesSet, A.getLabel(), B.getLabel())) {
				return true;
			}
		}
		return false;
	}
	
	private Boolean pairContained(Set<Pair<String, String>> collection, String A, String B) {
		Pair<String, String> f = Pair.of(A, B);
		Pair<String, String> s = Pair.of(B, A);
		if (collection.contains(f) || collection.contains(s)) {
			return true;
		}
		return false;
	}
	
	private Boolean inOnePair(Set<Pair<String, String>> collection, String A) {
		for (Pair<String, String> p : collection) {
			if (p.getLeft().equals(A) || p.getRight().equals(A)) {
				return true;
			}
		}
		return false;
	}
	
	private Double getRelationsCount(String A, String B) {
		Double AB = relations.get(Pair.of(A, B));
		Double countAB = (AB == null)? 0 : AB;
		return countAB;
	}
	
	public void updateStartsEnds(Map<String, Integer> startObservations, Map<String, Integer> endObservations, double activityThreshold) {
		this.startEvents = new HashSet<String>(1);
		this.endEvents = new HashSet<String>(1);
		for (String k : startObservations.keySet()) {
			if (startObservations.get(k) > activityThreshold && activities.containsKey(k) && activityFrequency.get(k) > activityThreshold) {
				startEvents.add(k);
			}
		}
		for (String k : endObservations.keySet()) {
			if (endObservations.get(k) > activityThreshold && activities.containsKey(k) && activityFrequency.get(k) > activityThreshold) {
				endEvents.add(k);
			}
		}
	}
	
	private Boolean allowedEdge(String source, String destination, Double dependencyThreshold, Double positiveObservations) {		
		Double edgeMeasure = calculateDependencyMeasure(source, destination);
		return (edgeMeasure >= dependencyThreshold &&
				(bestOutput.get(source).getLeft() - edgeMeasure <= relativeToBestThreshold) &&
				relations.get(Pair.of(source, destination)) >= positiveObservations);
	}
	
	private Double calculateDependencyMeasure(String A, String B) {
		Double AB = relations.get(Pair.of(A, B));
		Double BA = relations.get(Pair.of(B, A));
		
		Double countAB = (AB == null)? 0 : AB;
		Double countBA = (BA == null)? 0 : BA;
		
		Double measure = 0.0;
		measure = (countAB - countBA) / (countAB + countBA + 1);
		return measure;
	}
}
