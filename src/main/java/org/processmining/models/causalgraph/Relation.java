package org.processmining.models.causalgraph;

import org.deckfour.xes.classification.XEventClass;

/**
 * Relation between a source and a target {@link XEventClass}. Use the
 * {@link Factory} for common operations on relations.
 * 
 * @author F. Mannhardt
 *
 */
public interface Relation {

	public static final class Factory {
		
		private Factory() {
		}

		public static Relation create(XEventClass source, XEventClass target) {
			return new RelationImpl(source, target);
		}

		public static Relation inverse(Relation relation) {
			return create(relation.getTarget(), relation.getSource());
		}

	}

	XEventClass getSource();

	XEventClass getTarget();

}