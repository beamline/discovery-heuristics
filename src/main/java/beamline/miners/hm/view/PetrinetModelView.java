package beamline.miners.hm.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import beamline.graphviz.Dot;
import beamline.graphviz.DotEdge;
import beamline.graphviz.DotNode;

public class PetrinetModelView extends Dot {

	private Petrinet model;
	private Map<String, DotNode> activityToNode;
	private Set<Pair<String, String>> edges;
	
	public PetrinetModelView(Petrinet model) {
		this.model = model;
		this.activityToNode = new HashMap<String, DotNode>();
		this.edges = new HashSet<Pair<String, String>>();
		
		realize();
	}
	
	private void realize() {
		for (Transition t : model.getTransitions()) {
			String id = t.getId().toString();
			if (!activityToNode.containsKey(id)) {
				TransitionView tv = new TransitionView(t.getLabel(), id, t.isInvisible());
				addNode(tv);
				activityToNode.put(id.toString(), tv);
			}
		}
		for (Place p : model.getPlaces()) {
			String id = p.getId().toString();
			if (!activityToNode.containsKey(id)) {
				PlaceView pv = new PlaceView(id);
				addNode(pv);
				activityToNode.put(id.toString(), pv);
			}
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : model.getEdges()) {
			DotNode sourceNode = activityToNode.get(e.getSource().getId().toString());
			DotNode targetNode = activityToNode.get(e.getTarget().getId().toString());
			Pair<String, String> p = Pair.of(e.getSource().getId().toString(), e.getTarget().getId().toString());
			if (!edges.contains(p)) {
				addEdge(new DotEdge(sourceNode, targetNode));
				edges.add(p);
			}
		}
	}
}

class TransitionView extends DotNode {

	private String id;
	
	public TransitionView(String label, String id, boolean isInvisible) {
		super(/*isInvisible? "" :*/ label, null);
		this.id = id;
		
		setOption("fontname", "arial");
		setOption("fontsize", "10");
		if (isInvisible) {
			setOption("fillcolor", "gray");
			setOption("color", "white");
		} else {
			setOption("fillcolor", "white");
			setOption("color", "black");
		}
		setOption("shape", "rec");
		setOption("style", "filled");
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}

class PlaceView extends DotNode {
	private String id;
	
	public PlaceView(String id) {
		super("", null);
		this.id = id;
		
		setOption("fillcolor", "white");
		setOption("shape", "circle");
		setOption("style", "filled");
		setOption("color", "black");
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
