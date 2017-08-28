package net.otaupdate.lambdas.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.commons.dbutils.DbUtils;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import net.otaupdate.lambdas.model.db.otaupdates.tables.Apikeys;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devicetypes;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareimages;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizationusermap;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.util.Logger;


public class DatabaseManager
{
	private static final String TAG = DatabaseManager.class.getSimpleName();
	private static final String DB_HOST = System.getenv("db_endpoint");
	private static final String DB_USERNAME = System.getenv("db_username");
	private static final String DB_PASSWORD = System.getenv("db_password");
	private static final String DB_NAME = "otaUpdates";


	private final Connection connection;
	private DSLContext dslContext = null;


	public DatabaseManager() throws SQLException
	{
		// Setup the connection with the DB
		Logger.getSingleton().debug(TAG, "connecting to database...");
		this.connection = DriverManager.getConnection(String.format("jdbc:mysql://%s/%s", DB_HOST, DB_NAME), DB_USERNAME, DB_PASSWORD);
		Logger.getSingleton().debug(TAG, "connected to database");
	}


	public DSLContext getDslContext()
	{
		if( this.dslContext == null )
		{
			this.dslContext = DSL.using(this.connection, SQLDialect.MYSQL);
		}

		return this.dslContext;
	}


	public void close()
	{
		if( this.dslContext != null ) this.dslContext.close();
		DbUtils.closeQuietly(this.connection);
	}
	
	
	public UInteger getOrganizationIdForUuid(String orgUuidIn)
	{
		if( orgUuidIn == null ) return null;
		
		Result<Record1<UInteger>> result =
				this.getDslContext().select(Organizations.ORGANIZATIONS.ID)
				.from(Organizations.ORGANIZATIONS)
				.where(Organizations.ORGANIZATIONS.UUID.eq(orgUuidIn))
				.limit(1)
				.fetch();
		if( result.size() < 1 ) return null;
		
		return result.get(0).getValue(Organizations.ORGANIZATIONS.ID);
	}
	
	
	public UInteger getOrganizationIdForApiKey(String apiKeyIn)
	{
		if( apiKeyIn == null ) return null;
		
		Result<Record1<UInteger>> result =
				this.getDslContext().select(Apikeys.APIKEYS.ORGID)
				.from(Apikeys.APIKEYS)
				.where(Apikeys.APIKEYS.KEY.eq(apiKeyIn))
				.limit(1)
				.fetch();
		if( result.size() < 1 ) return null;
		
		return result.get(0).getValue(Apikeys.APIKEYS.ORGID);
	}
	
	
	public UInteger getDevTypeIdForUuid(String devTypeUuidIn)
	{
		if( devTypeUuidIn == null ) return null;
		
		Result<Record1<UInteger>> result =
				this.getDslContext().select(Devicetypes.DEVICETYPES.ID)
				.from(Devicetypes.DEVICETYPES)
				.where(Devicetypes.DEVICETYPES.UUID.eq(devTypeUuidIn))
				.limit(1)
				.fetch();
		if( result.size() < 1 ) return null;
		
		return result.get(0).getValue(Devicetypes.DEVICETYPES.ID);
	}
	
	
	public UInteger getProcTypeIdForUuid(String procTypeUuidIn)
	{
		if( procTypeUuidIn == null ) return null;
		
		Result<Record1<UInteger>> result =
				this.getDslContext().select(Processortypes.PROCESSORTYPES.ID)
				.from(Processortypes.PROCESSORTYPES)
				.where(Processortypes.PROCESSORTYPES.UUID.eq(procTypeUuidIn))
				.limit(1)
				.fetch();
		if( result.size() < 1 ) return null;
		
		return result.get(0).getValue(Processortypes.PROCESSORTYPES.ID);
	}
	
	
	public UInteger getFirmwareImageIdForUuid(String fwImageUuidIn)
	{
		if( fwImageUuidIn == null ) return null;
		
		Result<Record1<UInteger>> result =
				this.getDslContext().select(Firmwareimages.FIRMWAREIMAGES.ID)
				.from(Firmwareimages.FIRMWAREIMAGES)
				.where(Firmwareimages.FIRMWAREIMAGES.UUID.eq(fwImageUuidIn))
				.limit(1)
				.fetch();
		if( result.size() < 1 ) return null;
		
		return result.get(0).getValue(Processortypes.PROCESSORTYPES.ID);
	}
	
	
	public String getFirmwareImageUuidForId(UInteger fwIdIn)
	{
		if( fwIdIn == null ) return null;
		
		Result<Record1<String>> result =
				this.getDslContext().select(Firmwareimages.FIRMWAREIMAGES.UUID)
				.from(Firmwareimages.FIRMWAREIMAGES)
				.where(Firmwareimages.FIRMWAREIMAGES.ID.eq(fwIdIn))
				.limit(1)
				.fetch();
		if( result.size() < 1 ) return null;
		
		return result.get(0).getValue(Firmwareimages.FIRMWAREIMAGES.UUID);
	}


	public boolean addUserToOrganization(String awsSubIn, UInteger orgIdIn)
	{
		if( (awsSubIn == null) || awsSubIn.isEmpty() || (orgIdIn == null) ) return false;
		
		int numRecordsModified = this.getDslContext().insertInto(Organizationusermap.ORGANIZATIONUSERMAP, 
				Organizationusermap.ORGANIZATIONUSERMAP.AWSSUB, Organizationusermap.ORGANIZATIONUSERMAP.ORGANIZATIONID)
				.values(awsSubIn, orgIdIn)
				.execute();
		return (numRecordsModified == 1);
	}


	public static Timestamp getNow()
	{
		return new Timestamp(Calendar.getInstance().getTime().getTime());
	}
}
