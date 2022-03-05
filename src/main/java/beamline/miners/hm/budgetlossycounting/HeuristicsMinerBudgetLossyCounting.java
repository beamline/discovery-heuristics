package beamline.miners.hm.budgetlossycounting;

import java.util.HashMap;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.cnet.CNet;

import beamline.miners.hm.CNetGenerator;
import beamline.miners.hm.budgetlossycounting.models.DBudgetActivities;
import beamline.miners.hm.budgetlossycounting.models.DBudgetCases;
import beamline.miners.hm.budgetlossycounting.models.DBudgetRelations;
import beamline.miners.hm.budgetlossycounting.models.SharedDelta;
import beamline.models.algorithms.StreamMiningAlgorithm;

/**
 * A Budget Lossy Counting version of the Heuristics Miner
 * 
 * @author Andrea Burattin
 */
public class HeuristicsMinerBudgetLossyCounting extends StreamMiningAlgorithm<XTrace, CNet> {

	private HashMap<String, Integer> startingActivities;
	private HashMap<String, Integer> finishingActivities;
//	private Collection<Pair<String, String>> logStartsEnd;
//	private ArrayList<String> ends;
	private DBudgetActivities activities;
	private DBudgetRelations relations;
	private DBudgetCases cases;
	
	private CNet modelCache = null;
	private String latestActivity = null;
	
	private double dependencyThreshold = 0.9;
	private double andThreshold = 0.1;
	private double positiveObservationThreshold = 10.0;
	
	public HeuristicsMinerBudgetLossyCounting(int budget, double dependencyThreshold, double positiveObservationThreshold, double andThreshold) {
		this.dependencyThreshold = dependencyThreshold;
		this.positiveObservationThreshold = positiveObservationThreshold;
		this.andThreshold = andThreshold;

		startingActivities = new HashMap<String, Integer>();
		finishingActivities = new HashMap<String, Integer>();
		
//		ends = new ArrayList<String>();
//		for (Pair<String, String> p : logStartsEnd) {
//			ends.add(p.getSecond());
//		}
		
		SharedDelta delta = new SharedDelta();
		delta.budget = (int) budget;
		activities = new DBudgetActivities(delta);
		relations = new DBudgetRelations(delta);
		cases = new DBudgetCases(delta, startingActivities, finishingActivities);
	}
	
	@Override
	public CNet ingest(XTrace trace) {
		
		XEvent event = trace.get(0);
		String activityName = XConceptExtension.instance().extractName(event);
		String caseId = XConceptExtension.instance().extractName(trace);
		
		// update data structures
		activities.addObservation(activityName);
		latestActivity = cases.addObservation(caseId, activityName);
		if (latestActivity != null) {
			relations.addObservation(latestActivity, activityName);
		}
		
		// this activity is a termination activity
//		if (!ends.contains(activityName)) {
//			ends.add(activityName);
//		}
//		if (ends.contains(activityName)) {
//			cases.lastActivityObserved(activityName);
//		}
		
		return modelCache;
	}

	public CNet updateModel() {
		CNetGenerator generator = new CNetGenerator(
				activities.getActivities(),
				relations.getRelations(),
				startingActivities,
				cases.getFinishingActivities());
		modelCache = generator.generateModel(dependencyThreshold, positiveObservationThreshold, andThreshold);
		return modelCache;
	}
}
