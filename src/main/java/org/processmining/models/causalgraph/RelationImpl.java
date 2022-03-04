package org.processmining.models.causalgraph;

import org.deckfour.xes.classification.XEventClass;

public final class RelationImpl implements Relation {

	private final XEventClass source;
	private final XEventClass target;

	public RelationImpl(XEventClass source, XEventClass target) {
		this.source = source;
		this.target = target;
	}

	public XEventClass getTarget() {
		return target;
	}

	public XEventClass getSource() {
		return source;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RelationImpl))
			return false;
		RelationImpl other = (RelationImpl) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	public String toString() {
		return "(" + source + "," + target + ")";
	}

}