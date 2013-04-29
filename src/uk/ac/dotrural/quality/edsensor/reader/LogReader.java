package uk.ac.dotrural.quality.edsensor.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class LogReader {
	
	public ArrayList<String> readLog(String file)
	{
		ArrayList<String> lines = new ArrayList<String>();
		if(file != null)
		{
			try
			{
				String line = "";
				BufferedReader br = new BufferedReader(new FileReader(file));
				while((line = br.readLine()) != null)
				{
					lines.add(line);
				}
				br.close();
			}
			catch(Exception ex)
			{
				System.out.println(ex.toString());
			}
		}
		return lines;
	}

}
