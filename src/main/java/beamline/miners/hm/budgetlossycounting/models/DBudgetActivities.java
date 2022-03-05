package beamline.miners.hm.budgetlossycounting.models;

import java.util.HashMap;
import java.util.Iterator;


public class DBudgetActivities extends LossyCountingBudget<String> {

	private static final long serialVersionUID = -8206535869601977531L;

	public DBudgetActivities(SharedDelta delta) {
		super(delta);
		delta.activities = this;
	}
	
	public HashMap<String, Double> getActivities() {
		HashMap<String, Double> activities = new HashMap<String, Double>();
		for (Iterator<String> it = keySet().iterator(); it.hasNext();) {
			String activity = it.next();
			activities.put(activity, (double)get(activity).getFirst());
		}
		return activities;
	}
}
