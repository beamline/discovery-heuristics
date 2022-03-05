package beamline.miners.hm.budgetlossycounting.models;

public class SharedDelta {

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
