package uk.ac.dotrural.quality.edsensor.assessment;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class AnnotationParser {
	
	public int countResults(Model results)
	{
		int count = 0;

		String select = "SELECT * WHERE {" +
				"	?result a <http://abdn.ac.uk/~r01ccb9/Qual-O/Result> . " +
				"	?result <http://abdn.ac.uk/~r01ccb9/Qual-O/basedOn> ?metric . " + 
				"	?result <http://abdn.ac.uk/~r01ccb9/Qual-O/hasScore> ?score" + 
				"}";

		QueryExecution qe = QueryExecutionFactory.create(select, results);

		try
		{
			ResultSet rs = qe.execSelect();
			while(rs.hasNext())
			{
				QuerySolution qs = rs.next();
				
				Resource metric = qs.getResource("metric");
				Literal score = qs.getLiteral("score");
				
				System.out.println("Result for " + metric.getLocalName() + "; score: " + score.getLexicalForm());
				
				count++;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		if(count == 0)
			System.out.println("Couldn't parse results...");
		
		return count;
	}
	
}
