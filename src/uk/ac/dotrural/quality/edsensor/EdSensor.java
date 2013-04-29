package uk.ac.dotrural.quality.edsensor;

import java.util.ArrayList;

import uk.ac.dotrural.quality.edsensor.observation.*;
import uk.ac.dotrural.quality.edsensor.parser.LineParser;
import uk.ac.dotrural.quality.edsensor.reader.LogReader;
import uk.ac.dotrural.quality.edsensor.sparql.Updater;

public class EdSensor {
	
	public String file = "DalekMarch";
	public String filename = file + ".csv";
	public String filepath = "resource/" + filename;
	
	private final String NS = "http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/";
	
	public String storename = "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/" + file;
	public String endpoint = storename.concat("/statements");
	
	private int querySent = 0;
	
	private boolean sendLocation = true;
	
	ArrayList<Observation> observations;
	
	public static void main(String[] args) {
		new EdSensor();
	}
	
	public EdSensor()
	{
		run();
	}
	
	public EdSensor(String file, ArrayList<Observation> provenance)
	{
		this.file = file;
		observations = provenance;
		storename = "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/".concat(file);
		endpoint = storename.concat("/statements");
	}
	
	private void run()
	{
		System.out.println("Starting");
		System.out.println("===================");
		System.out.println("== Parsing File ===");

		observations = parseLogFile();
		
		System.out.println("=== File parsed ===");
		
		System.out.println("=== Storing Obs ===");
		storeObservations(observations);
		
		System.out.println("===================");
		System.out.println("Finished");
	}

	public ArrayList<Observation> parseLogFile()
	{
		LogReader reader = new LogReader();
		ArrayList<Observation> observations = new ArrayList<Observation>();
		ArrayList<String> lines = reader.readLog(filepath);
		System.out.println("== Log File Read ==");
		lines.remove(0);
		
		LineParser lp = new LineParser();
		
		for(int i=0;i<lines.size();i++)
		{
			ArrayList<Observation> obs = lp.parse((String)lines.get(i));
			for(int j=0;j<obs.size();j++)
			{
				observations.add(obs.get(j));
			}
		}
		return observations;
	}
	
	public void storeObservations(ArrayList<Observation> observations)
	{
		String query = "";
		try
		{
			for(int i=0;i<observations.size();i++)
			{
				query = "";
				Observation obs = (Observation)observations.get(i);
				switch(obs.property)
				{
				case ACCELERATION:
					AccelerometerObservation acc = (AccelerometerObservation)obs;
					query = acc.getModel(NS);				
					break;
				case GPS:
					if(sendLocation)
					{
						GPSObservation gps = (GPSObservation)obs;
						query = gps.getModel(NS);
					}
					break;
				case ALTITUDE:
					AltitudeObservation alt = (AltitudeObservation)obs;
					query = alt.getModel(NS);
					break;
				default:
					query = obs.getModel(NS);
					break;
				}
				if(query.length() > 0)
				{
					Updater update = new Updater();
					update.sendUpdate(endpoint, this, query);
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void status()
	{
		querySent++;
		System.out.println(querySent + " of " + observations.size() + " sent.");
	}
	
}
