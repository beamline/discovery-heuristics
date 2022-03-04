package org.processmining.models.cnet.exporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.processmining.models.cnet.CNetBinding;
import org.processmining.models.cnet.CNetNode;
import org.processmining.models.cnet.CausalNet;

/**
 * Exports a CausalNet
 * 
 * @author aadrians
 * @author F. Mannhardt
 * @author Andrea Burattin
 */
public class ExportCNet {

	public void exportCNetToCNetFile(CausalNet net, File file) throws IOException {
		exportCNetToCNetFile(net.getLabel(), net, file);
	}

	public void exportCNetToCNetFile(String label, CausalNet net, File file) throws IOException {
		// export  to file
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
				+ createStringRep(label, net, net.getStartNode(), net.getEndNode());

		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
			bw.write(text);
		}
	}

	private String createStringRep(String label, CausalNet net, CNetNode startNode, CNetNode endNode) {
		StringBuilder sb = new StringBuilder();
		sb.append("<cnet>");
		sb.append("<net type=\"http://www.processmining.org\" id=\"");
		sb.append(StringEscapeUtils.escapeXml(net.getLabel()));
		sb.append("\" />");
		sb.append("<name>");
		if (label == null || label.isEmpty()) { // fix for causal nets with empty name
			sb.append(" ");
		} else {
			sb.append(StringEscapeUtils.escapeXml(label));
		}
		sb.append("</name>");

		// utility variables
		int id = 0;
		Map<CNetNode, Integer> node2Id = new HashMap<CNetNode, Integer>();

		for (CNetNode node : net.getNodes()) {
			node2Id.put(node, id);

			sb.append("<node id=\"");
			sb.append(id);
			sb.append("\" isInvisible=\"false\">");
			sb.append("<name>");
			sb.append(node.getLabel());
			sb.append("</name>");
			sb.append("</node>");

			id++;
		}

		sb.append("<startTaskNode id=\"");
		sb.append(node2Id.get(startNode));
		sb.append("\"/>");

		sb.append("<endTaskNode id=\"");
		sb.append(node2Id.get(endNode));
		sb.append("\"/>");

		// set flex 
		for (CNetNode node : net.getNodes()) {
			// input first
			if (!node.equals(startNode)) {
				sb.append("<inputNode id=\"");
				sb.append(node2Id.get(node));
				sb.append("\">");
				for (CNetBinding inputBinding : net.getInputBindings(node)) {
					sb.append("<inputSet>");
					for (CNetNode nodeInput : inputBinding.getBoundNodes()) {
						sb.append("<node id=\"");
						sb.append(node2Id.get(nodeInput));
						sb.append("\" />");
					}
					sb.append("</inputSet>");
				}
				sb.append("</inputNode>");
			}

			// output first
			if (!node.equals(endNode)) {
				sb.append("<outputNode id=\"");
				sb.append(node2Id.get(node));
				sb.append("\">");
				for (CNetBinding outputBinding : net.getOutputBindings(node)) {
					sb.append("<outputSet>");
					for (CNetNode nodeOutput : outputBinding.getBoundNodes()) {
						sb.append("<node id=\"");
						sb.append(node2Id.get(nodeOutput));
						sb.append("\" />");
					}
					sb.append("</outputSet>");
				}
				sb.append("</outputNode>");
			}
		}

		// add arcs
		Set<CNetNode> allNodes = net.getNodes();
		Set<CNetNode> exploredCNet = new HashSet<CNetNode>(allNodes.size());
		for (CNetNode node : allNodes) {
			for (CNetNode succ : net.getSuccessors(node)) {
				if (!exploredCNet.contains(succ)) {
					sb.append("<arc id=\"");
					sb.append(id);
					sb.append("\" source=\"");
					sb.append(node2Id.get(node));
					sb.append("\" target=\"");
					sb.append(node2Id.get(succ));
					sb.append("\" />");
					id++;
				}
			}

			for (CNetNode pred : net.getPredecessors(node)) {
				if (!exploredCNet.contains(pred)) {
					sb.append("<arc id=\"");
					sb.append(id);
					sb.append("\" source=\"");
					sb.append(node2Id.get(pred));
					sb.append("\" target=\"");
					sb.append(node2Id.get(node));
					sb.append("\" />");
					id++;
				}
			}
		}
		sb.append("</cnet>");
		return sb.toString();
	}
}
