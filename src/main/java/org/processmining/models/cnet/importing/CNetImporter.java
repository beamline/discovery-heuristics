package org.processmining.models.cnet.importing;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.processmining.models.cnet.CNet;
import org.processmining.models.cnet.CNetNode;
import org.processmining.models.cnet.CausalNet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author aadrians
 * 
 */
public class CNetImporter {
	
	public static final String STARTTAG = "cnet";
	
	// result variable
	private CNet cnet;

	public CNetImporter() {
	}

	/**
	 * Assumption : the xml file is valid
	 * 
	 * @param xpp
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public void importElement(XmlPullParser xpp) throws XmlPullParserException, IOException {
		// utility variables
		String nodePairInfo = null;

		String lastFocusNode = "";
		Stack<String> tagStack = new Stack<String>(); // only used for inputNode and so on

		// mapping 
		Map<String, CNetNode> Id2Node = new HashMap<String, CNetNode>();

		// start parsing
		int eventType = xpp.getEventType(); // has to be XmlPullParser.START_TAG

		Set<CNetNode> lastBinding = new HashSet<CNetNode>();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equalsIgnoreCase("name")) {
					tagStack.add("name");
				} else if (xpp.getName().equalsIgnoreCase("startTaskNode")) {
					cnet.setStartNode(Id2Node.get(xpp.getAttributeValue(0)));
				} else if (xpp.getName().equalsIgnoreCase("endTaskNode")) {
					cnet.setEndNode(Id2Node.get(xpp.getAttributeValue(0)));
				} else if (xpp.getName().equalsIgnoreCase("node")) {
					if (tagStack.isEmpty()) { // start of node listing
						nodePairInfo = xpp.getAttributeValue(0);
					} else {
						lastBinding.add(Id2Node.get(xpp.getAttributeValue(0)));
					}
				} else if (xpp.getName().equalsIgnoreCase("inputNode")) {
					tagStack.add("inputNode");
					lastFocusNode = xpp.getAttributeValue(0);
				} else if (xpp.getName().equalsIgnoreCase("outputNode")) {
					tagStack.add("outputNode");
					lastFocusNode = xpp.getAttributeValue(0);
				} else if (xpp.getName().equalsIgnoreCase("inputSet")) {
					tagStack.add("inputSet");
					lastBinding = new HashSet<CNetNode>();
				} else if (xpp.getName().equalsIgnoreCase("outputSet")) {
					tagStack.add("outputSet");
					lastBinding = new HashSet<CNetNode>();
				}

				// ignore arc 
				// (xpp.getName().equalsIgnoreCase("arc"))

				// ignore cancellation region information.
				// (xpp.getName().equalsIgnoreCase("cancellationRegionNode")) 
			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equalsIgnoreCase("name")) {
					tagStack.pop();
				} else if (xpp.getName().equalsIgnoreCase("inputNode")) {
					tagStack.pop();
				} else if (xpp.getName().equalsIgnoreCase("outputNode")) {
					tagStack.pop();
				} else if (xpp.getName().equalsIgnoreCase("inputSet")) {
					tagStack.pop();
					//Id2Node.get(lastFocusNode).addInputBinding(lastBinding);
					cnet.addInputBinding(Id2Node.get(lastFocusNode), lastBinding);
				} else if (xpp.getName().equalsIgnoreCase("outputSet")) {
					tagStack.pop();
					//Id2Node.get(lastFocusNode).addOutputBinding(lastBinding);
					cnet.addOutputBinding(Id2Node.get(lastFocusNode), lastBinding);
				}
				// ignore cancellation node
			} else if (eventType == XmlPullParser.TEXT) {
				if (!tagStack.isEmpty()) {
					if (tagStack.peek().equals("name")) {
						if (nodePairInfo == null) {
							// means this is the name of the net
							cnet = new CNet(xpp.getText());
						} else {
							// can only be node
							CNetNode node = new CNetNode(xpp.getText());
							cnet.addNode(node);
							Id2Node.put(nodePairInfo, node);
							nodePairInfo = null;
						}
					}
				}
			}
			eventType = xpp.next();
		} // end of documents
	}

	public CausalNet getImportResult() {
		return cnet;
	}
}
