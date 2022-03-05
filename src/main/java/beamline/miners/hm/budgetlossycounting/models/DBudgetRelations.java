package beamline.miners.hm.budgetlossycounting.models;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;

public class DBudgetRelations extends LossyCountingBudget<Pair<String, String>> {

	private static final long serialVersionUID = 1099720801978544903L;

	public DBudgetRelations(SharedDelta delta) {
		super(delta);
		delta.relations = this;
	}
	
	public void addObservation(String source, String destination) {
		addObservation(Pair.of(source, destination));
	}
	
	public HashMap<Pair<String, String>, Double> getRelations() {
		HashMap<Pair<String, String>, Double> relations = new HashMap<Pair<String, String>, Double>();
		for (Iterator<Pair<String, String>> it = keySet().iterator(); it.hasNext();) {
			Pair<String, String> relationName = it.next();
			relations.put(relationName, (double)get(relationName).getFirst());
		}
		return relations;
	}
}
