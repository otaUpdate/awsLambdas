package net.otaupdate.lambdas.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.otaupdate.lambdas.util.Logger;


public class DatabaseManager
{
	public static final String DB_HOST = System.getenv("db_endpoint");
	public static final String DB_USERNAME = System.getenv("db_username");
	public static final String DB_PASSWORD = System.getenv("db_password");
	public static final String DB_NAME = "otaUpdates";
	
	
	private final Connection connection;
	
	
	public DatabaseManager() throws SQLException
	{
        // Setup the connection with the DB
		Logger.getSingleton().debug("connecting to database...");
        this.connection = DriverManager.getConnection(String.format("jdbc:mysql://%s/%s", DB_HOST, DB_NAME), DB_USERNAME, DB_PASSWORD);
        Logger.getSingleton().debug("connected to database");
	}
	
	
	public void close()
	{
		try { this.connection.close(); } catch( Exception e ) { }
	}
	
	
	public String getLatestFirmwareUuid(FirmwareIdentifier fiIn)
	{
		String retVal = null;
		
		try
		{
			PreparedStatement statement = this.connection.prepareStatement("SELECT * from `migrationPaths` WHERE `fromVersionUuid`=?");
			statement.setString(1, fiIn.getFirmwareUuid());
			ResultSet rs = statement.executeQuery();
			while( rs.next() )
			{
				retVal = rs.getString("toVersionUuid");
				if( retVal != null ) break; 
			}
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		
		if( retVal == null ) Logger.getSingleton().warn(String.format("no target for firmwareUuid '%s'", fiIn.toString()));
		
		return retVal;
	}
	
	
	public String getLatestFirmwareUuid(HardwareIdentifier hiIn)
	{
		String retVal = null;
		
		try
		{
			PreparedStatement statement = this.connection.prepareStatement("SELECT * from `hardware` WHERE `hardwareUuid`=?");
			statement.setString(1, hiIn.getHardwareUuid());
			ResultSet rs = statement.executeQuery();
			while( rs.next() )
			{
				retVal = rs.getString("latestFirmwareUuid");
				if( retVal != null ) break;
			}
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		
		if( retVal == null ) Logger.getSingleton().warn(String.format("no latest for hardwareUuid '%s'", hiIn.toString()));
		
		return retVal;
	}
	
	
	public DownloadableFirmwareImage getDownloadableFirmwareImageForFirmwareId(FirmwareIdentifier fiIn)
	{
		DownloadableFirmwareImage retVal = null;
		
		try
		{
			PreparedStatement statement = this.connection.prepareStatement("SELECT * from `firmwareImages` WHERE `versionUuid`=?");
			statement.setString(1, fiIn.getFirmwareUuid());
			ResultSet rs = statement.executeQuery();
			while( rs.next() )
			{
				String name = rs.getString("name");
				if( name == null ) continue;
				
				String uuid = rs.getString("versionUuid");
				if( uuid == null ) continue;
				
				String s3bucket = rs.getString("s3bucket");
				if( s3bucket == null ) continue;
				
				String s3key = rs.getString("s3key");
				if( s3key == null ) continue;
				
				retVal = new DownloadableFirmwareImage(name, uuid, s3bucket, s3key);
				break;
			}
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		
		if( retVal == null ) Logger.getSingleton().warn(String.format("no firmware image for '%s'", fiIn.toString()));
		
		return retVal;
	}
}
