package uk.ac.dotrural.quality.edsensor.agent;

public class AgentFactory {
	
	private static int index = 0;
	private static String[] agents = {"Chris", "Pete", "Ed"};
	
	public static String getAgent()
	{
		String agent = agents[index];
		
		if(index < 2)
			index++;
		else
			index = 0;
		
		return agent;
	}

}
