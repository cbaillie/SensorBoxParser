package uk.ac.dotrural.quality.edsensor;

import java.io.BufferedReader;

import java.io.FileReader;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import uk.ac.dotrural.quality.edsensor.assessment.AnnotationParser;
import uk.ac.dotrural.quality.edsensor.mysql.MySQL;
import uk.ac.dotrural.quality.edsensor.observation.AccelerometerObservation;
import uk.ac.dotrural.quality.edsensor.observation.AltitudeObservation;
import uk.ac.dotrural.quality.edsensor.observation.GPSObservation;
import uk.ac.dotrural.quality.edsensor.observation.Observation;
import uk.ac.dotrural.quality.edsensor.observation.ObservationType;
import uk.ac.dotrural.reasoning.reasoner.Reasoner;
import uk.ac.dotrural.reasoning.reasoner.ReasonerResult;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class EdSensorAssessor {
	
	private long applicationTime, inferredTriples, assessedDimensions;
	
	public String file;
	public boolean provenance;
	private boolean doLogging = true;
	
	public String filename;
	public String filepath;
	
	private final String NS = "http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/";
	
	public String storename;
	public String endpoint;
	
	private String qualityRules = "http://dtp-126.sncs.abdn.ac.uk/ontologies/CarWalkTrain/QualityRules.ttl";
	private String provQualRules = "http://dtp-126.sncs.abdn.ac.uk/ontologies/CarWalkTrain/ProvQualityRules.ttl";
	
	private String dbUrl;
	private String dbUser;
	private String dbPass;

	private String[] props = {
			"Temperature",
			"Humidity",
			"Location",
			"Speed",
			"Acceleration",
			"Altitude"
	};

	public static void main(String[] args) {
		if(args.length == 0)
			//System.out.println("Arguments: dataset name, use provenance true/false");
			new EdSensorAssessor("CityWalk", "true");
		else {
			new EdSensorAssessor(args[0], args[1]);	
		}
	}
	
	public EdSensorAssessor(String endpoint)
	{
		//this.endpoint = endpoint;
		storename = endpoint;
	}
	
	public EdSensorAssessor(String dataset, String prov)
	{	
		file = dataset;
		provenance = Boolean.parseBoolean(prov);
		
		filename = file +".csv";
		filepath = "resource/" + filename;
		storename = "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/" + file;
		endpoint = storename.concat("/statements");
		
		getUserDetails();

		ArrayList<OntModel> models = new ArrayList<OntModel>();
		ArrayList<Observation> obs = getSSN();
		
		for(int i=0;i<obs.size();i++)
		{
			Observation o = (Observation)obs.get(i);
			switch(o.property)
			{
			case ACCELERATION:
				AccelerometerObservation aobs = (AccelerometerObservation)o;
				models.add(aobs.getRdfModel(NS));
				break;
			case GPS:
				GPSObservation gobs = (GPSObservation)o;
				models.add(gobs.getRdfModel(NS));
				break;
			case ALTITUDE:
				AltitudeObservation alobs = (AltitudeObservation)o;
				models.add(alobs.getRdfModel(NS));
				break;
			default:
				models.add(o.getRdfModel(NS));
				break;
			}
		}
		
		models.addAll(getDerivedObservations());
		
		for(int i=0;i<models.size();i++)
		{
			OntModel m = (OntModel)models.get(i);
			assess(m, m.size());
			m.close();
			System.out.println(i + " of " + models.size() + " complete.");
		}
	}
	
	private ArrayList<Observation> getSSN()
	{
		return getObservations(storename);
	}
	
	private ArrayList<OntModel> getDerivedObservations()
	{
		ArrayList<Observation> observations = getObservations(storename.concat("Provenance"));
		ArrayList<OntModel> derivedObs = new ArrayList<OntModel>();
		for(int i=0;i<observations.size();i++)
		{
			OntModel assessment = ModelFactory.createOntologyModel();
			Observation obs = observations.get(i);	
			assessment.add(obs.getRdfModel(NS));
			ArrayList<String> dObs;
			switch(obs.property)
			{
			case ALTITUDE:
				dObs = obs.derivedFrom;
				for(int j=0;j<dObs.size();j++)
				{
					String dObsUri = dObs.get(j);
					ResultSet rs = getObservation("qObs:" + dObsUri.substring(dObsUri.lastIndexOf('/')+1), obs.property);
					while(rs.hasNext())
					{
						QuerySolution qs = rs.next();
						
						Resource foi = qs.getResource("foi");
						String prop = props[i];
						Resource sens = qs.getResource("sens");
						Literal rTime = qs.getLiteral("rTime");
						Literal event = qs.getLiteral("event");
						
						Resource res = qs.getResource("res");
						Resource val = qs.getResource("val");
						
						Literal value = qs.getLiteral("value");
				
						value = qs.getLiteral("value");
						Literal sats = qs.getLiteral("sats");
				
						AltitudeObservation alObs = new AltitudeObservation(
							dObsUri,
							ObservationType.strToObsType(prop),		
							foi.getURI(),							
							sens.getURI(),
							res.getURI(),
							val.getURI(),
							value.getLexicalForm(),
							sats.getLexicalForm(),
							rTime.getLexicalForm(),
							"0",
							event.getLexicalForm()
						);
						assessment.add(alObs.getRdfModel(NS));
					}
				}
				
				break;
			default:
				dObs = obs.derivedFrom;
				for(int j=0;j<dObs.size();j++)
				{
					String dObsUri = dObs.get(j);
					ResultSet rs = getObservation("qObs:" + dObsUri.substring(dObsUri.lastIndexOf('/')+1), obs.property);
					while(rs.hasNext())
					{
						QuerySolution qs = rs.next();
						
						Resource foi = qs.getResource("foi");
						ObservationType prop = obs.property;
						Resource sens = qs.getResource("sens");
						Literal rTime = qs.getLiteral("rTime");
						Literal event = qs.getLiteral("event");
						
						Resource res = qs.getResource("res");
						Resource val = qs.getResource("val");
						
						Literal value = qs.getLiteral("value");
						
						Observation observation = new Observation(
											dObsUri,
											prop,
											foi.getURI(),
											sens.getURI(),
											res.getURI(),
											val.getURI(),
											value.getLexicalForm(),
											rTime.getLexicalForm(),
											"0",
											event.getLexicalForm()
										 );
						assessment.add(observation.getRdfModel(NS));
					}
				}
				break;
			}
			derivedObs.add(assessment);
		}
		return derivedObs;
	}
	
	private void assess(OntModel obs, long sz)
	{
		inferredTriples = 0;
		long start = 0, finish = 0, reasoningTime = 0;
		start = System.currentTimeMillis();
		
		Reasoner reasoner;
		ReasonerResult results;
		if(!this.provenance)
		{
			reasoner = new Reasoner(this.qualityRules, "TTL", false);
			results = reasoner.performReasoning(obs);
			inferredTriples += results.ntriples.size();
			reasoningTime = results.duration;
		}
		else
		{
			reasoner = new Reasoner(this.qualityRules, "TTL", false);
			results = reasoner.performReasoning(obs);
			inferredTriples += results.ntriples.size();
			reasoningTime = results.duration;
			obs.add(results.ntriples);
			
			reasoner = new Reasoner(this.provQualRules, "TTL", false);
			results = reasoner.performReasoning(obs);
			inferredTriples += results.ntriples.size();
			reasoningTime += results.duration;
		}
		
		finish = System.currentTimeMillis();
		
		applicationTime = (finish - start);
		
		assessedDimensions = 0;
		
		AnnotationParser parser = new AnnotationParser();			
		if(inferredTriples > 0)
			assessedDimensions = parser.countResults(results.ntriples);	
		
		if(doLogging)
		{
			logToDatabase(reasoningTime, sz, (provenance ? 0 : 1), (provenance ? 1 : 0), 0, "");
		}
			
		System.out.println("Assessment took " + results.duration + "ms");
	}
	
	private void getUserDetails()
	{
		try
		{
			String read;
			BufferedReader br = new BufferedReader(new FileReader("config/db.cfg"));
			while((read = br.readLine()) != null)
			{
				String[] tokens = read.split("=");
				if(tokens[0].equals("url"))
					dbUrl = tokens[1].concat(file);
				else if(tokens[0].equals("user"))
					dbUser = tokens[1];
				else if(tokens[0].equals("password"))
					dbPass = tokens[1];
			}
			br.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private ResultSet getObservation(String uri, ObservationType property)
	{
		String query = buildQuery(uri, property);
		
		if(query.length() > 0){
			QueryExecution qe = QueryExecutionFactory.sparqlService(storename, query); 
			return qe.execSelect();
		}
		return null;
	}
	
	private String buildQuery(String obs, ObservationType property)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("PREFIX qual:   <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/> ");
		sb.append("PREFIX qProp:  <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Property/> ");
		sb.append("PREFIX qObs:   <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Observation/> ");
		sb.append("PREFIX ssn:	  <http://purl.oclc.org/NET/ssnx/ssn#> ");
		sb.append("PREFIX geo:	  <http://www.w3.org/2003/01/geo/wgs84_pos#> ");
		sb.append("PREFIX prov:	  <http://www.w3.org/ns/prov-o/> ");
		sb.append("SELECT * WHERE {");
		
		sb.append(obs);
		sb.append(" a ssn:Observation . ");
		sb.append(obs);
		sb.append(" ssn:featureOfInterest ?foi . ");
		sb.append(obs);
		sb.append(" ssn:observedBy ?sens . ");
		sb.append(obs);
		sb.append(" ssn:observationResultTime ?rTime . ");
		sb.append(obs);
		sb.append(" ssn:observationResult ?res . ");
		
		switch(property)
		{
		case TEMPERATURE:
			sb.append(obs);
			sb.append(" ssn:observedProperty qProp:Temperature . ");
			sb.append(" ?res ssn:hasValue ?val . ");
			sb.append(" ?val ssn:hasValue ?value . ");
			sb.append(obs);
			sb.append(" <http://dtp-126.sncs.abdn.ac.uk/quality/CarSensor/Event> ?event . ");
			break;
		case HUMIDITY:
			sb.append(obs);
			sb.append(" ssn:observedProperty qProp:Humidity . ");
			sb.append("?res ssn:hasValue ?val . ");
			sb.append("?val ssn:hasValue ?value . ");
			sb.append(obs);
			sb.append(" <http://dtp-126.sncs.abdn.ac.uk/quality/CarSensor/Event> ?event . ");
			break;
		case GPS:
			sb.append(obs);
			sb.append(" ssn:observedPropert qProp:Location . ");
			sb.append("?res ssn:hasValue ?val . ");
			sb.append("?val geo:lat ?lat . ");
			sb.append("?val geo:long ?lon . ");
			sb.append("?val qual:satellites ?sats . ");
			sb.append("?val qual:precision ?prec . "); 
			sb.append(obs);
			sb.append(" <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Event> ?event . ");
			break;
		case SPEED:
			sb.append(obs);
			sb.append(" ssn:observedProperty qProp:Speed . ");
			sb.append("?res ssn:hasValue ?val . ");
			sb.append("?val ssn:hasValue ?value . ");
			sb.append(obs);
			sb.append(" <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Event> ?event . ");
			break;
		case ACCELERATION:
			sb.append(obs);
			sb.append(" ssn:observedProperty qProp:Acceleration . ");
			sb.append("?res ssn:hasValue ?val . ");
			sb.append("?val ssn:hasValue ?value . ");
			sb.append("?val qual:AccelerationDirection ?dir . ");
			sb.append(obs);
			sb.append(" <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Event> ?event . ");
			break;
		case ALTITUDE:
			sb.append(obs);
			sb.append(" ssn:observedProperty qProp:Altitude . ");
			sb.append("?res ssn:hasValue ?val . ");
			sb.append("?val ssn:hasValue ?value . ");
			sb.append("?val qual:satellites ?sats . ");
			sb.append(obs);
			sb.append(" <http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/Event> ?event . ");
			break;
		}
		
		//Derived observations
		sb.append("OPTIONAL {");
		sb.append(obs);
		sb.append(" prov:wasGeneratedBy ?activity . ");
		sb.append(" ?activity prov:wasAssociatedWith ?agent . ");
		sb.append("}");

		sb.append("}");
		
		return sb.toString();
	}
	
	public ArrayList<Observation> getObservations(String endpoint)
	{		
		System.out.println("Getting observations from " + endpoint);
		ObservationType[] properties = {ObservationType.TEMPERATURE, ObservationType.HUMIDITY, ObservationType.GPS, ObservationType.SPEED, ObservationType.ACCELERATION, ObservationType.ALTITUDE};
		ArrayList<Observation> observations = new ArrayList<Observation>();

		for(int i=0;i<properties.length;i++)
		{
			ObservationType property = properties[i];
			String query = buildQuery("?obs", property);

			try {
				QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, query);
				ResultSet rs = qe.execSelect();
				while(rs.hasNext())
				{
					QuerySolution qs = rs.next();
					
					Resource observation = qs.getResource("obs");
					Resource foi = qs.getResource("foi");
					String prop = props[i];
					Resource sens = qs.getResource("sens");
					Literal rTime = qs.getLiteral("rTime");
					//Literal sTime = qs.getLiteral("sTime");
					Literal event = qs.getLiteral("event");
					
					Resource res = qs.getResource("res");
					Resource val = qs.getResource("val");
					
					Literal value;
					Literal sats;
					
					ArrayList<String> df = getDerivedObservations(observation.getURI());
					
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
												"0",
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
									"0",
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
									"0",
									event.getLexicalForm(),
									df
								);
						
						if(endpoint.contains("Provenance"))
						{
							Resource activity = qs.getResource("activity");
							Resource agent = qs.getResource("agent");
							alObs.activity = activity.getURI();
							alObs.agent = agent.getURI();
						}
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
											"0",
											event.getLexicalForm(),
											df
										 );
						if(endpoint.contains("Provenance"))
						{
							Resource activity = qs.getResource("activity");
							Resource agent = qs.getResource("agent");
							obs.activity = activity.getURI();
							obs.agent = agent.getURI();
						}
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
	
	private void logToDatabase(long reasoningTime, long modelSize, int used_obs_metadata, int used_obs_prov, int used_qual_prov, String observationUri)
	{
		MySQL database = new MySQL(dbUrl, dbUser, dbPass);
		
		PreparedStatement stmt = null;
		
		try{
			stmt = database.getConnection().prepareStatement("INSERT INTO logs (assessment_time, " +
					   "							reasoning_time, " +
					   "							model_size, " +
					   "							inferred_triples, " +
				       "							assessed_dimensions, " +
					   "							used_obs_metadata, " +
					   "							used_obs_prov, " +
					   "							used_qual_prov, " +
					   "							observation_uri)" +
					   " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			stmt.setLong(1,  applicationTime);
			stmt.setLong(2,  reasoningTime);
			stmt.setLong(3, modelSize);
			stmt.setLong(4, inferredTriples);
			stmt.setLong(5, assessedDimensions);
			stmt.setInt(6, used_obs_metadata);
			stmt.setInt(7, used_obs_prov);
			stmt.setInt(8, used_qual_prov);
			stmt.setString(9, observationUri);
			
			if(database.doMySQLInsert(stmt))
				System.out.println("DB: Row inserted");
			else
				System.out.println("DB: Failed to insert row");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private ArrayList<String> getDerivedObservations(String uri)
	{
		ArrayList<String> df = new ArrayList<String>();
		String query = "SELECT * WHERE {" +
						"<" + uri + "> <http://www.w3.org/ns/prov-o/wasDerivedFrom> ?obs . " + 
						"}";
		
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(storename.concat("Provenance"), query);
		ResultSet rs = qe.execSelect();
		
		while(rs.hasNext())
		{
			QuerySolution qs = rs.next();
			Resource obs = qs.getResource("obs");
			df.add(obs.getURI());
		}
		return df;
	}

}
