package beamline.miners.hm.budgetlossycounting.models;

import java.io.Serializable;

public class SharedDelta implements Serializable {

	private static final long serialVersionUID = -235468256817104055L;
	
	public int currentBucket = 0;
	public int budget = -1;
	
	public DBudgetCases cases = null;
	public DBudgetActivities activities = null;
	public DBudgetRelations relations = null;
	
	public void removeFromAll() {
		cases.removeBelowDelta();
		activities.removeBelowDelta();
		relations.removeBelowDelta();
	}

	public int getSize() {
		int sum = 0;
		sum += cases.size();
		sum += activities.size();
		sum += relations.size();
		return sum;
	}
}
