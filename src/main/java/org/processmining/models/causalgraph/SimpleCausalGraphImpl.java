package org.processmining.models.causalgraph;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSetMultimap.Builder;
import com.google.common.collect.SetMultimap;

/**
 * Basic immutable implementation of a {@link SimpleCausalGraph}.
 * 
 * @author F. Mannhardt
 *
 */
public class SimpleCausalGraphImpl implements XEventClassifierAwareSimpleCausalGraph {

	public XEventClassifier getEventClassifier() {
		return classifier;
	}

	private final SetMultimap<Integer, XEventClass> outputTasks;
	private final SetMultimap<Integer, XEventClass> inputTasks;
	private final Set<Relation> causalRelations;
	private final Set<XEventClass> activities;
	private final XEventClassifier classifier;

	/**
	 * Copy constructor
	 * 
	 * @param graph
	 */
	public SimpleCausalGraphImpl(SimpleCausalGraph graph) {
		this(graph.getSetActivities(), graph.getCausalRelations());
	}

	public SimpleCausalGraphImpl(Set<XEventClass> activities, Set<Relation> causalRelations) {
		this(null, activities, causalRelations);
	}

	public SimpleCausalGraphImpl(final XEventClassifier classifier, final Set<XEventClass> activities,
			final Set<Relation> causalRelations) {
		this.classifier = classifier;
		this.activities = ImmutableSet.copyOf(activities);
		this.causalRelations = ImmutableSet.copyOf(causalRelations);
		Builder<Integer, XEventClass> outputBuilder = ImmutableSetMultimap.<Integer, XEventClass>builder();
		Builder<Integer, XEventClass> inputBuilder = ImmutableSetMultimap.<Integer, XEventClass>builder();
		for (Relation baseRelation : causalRelations) {
			outputBuilder.put(baseRelation.getSource().getIndex(), baseRelation.getTarget());
			inputBuilder.put(baseRelation.getTarget().getIndex(), baseRelation.getSource());
		}
		outputTasks = outputBuilder.build();
		inputTasks = inputBuilder.build();

	}

	public Set<XEventClass> getPostset(XEventClass task) {
		return outputTasks.get(task.getIndex());
	}

	public Set<XEventClass> getPreset(XEventClass task) {
		return inputTasks.get(task.getIndex());
	}

	public Set<Relation> getCausalRelations() {
		return causalRelations;
	}

	public Set<XEventClass> getSetActivities() {
		return activities;
	}

	public String toString() {
		return "{" + Joiner.on(',').join(causalRelations) + "}";
	}

}