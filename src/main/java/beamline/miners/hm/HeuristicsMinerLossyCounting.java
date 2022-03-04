package beamline.miners.hm;

import java.util.HashMap;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.cnet.CNet;

import beamline.miners.hm.lossycounting.DActivities;
import beamline.miners.hm.lossycounting.DCases;
import beamline.miners.hm.lossycounting.DRelations;
import beamline.models.algorithms.StreamMiningAlgorithm;

public class HeuristicsMinerLossyCounting extends StreamMiningAlgorithm<XTrace, CNet> {

	private HashMap<String, Integer> startingActivities;
	private HashMap<String, Integer> finishingActivities;
	private DActivities activities;
	private DRelations relations;
	private DCases cases;
	private int bucketWidth = -1;
	private int currentBucket = -1;
	private CNet modelCache = null;
	private String latestActivity = null;
	
	private double dependencyThreshold = 0.9;
	private double andThreshold = 0.1;
	private double positiveObservationThreshold = 10.0;
	
	public HeuristicsMinerLossyCounting() {
		this(Double.MIN_VALUE);
	}
	
	public HeuristicsMinerLossyCounting(double maxApproximationError) {
		this(maxApproximationError, 0.9, 10, 0.1);
	}
	
	public HeuristicsMinerLossyCounting(double maxApproximationError, double dependencyThreshold, double positiveObservationThreshold, double andThreshold) {
		this.dependencyThreshold = dependencyThreshold;
		this.positiveObservationThreshold = positiveObservationThreshold;
		this.andThreshold = andThreshold;
		
		this.startingActivities = new HashMap<String, Integer>();
		this.finishingActivities = new HashMap<String, Integer>();
		this.activities = new DActivities();
		this.relations = new DRelations();
		this.cases = new DCases(startingActivities, finishingActivities);
		
		this.bucketWidth = (int)(1.0 / maxApproximationError);
	}
	
	@Override
	public CNet ingest(XTrace trace) {
		currentBucket = (int)(getProcessedEvents() / bucketWidth);
		XEvent event = trace.get(0);
		String activityName = XConceptExtension.instance().extractName(event);
		String caseId = XConceptExtension.instance().extractName(trace);
		
		// update data structures
		activities.addActivityObservation(activityName, currentBucket);
		latestActivity = cases.addCaseObservation(caseId, activityName, currentBucket);
		if (latestActivity != null) {
			relations.addRelationObservation(latestActivity, activityName, currentBucket);
		}
		
		// cleanup when required
		if (getProcessedEvents() % bucketWidth == 0) {
			activities.cleanup(currentBucket);
			cases.cleanup(currentBucket);
			relations.cleanup(currentBucket);
		}
		
		return modelCache;
	}

	public CNet updateModel() {
		CNetGenerator generator = new CNetGenerator(
				activities.getActivities(),
				relations.getRelations(),
				startingActivities.keySet(),
				cases.getFinishingActivities());
		modelCache = generator.generateModel(dependencyThreshold, positiveObservationThreshold, andThreshold);
		return modelCache;
	}
}
