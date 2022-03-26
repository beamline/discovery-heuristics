package beamline.miners.hm.lossycounting;

import java.util.HashMap;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.cnet.CNet;

import beamline.events.BEvent;
import beamline.miners.hm.CNetGenerator;
import beamline.miners.hm.budgetlossycounting.HeuristicsMinerBudgetLossyCounting;
import beamline.miners.hm.budgetlossycounting.models.StreamingCNet;
import beamline.miners.hm.lossycounting.models.DActivities;
import beamline.miners.hm.lossycounting.models.DCases;
import beamline.miners.hm.lossycounting.models.DRelations;
import beamline.models.algorithms.StreamMiningAlgorithm;

public class HeuristicsMinerLossyCounting extends StreamMiningAlgorithm<StreamingCNet> {

	private static final long serialVersionUID = -6998030391882756554L;
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
	
	private int modelRefreshRate = 10;
	
	public HeuristicsMinerLossyCounting() {
		this(0.0001);
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
	public StreamingCNet ingest(BEvent event) {
		currentBucket = (int)(getProcessedEvents() / bucketWidth);
		String activityName = event.getEventName();
		String caseId = event.getTraceName();
		
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
		
		if (getProcessedEvents() % modelRefreshRate == 0) {
			return updateModel();
		}
		return null;
	}

	public StreamingCNet updateModel() {
		CNetGenerator generator = new CNetGenerator(
				activities.getActivities(),
				relations.getRelations(),
				startingActivities,
				cases.getFinishingActivities());
		modelCache = generator.generateModel(dependencyThreshold, positiveObservationThreshold, andThreshold);
		return new StreamingCNet(modelCache);
	}

	public HeuristicsMinerLossyCounting setModelRefreshRate(int modelRefreshRate) {
		this.modelRefreshRate = modelRefreshRate;
		return this;
	}
}
