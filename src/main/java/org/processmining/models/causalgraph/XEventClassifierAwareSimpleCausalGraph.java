package org.processmining.models.causalgraph;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;

public interface XEventClassifierAwareSimpleCausalGraph extends SimpleCausalGraph {

	public class Factory {
		public static XEventClassifierAwareSimpleCausalGraph construct(final XEventClassifier classifier,
				final Set<XEventClass> activities, final Set<Relation> causalRelations) {
			return new SimpleCausalGraphImpl(classifier, activities, causalRelations);
		}

	}

	XEventClassifier getEventClassifier();

}
