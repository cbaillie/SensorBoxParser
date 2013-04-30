package uk.ac.dotrural.quality.edsensor.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class MySQL {
	
	private String url, user, password;
	private Connection con = null;
	
	public MySQL(String url, String user, String password)
	{
		this.url = url;
		this.user = user;
		this.password = password;
	}
	
	public Connection getConnection()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, user, password);
			System.out.println("[MySQLLogger] Connection created");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}		
		return con;
	}
	
	/**
	 * Perform an update query
	 * 
	 * @param updateQuery The query to be executed
	 * @return boolean indicating success
	 */
	public boolean doMySQLInsert(PreparedStatement stmt)
	{
		con = getConnection();
		try
		{
			int result = stmt.executeUpdate();
			con.close();
			if(result > 0)
				return true;
		}
		catch(Exception ex)
		{
			System.out.println("[MySQLLogger] doMySQLInsert : Update failed");
			ex.printStackTrace();
		}
		return false;
	}
	
	public void closeConnection()
	{
		try
		{
			con.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
