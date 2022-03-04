package beamline.miners.hm.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.models.cnet.CNet;
import org.processmining.models.cnet.CNetBinding;
import org.processmining.models.cnet.CNetNode;

import beamline.graphviz.Dot;
import beamline.graphviz.DotEdge;
import beamline.graphviz.DotNode;

public class CNetSimplifiedModelView extends Dot {

	private CNet model;
	private Map<String, DotNode> activityToNode;
	private Set<Pair<String, String>> edges;
	
	public CNetSimplifiedModelView(CNet model) {
		this.model = model;
		this.activityToNode = new HashMap<String, DotNode>();
		this.edges = new HashSet<Pair<String, String>>();
		
		realize();
	}
	
	private void realize() {
		for (CNetNode node : model.getNodes()) {
			for (CNetBinding b : model.getInputBindings(node)) {
				for(CNetNode input : b.getBoundNodes()) {
					addRelation(input.getLabel(), node.getLabel());
				}
			}
			for (CNetBinding b : model.getOutputBindings(node)) {
				for(CNetNode output : b.getBoundNodes()) {
					addRelation(node.getLabel(), output.getLabel());
				}
			}
		}
	}
	
	public DotNode getNodeIfNeeded(String activity) {
		if (!activityToNode.containsKey(activity)) {
			CNetActivity node = new CNetActivity(activity);
			addNode(node);
			activityToNode.put(activity, node);
		}
		return activityToNode.get(activity);
	}
	
	public void addRelation(String source, String target) {
		DotNode sourceNode = getNodeIfNeeded(source);
		DotNode targetNode = getNodeIfNeeded(target);
		DotEdge edge = new DotEdge(sourceNode, targetNode);
		Pair<String, String> p = Pair.of(source, target);
		if (!edges.contains(p)) {
			addEdge(edge);
			edges.add(p);
		}
	}
}

class CNetActivity extends DotNode {

	public CNetActivity(String label) {
		super(label, null);
		
		setOption("fontname", "arial");
		setOption("fontsize", "10");
		setOption("fillcolor", "white");
		setOption("shape", "rec");
		setOption("style", "filled");
		setOption("color", "black");
	}
	
	@Override
	public int hashCode() {
		return getLabel().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return getLabel().equals(object);
	}
}