package org.processmining.models.causalgraph;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

/**
 * Causal graph defined through the set of {@link Relation} between the
 * activities. Activities are represented through {@link XEventClass}. It also
 * defined convenience methods {@link #getPostset(XEventClass)} and
 * {@link #getPreset(XEventClass)} to efficiently query the set of relations.
 * 
 * @author F. Mannhardt
 *
 */
public interface SimpleCausalGraph {

	Set<XEventClass> getSetActivities();

	Set<Relation> getCausalRelations();

	Set<XEventClass> getPostset(XEventClass activity);

	Set<XEventClass> getPreset(XEventClass activity);

}