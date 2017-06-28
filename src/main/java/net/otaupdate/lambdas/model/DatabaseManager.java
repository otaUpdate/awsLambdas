package net.otaupdate.lambdas.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.dbutils.DbUtils;

import net.otaupdate.lambdas.util.Logger;


public class DatabaseManager
{
	private static final String DB_HOST = System.getenv("db_endpoint");
	private static final String DB_USERNAME = System.getenv("db_username");
	private static final String DB_PASSWORD = System.getenv("db_password");
	private static final String DB_NAME = "otaUpdates";
	
	private static final int LOGIN_TOKEN_PERIOD_MINS = 60;
	
	
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
		DbUtils.closeQuietly(this.connection);
	}
	
	
	public String getNextFirmwareForFirmwareUuid(String fwUuidIn)
	{
		String retVal = null;
		
		try
		{
			PreparedStatement statement = this.connection.prepareStatement("SELECT * from `firmwareImages` WHERE `uuid`=?");
			statement.setString(1, fwUuidIn);
			ResultSet rs = statement.executeQuery();
			if( !rs.first() ) return null;
			
			retVal = rs.getString("toVersionUuid");
			if( retVal.isEmpty() ) retVal = null;
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		
		if( retVal == null ) Logger.getSingleton().warn(String.format("no target for firmwareUuid '%s'", fwUuidIn));
		
		return retVal;
	}
	
	
	public String getLatestFirmwareForProcessorUuid(String hwUuidIn)
	{
		String retVal = null;
		
		try
		{
			PreparedStatement statement = this.connection.prepareStatement("SELECT * from `processors` WHERE `uuid`=?");
			statement.setString(1, hwUuidIn);
			ResultSet rs = statement.executeQuery();
			if( !rs.first() ) return null;
			
			retVal = rs.getString("latestFirmwareUuid");
			if( retVal.isEmpty() ) retVal = null;
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		
		if( retVal == null ) Logger.getSingleton().warn(String.format("no latest for hardwareUuid '%s'", hwUuidIn));
		
		return retVal;
	}
	
	
	public String getNameFirmwareUuidd(String fwUuidIn)
	{
		String retVal = null;
		
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			statement = this.connection.prepareStatement("SELECT * from `firmwareImages` WHERE `uuid`=?");
			statement.setString(1, fwUuidIn);
			rs = statement.executeQuery();
			if( !rs.first() ) return null;
			
			retVal = rs.getString("name");
			if( retVal.isEmpty() ) retVal = null;
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(statement);
		}
		
		if( retVal == null ) Logger.getSingleton().warn(String.format("no firmware image for '%s'", fwUuidIn));
		
		return retVal;
	}
	
	
	public List<Map<String, Object>> listTableContents(String tableNameIn, String joinClauseIn, String whereClauseIn, String resultColumnsIn)
	{
		List<Map<String, Object>> retVal = new ArrayList<Map<String, Object>>();
		
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			if( resultColumnsIn == null ) resultColumnsIn = "*";
			String sqlQueryString = String.format("SELECT %s from `%s`", resultColumnsIn, tableNameIn);
			if( joinClauseIn != null ) sqlQueryString += joinClauseIn;
			if( whereClauseIn != null ) sqlQueryString += whereClauseIn;
			Logger.getSingleton().debug(String.format("sqlQuery: '%s", sqlQueryString));
			
			statement = this.connection.prepareStatement(sqlQueryString);
			rs = statement.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			while( rs.next() )
			{
				Map<String, Object> currEntry = new HashMap<String, Object>();
				for( int i = 1; i <= rsmd.getColumnCount(); i++ )
				{
					currEntry.put(rsmd.getColumnName(i), rs.getObject(i));
				}
				retVal.add(currEntry);
			}
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public boolean insertSelect(String insertTableNameIn, String columnNamesIn, String columnValuesIn, 
								String selectTableNameIn, String joinClauseIn, String whereClauseIn)
	{
		boolean retVal = false;
		
		PreparedStatement statement = null;
		try
		{
			String sqlQueryString = String.format("INSERT INTO `%s` %s SELECT %s FROM %s", 
					insertTableNameIn, columnNamesIn, columnValuesIn, selectTableNameIn);
			if( joinClauseIn != null ) sqlQueryString += joinClauseIn;
			if( whereClauseIn != null ) sqlQueryString += whereClauseIn;
			Logger.getSingleton().debug(String.format("sqlQuery: '%s", sqlQueryString));
			
			statement = this.connection.prepareStatement(sqlQueryString);
			retVal = (statement.executeUpdate() > 0);
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public boolean insert(String insertTableNameIn, String columnNamesIn, String columnValuesIn)
	{
		boolean retVal = false;

		PreparedStatement statement = null;
		try
		{
			String sqlQueryString = String.format("INSERT INTO `%s` %s VALUES %s", 
					insertTableNameIn, columnNamesIn, columnValuesIn);
			Logger.getSingleton().debug(String.format("sqlQuery: '%s", sqlQueryString));

			statement = this.connection.prepareStatement(sqlQueryString);
			retVal = (statement.executeUpdate() > 0);
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(statement);
		}

		return retVal;
	}
	
	
	public boolean updateInTable(String tableNameIn, String joinClauseIn, String setClauseIn, String whereClauseIn)
	{
		boolean retVal = false;
		
		PreparedStatement statement = null;
		try
		{
			String sqlQueryString = String.format("UPDATE `%s`", tableNameIn);
			if( joinClauseIn != null ) sqlQueryString += joinClauseIn;
			if( setClauseIn != null ) sqlQueryString += setClauseIn;
			if( whereClauseIn != null ) sqlQueryString += whereClauseIn;
			Logger.getSingleton().debug(String.format("sqlQuery: '%s", sqlQueryString));
			
			statement = this.connection.prepareStatement(sqlQueryString);
			retVal = (statement.executeUpdate() > 0);
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public boolean deleteFromTable(String tableNameIn, String joinClauseIn, String whereClauseIn)
	{

		boolean retVal = false;
		
		PreparedStatement statement = null;
		try
		{
			String sqlQueryString = String.format("DELETE %s from `%s`", tableNameIn, tableNameIn);
			if( joinClauseIn != null ) sqlQueryString += joinClauseIn;
			if( whereClauseIn != null ) sqlQueryString += whereClauseIn;
			Logger.getSingleton().debug(String.format("sqlQuery: '%s", sqlQueryString));
			
			statement = this.connection.prepareStatement(sqlQueryString);
			retVal = (statement.executeUpdate() > 0);
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public boolean addUser(String emailAddressIn, String passwordIn)
	{
		boolean retVal = false;
		
		PreparedStatement statement = null;
		try
		{
			String salt = UUID.randomUUID().toString();
			
			statement = this.connection.prepareStatement("INSERT INTO `users`(email, passwordHash, salt) VALUES (?, ENCRYPT(?, ?), ?)");
			statement.setString(1, emailAddressIn);
			statement.setString(2, passwordIn);
			statement.setString(3, salt);
			statement.setString(4, salt);
			retVal = statement.executeUpdate() == 1;
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public boolean doesUserExist(String emailAddressIn)
	{
		boolean retVal = false;
		
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			statement = this.connection.prepareStatement("SELECT * FROM `users` WHERE email=?");
			statement.setString(1, emailAddressIn);
			rs = statement.executeQuery();
			retVal = rs.first();
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public String authorizeUser(String emailAddressIn, String passwordIn)
	{
		String retVal = null;
		
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			// first we have to get the user row for the salt
			statement = this.connection.prepareStatement("SELECT * FROM `users` WHERE email=?");
			statement.setString(1, emailAddressIn);
			rs = statement.executeQuery();
			if( !rs.next() ) return null;
			
			String salt = rs.getString("salt");
			if( salt == null ) return null;
			
			// release our previous statement and result set
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(statement);
			
			// now we need to see if the hashed password is correct
			statement = this.connection.prepareStatement("SELECT * FROM `users` WHERE email=? AND passwordHash=ENCRYPT(?, ?)");
			statement.setString(1, emailAddressIn);
			statement.setString(2, passwordIn);
			statement.setString(3,  salt);
			boolean loginSuccessful = statement.executeQuery().first();
			
			// release our previous statement and result set
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(statement);
			
			// update the database accordingly
			if( loginSuccessful)
			{
				// login successful...now we need to generate a login token
				String loginToken = UUID.randomUUID().toString();
				statement = this.connection.prepareStatement("UPDATE `users` SET loginToken=?, loginTokenCreation=NOW() WHERE email=?");
				statement.setString(1, loginToken);
				statement.setString(2, emailAddressIn);
				if( statement.executeUpdate() > 0 ) retVal = loginToken;
			}
			else
			{
				// login failed...invalidate any previous tokens
				statement = this.connection.prepareStatement("UPDATE `users` SET loginToken=NULL WHERE email=?");
				statement.setString(1, emailAddressIn);
				statement.executeUpdate();
			}
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public Integer getUserIdForLoginToken(String authTokenIn)
	{
		Integer retVal = null;
		
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			statement = this.connection.prepareStatement("SELECT * FROM `users` WHERE loginToken=? and loginTokenCreation > NOW() - INTERVAL ? MINUTE");
			statement.setString(1, authTokenIn);
			statement.setInt(2, LOGIN_TOKEN_PERIOD_MINS);
			rs = statement.executeQuery();
			if( rs.first() )
			{
				retVal = new Integer(rs.getInt("id"));
			}
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public boolean addUserToOrganization(String emailAddressIn, String organizationUuidIn)
	{
		boolean retVal = false;
		
		PreparedStatement statement = null;
		try
		{
			statement = this.connection.prepareStatement("INSERT INTO `organizationUserMap` (userId, organizationUuid) VALUES((SELECT id FROM `users` WHERE email=?), ?)");
			statement.setString(1, emailAddressIn);
			statement.setString(2, organizationUuidIn);
			retVal = statement.executeUpdate() == 1;
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public boolean addUserToOrganization(Integer userIdIn, String organizationUuidIn)
	{
		boolean retVal = false;
		
		PreparedStatement statement = null;
		try
		{
			statement = this.connection.prepareStatement("INSERT INTO `organizationUserMap` (userId, organizationUuid) VALUES(?, ?)");
			statement.setInt(1, userIdIn);
			statement.setString(2, organizationUuidIn);
			retVal = statement.executeUpdate() == 1;
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public boolean isUserPartOfOrganization(Integer userIdIn, String organizationUuidIn)
	{
		boolean retVal = false;
		
		PreparedStatement statement = null;
		try
		{
			statement = this.connection.prepareStatement("SELECT * FROM `organizationUserMap` WHERE userId=? AND organizationUuid=?");
			statement.setInt(1, userIdIn);
			statement.setString(2, organizationUuidIn);
			retVal = statement.executeQuery().first();
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public List<Map<String, String>> listUsersInOrganization(String organizationUuidIn)
	{
		List<Map<String, String>> retVal = new ArrayList<Map<String, String>>();
		
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			statement = this.connection.prepareStatement("SELECT users.email FROM `users` JOIN `organizationUserMap` ON users.id=organizationUserMap.userId AND organizationUserMap.organizationUuid=?");
			statement.setString(1, organizationUuidIn);
			rs = statement.executeQuery();
			while( rs.next() )
			{
				Map<String, String> newItem = new HashMap<String, String>();
				newItem.put("email", rs.getString("email"));
				retVal.add(newItem);
			}
		}
		catch( Exception e )
		{
			retVal = null;
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public boolean removeUserFromOrganization(String emailAddressIn, String organizationUuidIn)
	{
		boolean retVal = false;
		
		PreparedStatement statement = null;
		try
		{
			statement = this.connection.prepareStatement("DELETE `organizationUserMap` FROM `organizationUserMap` JOIN `users` ON organizationUserMap.userId=users.id WHERE users.email=? AND organizations.uuid=?");
			statement.setString(1, emailAddressIn);
			statement.setString(2, organizationUuidIn);
			retVal = statement.executeUpdate() == 1;
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public String insertFirmwareImageGetUuid(String nameIn, String processorUuidIn, String deviceUuidIn, String organizationUuidIn)
	{
		String retVal = null;
		
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			// first we need to make sure the processor/device/organization tree is OK
			statement = this.connection.prepareStatement("SELECT processors.uuid FROM `processors` "
														+ "JOIN `devices` ON processors.deviceUuid=devices.uuid "
														+ "WHERE processors.uuid=? AND devices.uuid=? AND devices.organizationUuid=?");
			statement.setString(1, processorUuidIn);
			statement.setString(2, deviceUuidIn);
			statement.setString(3, organizationUuidIn);
			if( !((rs = statement.executeQuery()).first()) ) return null;
			
			// release our previous statement and result set
			DbUtils.close(rs);
			DbUtils.closeQuietly(statement);
			
			// processors/device/organization tree is OK...insert the firmware image
			String uuid = UUID.randomUUID().toString();
			statement = this.connection.prepareStatement("INSERT INTO `firmwareImages` (uuid, name, processorUuid) VALUES (?, ?, ?)");
			statement.setString(1, uuid);
			statement.setString(2, nameIn);
			statement.setString(3, processorUuidIn);
			if( statement.executeUpdate() != 1 ) return null;
			
			// if we made it here, the new firmware image has been created successfully
			retVal = uuid;
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
	
	
	public boolean deleteFirmwareImage(String fwUuidIn, String processorUuidIn, String deviceUuidIn, String organizationUuidIn)
	{
		boolean retVal = false;
		
		PreparedStatement statement = null;
		try
		{
			// first we need to make sure the processor/device/organization tree is OK
			statement = this.connection.prepareStatement("DELETE firmwareImages FROM `firmwareImages` "
													   + "JOIN `processors` ON firmwareImages.processorUuid=processors.uuid "
													   + "JOIN `devices` ON processors.deviceUuid=devices.uuid "
													   + "JOIN `organizationUserMap` ON devices.organizationUuid=organizationUserMap.organizationUuid "
													   + "WHERE firmwareImages.uuid=? "
													   + "AND processors.uuid=? "
													   + "AND devices.uuid=? "
													   + "AND devices.organizationUuid=?");
			statement.setString(1, fwUuidIn);
			statement.setString(2, processorUuidIn);
			statement.setString(3, deviceUuidIn);
			statement.setString(4, organizationUuidIn);
			retVal = (statement.executeUpdate() == 1 );
		}
		catch( Exception e )
		{
			Logger.getSingleton().error(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(statement);
		}
		
		return retVal;
	}
}
