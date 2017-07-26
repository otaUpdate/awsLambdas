package net.otaupdate.lambdas.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.dbutils.DbUtils;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import net.otaupdate.lambdas.model.db.otaupdates.tables.Devicetypes;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareimages;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizationusermap;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Users;
import net.otaupdate.lambdas.util.Logger;


public class DatabaseManager
{
	public static final String TABLE_ORGANIZATIONS = "organizations";
	public static final String TABLE_ORG_USER_MAP = "organizationUserMap";
	public static final String TABLE_USERS = "users";
	public static final String TABLE_FWUPDATE_HISTORY = "fwUpdateHistory";
	public static final String TABLE_DEVICE_TYPES = "deviceTypes";
	public static final String TABLE_PROCESSOR_TYPES = "processorTypes";
	public static final String TABLE_FIRMWARE_IMAGES = "firmwareImages";

	private static final String TAG = DatabaseManager.class.getSimpleName();
	private static final String DB_HOST = System.getenv("db_endpoint");
	private static final String DB_USERNAME = System.getenv("db_username");
	private static final String DB_PASSWORD = System.getenv("db_password");
	private static final String DB_NAME = "otaUpdates";

	private static final int LOGIN_TOKEN_PERIOD_MINS = 60;


	public interface DatabaseQueryHelper<T>
	{
		public T executeQueryReturnResult(DSLContext create);
	}


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


	public UInteger getUserIdForLoginToken(String authTokenIn)
	{
		UInteger retVal = null;

		Result<Record2<UInteger, Timestamp>> result = this.getDslContext().select(Users.USERS.ID, Users.USERS.LOGINTOKENCREATION)
				.from(Users.USERS)
				.where(Users.USERS.LOGINTOKEN.eq(authTokenIn))
				.limit(1)
				.fetch();

		if( result.size() > 0 )
		{
			// got a result...check the timestamp
			Timestamp ts = result.get(0).getValue(Users.USERS.LOGINTOKENCREATION);
			long timeDiff_ms = ((new Date()).getTime() - ts.getTime());
			if( (0 < timeDiff_ms) && (timeDiff_ms < (LOGIN_TOKEN_PERIOD_MINS * 60 * 1000)) )
			{
				retVal = result.get(0).getValue(Users.USERS.ID);	
			}
		}

		return retVal;
	}


	public UInteger getUserIdForEmailAddress(String emailAddressIn)
	{
		Result<Record1<UInteger>> result = 
				this.getDslContext().select(Users.USERS.ID)
				.from(Users.USERS)
				.where(Users.USERS.EMAIL.eq(emailAddressIn))
				.limit(1)
				.fetch();
		if( result.size() < 1 ) return null;

		return result.get(0).getValue(Users.USERS.ID);
	}


	public boolean doesUserHavePermissionForOrganization(UInteger userIdIn, String orgUuidIn)
	{
		Result<Record1<String>> result = this.getDslContext().select(Organizations.ORGANIZATIONS.UUID)
				.from(Organizations.ORGANIZATIONS)
				.join(Organizationusermap.ORGANIZATIONUSERMAP)
				.on(Organizations.ORGANIZATIONS.UUID.eq(Organizationusermap.ORGANIZATIONUSERMAP.ORGANIZATIONUUID))
				.where(Organizations.ORGANIZATIONS.UUID.eq(orgUuidIn))
				.and(Organizationusermap.ORGANIZATIONUSERMAP.USERID.eq(userIdIn))
				.limit(1)
				.fetch();

		return result.size() > 0;
	}


	public boolean doesUserHavePermissionForDeviceType(UInteger userIdIn, String orgUuidIn, String devTypeUuidIn)
	{
		Result<Record1<String>> result = this.getDslContext().select(Devicetypes.DEVICETYPES.UUID)
				.from(Devicetypes.DEVICETYPES)
				.join(Organizations.ORGANIZATIONS)
				.on(Devicetypes.DEVICETYPES.ORGANIZATIONUUID.eq(Organizations.ORGANIZATIONS.UUID))
				.join(Organizationusermap.ORGANIZATIONUSERMAP)
				.on(Organizations.ORGANIZATIONS.UUID.eq(Organizationusermap.ORGANIZATIONUSERMAP.ORGANIZATIONUUID))
				.where(Devicetypes.DEVICETYPES.UUID.eq(devTypeUuidIn))
				.and(Organizations.ORGANIZATIONS.UUID.eq(orgUuidIn))
				.and(Organizationusermap.ORGANIZATIONUSERMAP.USERID.eq(userIdIn))
				.limit(1)
				.fetch();

		return result.size() > 0;
	}


	public boolean doesUserHavePermissionForProcessorType(UInteger userIdIn, String orgUuidIn, String devTypeUuidIn, String procTypeUuidIn)
	{
		Result<Record1<String>> result = this.getDslContext().select(Processortypes.PROCESSORTYPES.UUID)
				.from(Processortypes.PROCESSORTYPES)
				.join(Devicetypes.DEVICETYPES)
				.on(Processortypes.PROCESSORTYPES.DEVICETYPEUUID.eq(Devicetypes.DEVICETYPES.UUID))
				.join(Organizations.ORGANIZATIONS)
				.on(Devicetypes.DEVICETYPES.ORGANIZATIONUUID.eq(Organizations.ORGANIZATIONS.UUID))
				.join(Organizationusermap.ORGANIZATIONUSERMAP)
				.on(Organizations.ORGANIZATIONS.UUID.eq(Organizationusermap.ORGANIZATIONUSERMAP.ORGANIZATIONUUID))
				.where(Processortypes.PROCESSORTYPES.UUID.eq(procTypeUuidIn))
				.and(Devicetypes.DEVICETYPES.UUID.eq(devTypeUuidIn))
				.and(Organizations.ORGANIZATIONS.UUID.eq(orgUuidIn))
				.and(Organizationusermap.ORGANIZATIONUSERMAP.USERID.eq(userIdIn))
				.limit(1)
				.fetch();

		return result.size() > 0;
	}


	public boolean doesUserHavePermissionForFirmware(UInteger userIdIn, String orgUuidIn, String devTypeUuidIn, String procTypeUuidIn, String fwUuidIn)
	{
		Result<Record1<String>> result = this.getDslContext().select(Firmwareimages.FIRMWAREIMAGES.UUID)
				.from(Firmwareimages.FIRMWAREIMAGES)
				.join(Processortypes.PROCESSORTYPES)
				.on(Firmwareimages.FIRMWAREIMAGES.PROCESSORTYPEUUID.eq(Processortypes.PROCESSORTYPES.UUID))
				.join(Devicetypes.DEVICETYPES)
				.on(Processortypes.PROCESSORTYPES.DEVICETYPEUUID.eq(Devicetypes.DEVICETYPES.UUID))
				.join(Organizations.ORGANIZATIONS)
				.on(Devicetypes.DEVICETYPES.ORGANIZATIONUUID.eq(Organizations.ORGANIZATIONS.UUID))
				.join(Organizationusermap.ORGANIZATIONUSERMAP)
				.on(Organizations.ORGANIZATIONS.UUID.eq(Organizationusermap.ORGANIZATIONUSERMAP.ORGANIZATIONUUID))
				.where(Firmwareimages.FIRMWAREIMAGES.UUID.eq(fwUuidIn))
				.and(Processortypes.PROCESSORTYPES.UUID.eq(procTypeUuidIn))
				.and(Devicetypes.DEVICETYPES.UUID.eq(devTypeUuidIn))
				.and(Organizations.ORGANIZATIONS.UUID.eq(orgUuidIn))
				.and(Organizationusermap.ORGANIZATIONUSERMAP.USERID.eq(userIdIn))
				.limit(1)
				.fetch();

		return result.size() > 0;
	}


	public boolean addUserToOrganization(UInteger userIdIn, String orgUuidIn)
	{
		int numRecordsModified = this.getDslContext().insertInto(Organizationusermap.ORGANIZATIONUSERMAP, 
				Organizationusermap.ORGANIZATIONUSERMAP.USERID, Organizationusermap.ORGANIZATIONUSERMAP.ORGANIZATIONUUID)
				.values(userIdIn, orgUuidIn)
				.execute();
		return (numRecordsModified == 1);
	}


	public static Timestamp getNow()
	{
		return new Timestamp(Calendar.getInstance().getTime().getTime());
	}
}
