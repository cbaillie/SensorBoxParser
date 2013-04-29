package uk.ac.dotrural.quality.edsensor.observation;

import java.util.UUID;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class GPSObservation extends Observation {
	
	public String lat = "-999";
	public String lon = "-999";
	public String sat = "0";
	public String prec = "0";
	
	public String GEO = "http://www.w3.org/2003/01/geo/wgs84_pos#";
	
	public GPSObservation(ObservationType property, String time, String lat, String lon, String sat, String prec, String event)
	{
		super(property, time, "0", event);
		setValues(lat, lon, sat, prec);
	}
	
	public GPSObservation(String id, ObservationType property, String foi, String obsBy, String result, String obsVal, String lat, String lon, String sats, String prec, String rTime, String sTime, String event)
	{
		super(id, property, foi, obsBy, result, obsVal, null, rTime, sTime, event);
		this.lat = lat;
		this.lon = lon;
		sat = sats;
		this.prec = prec;
	}
	
	public void setValues(String la, String lo, String s, String p)
	{
		lat = (la.length() > 0 ? la : "0");
		lon = (lo.length() > 0 ? lo : "0");
		sat = (s.length() > 0 ? s : "0");
		prec = (p.length() > 0 ? p : "0");
		
		//System.out.println("Created GPS obs with prec " + p);
	}
	
	public String getObservationValue(String value)
	{
		if(value.equals("lat"))
			return lat;
		if(value.equals("lon"))
			return lon;
		if(value.equals("sat"))
			return sat;
		if(value.equals("prec"))
			return prec;
		return null;
	}
	
	public void describe()
	{
		super.describe();
		System.out.println("LATITUDE: " + lat);
		System.out.println("LONGITUDE: " + lon);
		System.out.println("SATELLITES: " + sat);
		System.out.println("PRECISION: " + prec);
	}
	
	public String getModel(String NS)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("INSERT DATA {\n");
		
		String observationUri = NS + "Observation/" + id;
		String sensorOutputUri = NS + "SensorOutput/" + UUID.randomUUID();
		String observationValueUri = NS + "ObservationValue/" + UUID.randomUUID();
		
		//Observation
		sb.append("\t<" + observationUri + "> a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . \n");
		sb.append("\t<" + observationUri + "> a <http://www.w3.org/ns/prov-o/Entity> . \n");
		sb.append("\t<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#featureOfInterest> <" + NS + "Features/EdJourney1> . \n");
		sb.append("\t<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observationResult> <" + sensorOutputUri + "> . \n");
		sb.append("\t<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observedBy> <" + NS + "Sensor/" + ObservationType.lookup(property) + "> . \n");
		sb.append("\t<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <" + NS + "Property/" + ObservationType.lookup(property) + "> . \n");
		sb.append("\t<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> " + time + " . \n");
		sb.append("\t<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observationServerTime> " + System.currentTimeMillis() + " . \n\n");
		sb.append("\t<" + observationUri + "> <" + NS + "Event> \"" + super.event + "\" . \n\n");
		
		//SensorOutput
		sb.append("\t<" + sensorOutputUri + "> a <http://purl.oclc.org/NET/ssnx/ssn#SensorOutput> . \n");
		sb.append("\t<" + sensorOutputUri + "> a <http://www.w3.org/ns/prov-o/Entity> . \n");
		sb.append("\t<" + sensorOutputUri + "> <http://purl.oclc.org/NET/ssnx/ssn#hasValue> <" + observationValueUri + "> . \n\n");
		
		//ObservationValue
		sb.append("\t<" + observationValueUri + "> a <http://purl.oclc.org/NET/ssnx/ssn#ObservationValue> . \n");
		sb.append("\t<" + observationValueUri + "> a <http://www.w3.org/ns/prov-o/Entity> . \n");
		sb.append("\t<" + observationValueUri + "> <http://www.w3.org/2003/01/geo/wgs84_pos#lat> \"" + lat + "\" . \n");
		sb.append("\t<" + observationValueUri + "> <http://www.w3.org/2003/01/geo/wgs84_pos#long> \"" + lon + "\". \n");
		sb.append("\t<" + observationValueUri + "> <" + NS + "satellites> \"" + sat + "\" . \n");
		sb.append("\t<" + observationValueUri + "> <" + NS + "precision> \"" + prec + "\" . \n");
		
		sb.append("}");
		
		return sb.toString();
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.toString());
		sb.append("Latitude: " + lat + '\n');
		sb.append("Longitude: " + lon + '\n');
		sb.append("Satellites: " + sat + '\n');
		sb.append("Precision: " + prec + '\n');
		
		return sb.toString();
	}
	
	public OntModel getRdfModel(String NS)
	{		
		OntModel sensorObservationModel = ModelFactory.createOntologyModel();
		Resource observationResource = sensorObservationModel.createResource(id);
		Statement observationTypeStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "Observation"));
		Statement observationResultStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observationResult"), sensorObservationModel.createResource(this.result));
		Statement resultTimeStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observationResultTime"), sensorObservationModel.createTypedLiteral(Long.parseLong(this.time)));
		Statement serverTimeStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(NS,"serverTimestamp"), sensorObservationModel.createTypedLiteral(Long.parseLong(sTime)));
		Statement observationResourceEventStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(NS, "Event"), sensorObservationModel.createTypedLiteral(event));
		
		Resource sensor = sensorObservationModel.createResource(obsBy);
		Statement sensorTypeStmt = sensorObservationModel.createStatement(sensor, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "SensingDevice"));
		Statement observedByStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observedBy"), sensor);
		
		Resource property = sensorObservationModel.createResource(NS + "Property/" + ObservationType.lookup(this.property));
		Statement propertyStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observedProperty"), property);
		Statement propertyTypeStmt = sensorObservationModel.createStatement(property, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "Property"));
		
		Resource featureOfInterest = sensorObservationModel.createResource(foi);
		Statement featureOfInterestStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN,"featureOfInterest"), sensorObservationModel.createResource(this.foi));
		Statement featureOfInterestTypeStmt = sensorObservationModel.createStatement(featureOfInterest, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "FeatureOfInterest"));
		
		Resource sensorOutput = sensorObservationModel.createResource(result);
		Statement observationResultTypeStmt = sensorObservationModel.createStatement(sensorOutput, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "SensorOutput"));
		Statement observationValueStmt = sensorObservationModel.createStatement(sensorOutput, sensorObservationModel.createProperty(SSN, "hasValue"), sensorObservationModel.createResource(obsVal));
		
		Resource observationValue = sensorObservationModel.createResource(obsVal);
		Statement observationValueTypeStmt = sensorObservationModel.createStatement(observationValue, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "ObservationValue"));
		//Statement observationValueValueStmt = sensorObservationModel.createStatement(observationValue, sensorObservationModel.createProperty(SSN, "hasValue"), sensorObservationModel.createTypedLiteral(value));
		Statement observationValueLatStmt = sensorObservationModel.createStatement(observationValue, sensorObservationModel.createProperty(GEO, "lat"), sensorObservationModel.createTypedLiteral(lat));
		Statement observationValueLonStmt = sensorObservationModel.createStatement(observationValue, sensorObservationModel.createProperty(GEO, "lon"), sensorObservationModel.createTypedLiteral(lon));
		Statement observationValueSatStmt = sensorObservationModel.createStatement(observationValue, sensorObservationModel.createProperty(NS, "satellites"), sensorObservationModel.createTypedLiteral(sat));
		Statement observationValuePrecStmt = sensorObservationModel.createStatement(observationValue, sensorObservationModel.createProperty(NS, "precision"), sensorObservationModel.createTypedLiteral(prec));
		
		sensorObservationModel.add(observationTypeStmt);
		sensorObservationModel.add(observationResultStmt);
		sensorObservationModel.add(resultTimeStmt);
		sensorObservationModel.add(serverTimeStmt);
		sensorObservationModel.add(featureOfInterestStmt);
		sensorObservationModel.add(featureOfInterestTypeStmt);
		sensorObservationModel.add(sensorTypeStmt);
		sensorObservationModel.add(observedByStmt);
		sensorObservationModel.add(propertyStmt);
		sensorObservationModel.add(propertyTypeStmt);
		sensorObservationModel.add(observationResourceEventStmt);
		sensorObservationModel.add(observationResultTypeStmt);
		sensorObservationModel.add(observationValueStmt);
		sensorObservationModel.add(observationValueTypeStmt);
		sensorObservationModel.add(observationValueLatStmt);
		sensorObservationModel.add(observationValueLonStmt);
		sensorObservationModel.add(observationValueSatStmt);
		sensorObservationModel.add(observationValuePrecStmt);
		
		return sensorObservationModel;
	}

}
