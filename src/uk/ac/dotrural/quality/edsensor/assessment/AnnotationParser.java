package uk.ac.dotrural.quality.edsensor.assessment;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public class AnnotationParser {
	
	public int countResults(Model results)
	{
		int count = 0;

		String select = "SELECT * WHERE {" +
				"	?result a <http://abdn.ac.uk/~r01ccb9/Qual-O/Result> . " +
				"}";

		QueryExecution qe = QueryExecutionFactory.create(select, results);

		try
		{
			ResultSet rs = qe.execSelect();
			while(rs.hasNext())
			{
				rs.next();				
				count++;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return count;
	}
	
}
