package uk.ac.dotrural.quality.edsensor.sparql;

import uk.ac.dotrural.quality.edsensor.EdSensor;

import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class Updater {
	
	public void sendUpdate(String endpoint, EdSensor edsensor, String query)
	{	
		UpdateRequest request = UpdateFactory.create();
		request.add(query);
		
		UpdateProcessor update = UpdateExecutionFactory.createRemoteForm(request, endpoint);
		update.execute();
		
		edsensor.status();
	}

}
