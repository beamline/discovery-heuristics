package org.processmining.models.cnet.importing;

import java.io.InputStream;

import org.processmining.models.cnet.CausalNet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Imports a CausalNet
 * 
 * @author aadrians
 * @author F. Mannhardt
 * 
 */
public class ImportCNet {

	protected Object importFromStream(InputStream input, String filename, long fileSizeInBytes)
			throws Exception {

		// start parsing
		/*
		 * Get an XML pull parser.
		 */
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		/*
		 * Initialize the parser on the provided input.
		 */
		xpp.setInput(input, null);
		/*
		 * Get the first event type.
		 */
		int eventType = xpp.getEventType();
		/*
		 * Skip whatever we find until we've found a start tag.
		 */
		while (eventType != XmlPullParser.START_TAG) {
			eventType = xpp.next();
		}

		CNetImporter importer = new CNetImporter();
		/*
		 * Check whether start tag corresponds to CNet start tag.
		 */
		if (xpp.getName().equals(CNetImporter.STARTTAG)) {
			/*
			 * Yes it does. Import the PNML element.
			 */
			importer.importElement(xpp);
		} else {
			/*
			 * No it does not. Return null to signal failure.
			 */
			return null;
		}


		// rename results
		CausalNet result = importer.getImportResult();
		
		return result;
	}

}
