package beamline.miners.hm.budgetlossycounting.models;

import org.processmining.models.cnet.CNet;

import beamline.models.responses.Response;

public class StreamingCNet extends Response {

	private static final long serialVersionUID = -803561441785918416L;
	private CNet cnet;
	
	public StreamingCNet(CNet cnet) {
		this.cnet = cnet;
	}
	
	public CNet getCnet() {
		return cnet;
	}
	
	public void setCnet(CNet cnet) {
		this.cnet = cnet;
	}
}
