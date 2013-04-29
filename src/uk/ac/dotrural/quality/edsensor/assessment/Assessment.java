package uk.ac.dotrural.quality.edsensor.assessment;

import java.util.ArrayList;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

import uk.ac.dotrural.quality.edsensor.observation.Observation;

public class Assessment {
	
	private final String ENDPOINT = "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/EdSensor";
	
	public static void main(String[] args)
	{
		new Assessment();
	}
	
	public Assessment()
	{
		ArrayList<Observation> observations = getObservations();
		
		for(int i=0;i<observations.size();i++)
		{
			
		}
	}
	
	private ArrayList<Observation> getObservations()
	{
		ArrayList<Observation> observations = new ArrayList<Observation>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * WHERE {");
		sb.append("	?obs a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ");
		sb.append("}");
		
		try
		{
			QueryExecution qe = QueryExecutionFactory.sparqlService(ENDPOINT, sb.toString());
			ResultSet rs = qe.execSelect();
			while(rs.hasNext())
			{
				QuerySolution qs = rs.next();
				Resource observation = qs.getResource("obs");
				observations.add(getObservation(observation.getURI()));
			}
		}
		catch(Exception ex)
		{
			System.out.println("getObservations Exception: " + ex.toString());
		}
		
		return observations;
	}
	
	private Observation getObservation(String uri)
	{
		uri = "<" + uri + ">";
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ?property ?time ?value WHERE {");
		sb.append(uri);
		sb.append(" a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . ");
		sb.append(uri);
		sb.append(" <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> ?property . ");
		sb.append(uri);
		sb.append(" <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?time . ");
		sb.append(uri);
		sb.append(" <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?so . ");
		sb.append("?so <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?ov . ");
		sb.append("?ov <http://purl.oclc.org/NET/ssnx/ssn#hasValue> ?value . ");
		sb.append("}");
		
		try
		{
			QueryExecution qe = QueryExecutionFactory.sparqlService(ENDPOINT, sb.toString());
			ResultSet rs = qe.execSelect();
			while(rs.hasNext())
			{
				
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	//private ObservationType parseProperty(String property)
	//{
		//return ObservationType.strToObsType(property);
	//}

}
