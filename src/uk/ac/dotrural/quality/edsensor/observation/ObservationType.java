package uk.ac.dotrural.quality.edsensor.observation;

public enum ObservationType {
	
	ACCELERATION, HUMIDITY, TEMPERATURE, GPS, SPEED, ALTITUDE;
	
	/*private static String acceleration = "http://dbpedia.org/resource/Acceleration";
	private static String humidity = "http://dbpedia.org/resource/Humidity";
	private static String temperature = "http://dbpedia.org/resource/Temperature";
	private static String location = "http://dbpedia.org/resource/Location_(geography)";*/
	
	public static String lookup(ObservationType type)
	{
		switch(type)
		{
		case ACCELERATION:
			return "Acceleration";
		case HUMIDITY:
			return "Humidity";
		case TEMPERATURE:
			return "Temperature";
		case GPS:
			return "Location";
		case SPEED:
			return "Speed";
		case ALTITUDE:
			return "Altitude";
		}
		return null;
	}
	
	public static ObservationType strToObsType(String str)
	{
		if(str.equals("Acceleration"))
			return ObservationType.ACCELERATION;
		if(str.equals("Humidity"))
			return ObservationType.HUMIDITY;
		if(str.equals("Temperature"))
			return ObservationType.TEMPERATURE;
		if(str.equals("Location"))
			return ObservationType.GPS;
		if(str.equals("Speed"))
			return ObservationType.SPEED;
		if(str.equals("Altitude"))
			return ObservationType.ALTITUDE;
		return null;
	}

}
