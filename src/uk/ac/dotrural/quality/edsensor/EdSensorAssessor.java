package uk.ac.dotrural.quality.edsensor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import uk.ac.dotrural.quality.edsensor.assessment.AnnotationParser;
import uk.ac.dotrural.quality.edsensor.observation.AccelerometerObservation;
import uk.ac.dotrural.quality.edsensor.observation.AltitudeObservation;
import uk.ac.dotrural.quality.edsensor.observation.GPSObservation;
import uk.ac.dotrural.quality.edsensor.observation.Observation;
import uk.ac.dotrural.quality.edsensor.observation.ObservationType;
import uk.ac.dotrural.reasoning.reasoner.Reasoner;
import uk.ac.dotrural.reasoning.reasoner.ReasonerResult;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class EdSensorAssessor {
	
	private final boolean observationMetadata = true; 
	private final boolean observationProvenance = false; 
	private final boolean qualityProvenance = false; 
	private final boolean doLogging = true;
	
	private final String NS = "http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/";
	
	private long applicationTime, inferredTriples, assessedTriples, assessedDimensions;
	
	private String endpoint = "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/CarWalkTrain";

	private String[] props = {
			"Temperature",
			"Humidity",
			"Location",
			"Speed",
			"Acceleration",
			"Altitude"
	};
	
	private String[] queries = {
		"SELECT * WHERE {?obs a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ?obs <http://purl.oclc.org/NET/ssnx/ssn#featureOfInterest> ?foi . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/Temperature> . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observedBy> ?sens . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?rTime . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationServerTime> ?sTime . ?obs <http://dtp-126.sncs.abdn.ac.uk/quality/CarSensor/Event> ?event . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?res . ?res <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . ?val <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?value . }",
		"SELECT * WHERE {?obs a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ?obs <http://purl.oclc.org/NET/ssnx/ssn#featureOfInterest> ?foi . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/Humidity> . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observedBy> ?sens . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?rTime . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationServerTime> ?sTime . ?obs <http://dtp-126.sncs.abdn.ac.uk/quality/CarSensor/Event> ?event . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?res . ?res <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . ?val <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?value . }",
		"SELECT * WHERE {?obs a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ?obs <http://purl.oclc.org/NET/ssnx/ssn#featureOfInterest> ?foi . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/Location> . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observedBy> ?sens . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?rTime . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationServerTime> ?sTime . ?obs <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Event> ?event . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?res . ?res <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . ?val <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat . ?val <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?lon . ?val <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/satellites> ?sats . ?val <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/precision> ?prec . }",
		"SELECT * WHERE {?obs a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ?obs <http://purl.oclc.org/NET/ssnx/ssn#featureOfInterest> ?foi . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/Speed> . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observedBy> ?sens . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?rTime . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationServerTime> ?sTime . ?obs <http://dtp-126.sncs.abdn.ac.uk/quality/CarSensor/Event> ?event . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?res . ?res <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . ?val <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?value . }",
		"SELECT * WHERE {?obs a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ?obs <http://purl.oclc.org/NET/ssnx/ssn#featureOfInterest> ?foi . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/Acceleration> . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observedBy> ?sens . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?rTime . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationServerTime> ?sTime . ?obs <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Event> ?event . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?res . ?res <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . ?val <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?value . ?val <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/AccelerationDirection> ?dir . }",
		"SELECT * WHERE {?obs a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ?obs <http://purl.oclc.org/NET/ssnx/ssn#featureOfInterest> ?foi . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/Altitude> . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observedBy> ?sens . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?rTime . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationServerTime> ?sTime . ?obs <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Event> ?event . ?obs <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?res . ?res <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?val . ?val <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?value . ?val <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/satellites> ?sats . }"									
							   };

	public static void main(String[] args) {
		new EdSensorAssessor();
	}
	
	public EdSensorAssessor(String endpoint)
	{
		this.endpoint = endpoint;
	}
	
	public EdSensorAssessor()
	{
		ArrayList<Observation> observations = getObservations();
		
		for(int i=0;i<observations.size();i++)
		{
			long start = 0, finish = 0;
			start = System.currentTimeMillis();
			Observation obs = (Observation)observations.get(i);
			
			Reasoner reasoner = new Reasoner("http://dtp-126.sncs.abdn.ac.uk/ontologies/CarWalkTrain/CarWalkTrain.ttl", "TTL", false);
			
			ReasonerResult results = reasoner.performReasoning(obs.getRdfModel(NS));
			finish = System.currentTimeMillis();
			
			applicationTime = (finish - start);
			assessedTriples = obs.getRdfModel(NS).size();
			inferredTriples = results.ntriples.size();
			
			assessedDimensions = 0;
			
			AnnotationParser parser = new AnnotationParser();			
			if(inferredTriples > 0)
				assessedDimensions = parser.countResults(results.ntriples);
			
			if(doLogging)
			{
				logToDatabase(results, (observationMetadata ? 1 : 0), (observationProvenance ? 1 : 0), (qualityProvenance ? 1: 0), obs.id);
			}
			
			System.out.println("Assessment " + i + " of " + observations.size() + " took " + results.duration + "ms");
		}
		System.out.println("No. of observations: " + observations.size());
	}
	
	public ArrayList<Observation> getObservations()
	{		
		System.out.println("[Getting observations]");
		ArrayList<Observation> observations = new ArrayList<Observation>();				   

		for(int i=0;i<queries.length;i++)
		{
			String q = queries[i];
			try {
				QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, q);
				ResultSet rs = qe.execSelect();
				while(rs.hasNext())
				{
					QuerySolution qs = rs.next();
					
					Resource observation = qs.getResource("obs");
					Resource foi = qs.getResource("foi");
					String prop = props[i];
					Resource sens = qs.getResource("sens");
					Literal rTime = qs.getLiteral("rTime");
					Literal sTime = qs.getLiteral("sTime");
					Literal event = qs.getLiteral("event");
					
					Resource res = qs.getResource("res");
					Resource val = qs.getResource("val");
					
					Literal value;
					Literal sats;
					
					switch(ObservationType.strToObsType(prop))
					{
					case ACCELERATION:
						value = qs.getLiteral("value");
						Literal direction = qs.getLiteral("dir");
						
						AccelerometerObservation aObs = new AccelerometerObservation(
												observation.getURI(),
												ObservationType.strToObsType(prop),
												foi.getURI(),
												sens.getURI(),
												res.getURI(),
												val.getURI(),
												value.getLexicalForm(),
												direction.getLexicalForm(),
												rTime.getLexicalForm(),
												sTime.getLexicalForm(),
												event.getLexicalForm()
										);
						observations.add(aObs);
						
						break;
					case GPS:
						Literal lat = qs.getLiteral("lat");
						Literal lon = qs.getLiteral("lon");
						sats = qs.getLiteral("sats");
						Literal prec = qs.getLiteral("prec");
						
						GPSObservation lObs = new GPSObservation(
									observation.getURI(),
									ObservationType.strToObsType(prop),
									foi.getURI(),
									sens.getURI(),
									res.getURI(),
									val.getURI(),
									lat.getLexicalForm(),
									lon.getLexicalForm(),
									sats.getLexicalForm(),
									prec.getLexicalForm(),
									rTime.getLexicalForm(),
									sTime.getLexicalForm(),
									event.getLexicalForm()
								);
						
						observations.add(lObs);
						
						break;
	
					case ALTITUDE:
						value = qs.getLiteral("value");
						sats = qs.getLiteral("sats");
						
						AltitudeObservation alObs = new AltitudeObservation(
									observation.getURI(),
									ObservationType.strToObsType(prop),
									foi.getURI(),
									sens.getURI(),
									res.getURI(),
									val.getURI(),
									value.getLexicalForm(),
									sats.getLexicalForm(),
									rTime.getLexicalForm(),
									sTime.getLexicalForm(),
									event.getLexicalForm()
								);
						
						observations.add(alObs);
						
						break;
					default:
						value = qs.getLiteral("value");
						
						Observation obs = new Observation(
											observation.getURI(),
											ObservationType.strToObsType(prop),
											foi.getURI(),
											sens.getURI(),
											res.getURI(),
											val.getURI(),
											value.getLexicalForm(),
											rTime.getLexicalForm(),
											sTime.getLexicalForm(),
											event.getLexicalForm()
										 );
						
						observations.add(obs);	
						
						break;
					}
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return observations;	
	}
	
	private void logToDatabase(ReasonerResult results, int used_obs_metadata, int used_obs_prov, int used_qual_prov, String observationUri)
	{
		//String url = 	"http://dtp-126.sncs.abdn.ac.uk/MySQLBridge.php?" +
		String url = "http://dtp-126.sncs.abdn.ac.uk:8080/CarWalkTrainBridge/MySQLBridge?db=carwalktrain_logs" + 
						"&at=" + applicationTime +
						"&rt=" + results.duration +
						"&ms=" + assessedTriples +
						"&it=" + inferredTriples +
						"&ad=" + assessedDimensions +
						"&uom=" + used_obs_metadata +
						"&uop=" + used_obs_prov + 
						"&uqp=" + used_qual_prov +
						"&ou=" + observationUri;
		
		System.out.println(url);
		
		StringBuilder in = new StringBuilder();
		String tmp = "";
		try
		{
			InputStreamReader input = new InputStreamReader(new URL(url).openStream());
			BufferedReader br = new BufferedReader(input);
			while((tmp = br.readLine()) != null)
			{
				in.append(tmp);
			}	
			input.close();
			br.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		//Parse response
		JSONObject response = (JSONObject)JSONSerializer.toJSON(in.toString());
		String result = response.getString("result");
		String reason = response.getString("reason");
		
		if(result.equalsIgnoreCase("success"))
			System.out.println("Log entry created successfully!");
		else
			System.out.println("Logging failed: " + reason);
		
	}

}
