package uk.ac.dotrural.quality.edsensor;

import java.util.ArrayList;

import uk.ac.dotrural.quality.edsensor.observation.AltitudeObservation;
import uk.ac.dotrural.quality.edsensor.observation.Observation;
import uk.ac.dotrural.quality.edsensor.observation.ObservationType;

public class ProvenanceCreator {
	
	private ArrayList<Observation> provenance = new ArrayList<Observation>();
	
	private String file = "WeatherStation";
	
	public String filename = file +".csv";
	public String filepath = "resource/" + filename;
	
	public String storename = "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/" + file;
	public String endpoint = storename.concat("/statements");
	
	public static void main(String[] args)
	{
		new ProvenanceCreator();
	}
	
	public ProvenanceCreator()
	{
		EdSensorAssessor esa = new EdSensorAssessor(storename);
		ArrayList<Observation> obs = esa.getObservations(storename);
		fixErrors(obs);
		
		EdSensor es = new EdSensor(file.concat("Provenance"), provenance);
		es.storeObservations(provenance);
	}
	
	private void fixErrors(ArrayList<Observation> obs)
	{
		for(int i=0;i<obs.size();i++)
		{
			Observation o = (Observation)obs.get(i);
			switch(o.property)
			{
			case ACCELERATION:
				break;
			case GPS:
				break;
			case ALTITUDE:
				AltitudeObservation aobs = (AltitudeObservation)o;
				if(Double.parseDouble(aobs.value) < 0.0)
				{					
					Observation o1 = findPrevious(obs, aobs.property, i);
					Observation o2 = findNext(obs, aobs.property, i);
					
					if(o1 != null && o2 != null)
					{
						AltitudeObservation newObs = (AltitudeObservation)createDerivedObs(o, o1, o2, o.property);
						provenance.add(newObs);
						
						obs.remove(i);
						obs.add(i, newObs);
					}
				}
				break;
			case SPEED:
				break;
			default:
				if(Double.parseDouble(o.value) < 1.0)
				{
					Observation o1 = findPrevious(obs, o.property, i);
					Observation o2 = findNext(obs, o.property, i);
					
					if(o1 != null && o2 != null)
					{
						Observation newObs = createDerivedObs(o, o1, o2, o.property);
						provenance.add(newObs);
					
						obs.remove(i);
						obs.add(i, newObs);
					}
				}
				break;
			}
		}
	}
	
	private Observation findPrevious(ArrayList<Observation> obs, ObservationType prop, int index)
	{
		switch(prop)
		{
		default:
			
			for(int i=(index-1);i>0;i--)
			{
				Observation o = (Observation)obs.get(i);
				if(o.property == prop && (Double.parseDouble(o.value) > 0))
				{
					return o;
				}
			}
			break;
		}
		System.out.println("Couldn't find previous...");
		return null;
	}
	
	private Observation findNext(ArrayList<Observation> obs, ObservationType prop, int index)
	{
		switch(prop)
		{
		default:
			for(int i=(index+1);i>0;i++)
			{
				Observation o = (Observation)obs.get(i);
				if(o.property == prop && (Double.parseDouble(o.value) > 0))
				{
					return o;
				}
			}
			break;
		}
		System.out.println("Couldn't find next...");
		return null;
	}
	
	private Observation createDerivedObs(Observation o0, Observation o1, Observation o2, ObservationType type)
	{
		Long t3 = 0L;
		ArrayList<String> df = new ArrayList<String>();
		switch(type){
		case ALTITUDE:
			Double alt = ((Double.parseDouble(o1.value) + Double.parseDouble(o2.value)) / 2.0);
			int sats = 0;
			t3 = (((Long.parseLong(o1.time) / 1000) + (Long.parseLong(o2.time) / 1000)) / 2) * 1000;
			
			df = new ArrayList<String>();
			df.add(o0.id);
			df.add(o1.id);
			df.add(o2.id);
			
			AltitudeObservation aobs = new AltitudeObservation(
					type,
					o1.foi,
					"ProvenanceCreator.java",
					"" + alt,
					"" + sats,
					"" + t3,
					o1.event,
					df
					);
			
			return aobs;
		default:
			Double temp = ((Double.parseDouble(o1.value) + Double.parseDouble(o2.value)) / 2.0);
			t3 = (((Long.parseLong(o1.time) / 1000) + (Long.parseLong(o2.time) / 1000)) / 2) * 1000;
			
			df = new ArrayList<String>();
			df.add(o0.id);
			df.add(o1.id);
			df.add(o2.id);
			Observation obs = new Observation(
					type,
					o1.foi,
					"ProvenanceCreator.java",
					"" + temp,
					"" + t3,
					o1.event,
					df
				 );
			
			return obs;
		}
	}
}
