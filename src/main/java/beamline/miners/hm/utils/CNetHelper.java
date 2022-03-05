package beamline.miners.hm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

import org.processmining.models.cnet.CNet;
import org.processmining.models.cnet.CNetBinding;
import org.processmining.models.cnet.CNetNode;
import org.processmining.models.cnet.exporting.ExportCNet;
import org.processmining.models.cnet.importing.CNetImporter;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.cnet.replayer.converter.CNet2PetrinetConverter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 *
 * @author Andrea Burattin
 */
public class CNetHelper {

	/**
	 * 
	 * @param model
	 * @param source
	 * @param destination
	 */
	public static void addConnection(CNet model, CNetNode source, CNetNode destination) {
		if (model != null && source != null && destination != null) {
			model.addOutputBinding(source, destination);
			model.addInputBinding(destination, source);
		}
	}
	
	/**
	 * 
	 * @param model
	 * @param split
	 * @param branches
	 */
	public static void addAndSplit(CNet model, CNetNode split, CNetNode ... branches) {
		model.addOutputBinding(split, branches);
		for (CNetNode b : branches) {
			model.addInputBinding(b, split);
		}
	}
	
	/**
	 * 
	 * @param model
	 * @param join
	 * @param branches
	 */
	public static void addAndJoin(CNet model, CNetNode join, CNetNode ... branches) {
		model.addInputBinding(join, branches);
		for (CNetNode b : branches) {
			model.addOutputBinding(b, join);
		}
	}
	
	/**
	 * 
	 * @param model
	 * @param startEvents
	 * @param node
	 * @return
	 */
	public static Boolean nodeRequireArtificialInput(CNet model, Set<String> startEvents, CNetNode node) {
		if (startEvents.contains(node.getLabel())) {
			// event is a start event, it is allowed to have empty input
			return false;
		} else {
			Set<CNetBinding> input = model.getInputBindings(node);
			if (input.size() == 0) {
				// no input edge
				return true;
			} else if (input.size() == 1 && input.iterator().next().getNode().equals(node)) {
				// one input edge, and is a self loop
				return true;
			} else {
				// more then one input edge
				return false;
			}
		}
	}
	
	/**
	 * 
	 * @param model
	 * @param endEvents
	 * @param node
	 * @return
	 */
	public static Boolean nodeRequireArtificialOutput(CNet model, Set<String> endEvents, CNetNode node) {
		if (endEvents.contains(node.getLabel())) {
			// event is an end event, it is allowed to have empty output
			return false;
		} else {
			Set<CNetBinding> output = model.getOutputBindings(node);
			if (output.size() == 0) {
				// no output edge
				return true;
			} else if (output.size() == 1 && output.iterator().next().getNode().equals(node)) {
				// one output edge, and is a self loop
				return true;
			} else {
				// more then one output edge
				return false;
			}
		}
	}

	static int depth = 0;
	/**
	 * 
	 * @param model
	 * @param currentNode
	 * @param endNode
	 * @param visitedNodes
	 * @return
	 */
	public static boolean reachEndNode(CNet model, CNetNode currentNode, CNetNode endNode, Set<CNetNode> visitedNodes) {
		visitedNodes.add(currentNode);
//		System.out.print(currentNode.getLabel() + " ");
		
		if (currentNode.equals(endNode)) {
//			System.out.println(" -> is end");
			return true;
		}
		
		for (CNetBinding binding : model.getOutputBindings(currentNode)) {
			for (CNetNode node : binding.getBoundNodes()) {
				if (!visitedNodes.contains(node)) {
					if (reachEndNode(model, node, endNode, visitedNodes)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param context
	 * @param model
	 * @return
	 */
	public static Petrinet convert(CNet model) {
		
		CNet2PetrinetConverter converter = new CNet2PetrinetConverter();
		converter.convert(model, true);
		Petrinet net = converter.getPetrinet();
		
		return net;
	}

	/**
	 *
	 * @param context
	 * @param destinationPath
	 * @param modelName
	 * @param model
	 */
	public static void saveModelToFile(String destinationPath, String modelName, CNet model) {
		if (destinationPath == null || modelName == null) {
			return;
		}
		String completePath = destinationPath  + System.getProperty("file.separator") + modelName;
		try {
			ExportCNet export = new ExportCNet();
			export.exportCNetToCNetFile(model, new File(completePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param sourcePath
	 * @return
	 */
	public static CNet loadModelFromFile(String sourcePath) {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(new FileInputStream(sourcePath), null);
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.START_TAG) {
				eventType = xpp.next();
			}
			CNetImporter importer = new CNetImporter();
			if (xpp.getName().equals(CNetImporter.STARTTAG)) {
				importer.importElement(xpp);
			} else {
				return null;
			}
			CNet result = (CNet) importer.getImportResult();
			if (result.isConsistent()) {
				return result;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*private static Pair<String, Pair<String, Set<String>>> fromCNetBindingToSimpleStructure(CNetBinding binding) {
		Pair<String, Pair<String, Set<String>>> p = new Pair<String, Pair<String, Set<String>>>();
		return p;
	}*/
	
	/*public static Double calculateF1(CNet baseline, CNet extracted) {
//		Set<String> activitiesBaseline = new HashSet<String>();
//		for(CNetNode n : baseline.getNodes()) activitiesBaseline.add(n.getLabel());
//		Set<String> activitiesExtracted = new HashSet<String>();
//		for(CNetNode n : extracted.getNodes()) activitiesExtracted.add(n.getLabel());
//		
//		Double precision = getPrecision(activitiesBaseline, activitiesExtracted);
//		Double recall = getRecall(activitiesBaseline, activitiesExtracted);
		
		Set<CNetBinding> bindingsBaseline = baseline.getBindings();
		Set<CNetBinding> bindingsExtracted = extracted.getBindings();
		
		System.out.println(bindingsBaseline);
		System.out.println(bindingsExtracted);
		
		Double precision = getPrecision(bindingsBaseline, bindingsExtracted);
		Double recall = getRecall(bindingsBaseline, bindingsExtracted);
		
		return 2.0 * (precision * recall) / (precision + recall);
	}*/
	
	/*private static <K> Double getPrecision(Set<K> baseline, Set<K> extracted) {
		Double tp = 0.0;
		Double fp = 0.0;
		Double fn = 0.0;
		for (K a : extracted) {
			if (baseline.contains(a)) {
				tp++;
			} else {
				fn++;
			}
		}
		for (K a : baseline) {
			if (!extracted.contains(a)) {
				fp++;
			}
		}
		System.out.println("TP = " + tp);
		System.out.println("FP = " + fp);
		System.out.println("FN = " + fn);
		return tp / (tp + fp);
	}
	
	private static <K> Double getRecall(Set<K> baseline, Set<K> extracted) {
		Double tp = 0.0;
		Double fp = 0.0;
		Double fn = 0.0;
		for (K a : extracted) {
			if (baseline.contains(a)) {
				tp++;
			} else {
				fn++;
			}
		}
		for (K a : baseline) {
			if (!extracted.contains(a)) {
				fp++;
			}
		}
		return tp / (tp + fn);
	}*/
}
