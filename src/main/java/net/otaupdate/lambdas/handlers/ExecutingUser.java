package net.otaupdate.lambdas.handlers;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;

import com.nimbusds.jwt.SignedJWT;

import net.otaupdate.lambdas.model.db.otaupdates.tables.Devices;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devicetypes;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareimages;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizationusermap;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.util.Logger;


public class ExecutingUser
{
	private static final String TAG = ExecutingUser.class.getSimpleName();


	private final String token;


	public ExecutingUser(String tokenIn)
	{
		this.token = tokenIn;
	}


	public String getAwsSub()
	{
		String retVal = null;
		try
		{
			SignedJWT obj = SignedJWT.parse(this.token);
			retVal = (String)obj.getPayload().toJSONObject().get("sub");
		}
		catch( Exception e )
		{
			Logger.getSingleton().warn(TAG, String.format("error parsing token: '%s'", e.toString()));
		}
		return retVal;
	}


	public boolean hasPermissionForOrganization(String orgUuidIn, DSLContext dslContextIn)
	{
		Result<Record> result = dslContextIn.select()
				.from(Organizationusermap.ORGANIZATIONUSERMAP)
				.where(Organizationusermap.ORGANIZATIONUSERMAP.AWSSUB.eq(this.getAwsSub()))
				.limit(1)
				.fetch();
		
		return (result.size() > 0);
	}


	public boolean hasPermissionForDeviceType(String orgUuidIn, String devTypeUuidIn, DSLContext dslContextIn)
	{
		Result<Record1<String>> result = dslContextIn.select(Devicetypes.DEVICETYPES.UUID)
				.from(Devicetypes.DEVICETYPES)
				.join(Organizations.ORGANIZATIONS)
				.on(Devicetypes.DEVICETYPES.ORGID.eq(Organizations.ORGANIZATIONS.ID))
				.join(Organizationusermap.ORGANIZATIONUSERMAP)
				.on(Organizations.ORGANIZATIONS.ID.eq(Organizationusermap.ORGANIZATIONUSERMAP.ORGANIZATIONID))
				.where(Devicetypes.DEVICETYPES.UUID.eq(devTypeUuidIn))
				.and(Organizations.ORGANIZATIONS.UUID.eq(orgUuidIn))
				.and(Organizationusermap.ORGANIZATIONUSERMAP.AWSSUB.eq(this.getAwsSub()))
				.limit(1)
				.fetch();

		return result.size() > 0;
	}
	
	
	public boolean hasPermissionForDevice(String orgUuidIn, String devTypeUuidIn, String devSerNumIn, DSLContext dslContextIn)
	{
		Result<Record1<String>> result = dslContextIn.select(Devices.DEVICES.SERIALNUMBER)
				.from(Devices.DEVICES)
				.join(Devicetypes.DEVICETYPES)
				.on(Devices.DEVICES.DEVTYPEID.eq(Devicetypes.DEVICETYPES.ID))
				.join(Organizations.ORGANIZATIONS)
				.on(Devicetypes.DEVICETYPES.ORGID.eq(Organizations.ORGANIZATIONS.ID))
				.join(Organizationusermap.ORGANIZATIONUSERMAP)
				.on(Organizations.ORGANIZATIONS.ID.eq(Organizationusermap.ORGANIZATIONUSERMAP.ORGANIZATIONID))
				.where(Devicetypes.DEVICETYPES.UUID.eq(devTypeUuidIn))
				.and(Organizations.ORGANIZATIONS.UUID.eq(orgUuidIn))
				.and(Organizationusermap.ORGANIZATIONUSERMAP.AWSSUB.eq(this.getAwsSub()))
				.limit(1)
				.fetch();

		return result.size() > 0;
	}


	public boolean hasPermissionForProcessorType(String orgUuidIn, String devTypeUuidIn, String procTypeUuidIn, DSLContext dslContextIn)
	{
		Result<Record1<String>> result = dslContextIn.select(Processortypes.PROCESSORTYPES.UUID)
				.from(Processortypes.PROCESSORTYPES)
				.join(Devicetypes.DEVICETYPES)
				.on(Processortypes.PROCESSORTYPES.DEVTYPEID.eq(Devicetypes.DEVICETYPES.ID))
				.join(Organizations.ORGANIZATIONS)
				.on(Devicetypes.DEVICETYPES.ORGID.eq(Organizations.ORGANIZATIONS.ID))
				.join(Organizationusermap.ORGANIZATIONUSERMAP)
				.on(Organizations.ORGANIZATIONS.ID.eq(Organizationusermap.ORGANIZATIONUSERMAP.ORGANIZATIONID))
				.where(Processortypes.PROCESSORTYPES.UUID.eq(procTypeUuidIn))
				.and(Devicetypes.DEVICETYPES.UUID.eq(devTypeUuidIn))
				.and(Organizations.ORGANIZATIONS.UUID.eq(orgUuidIn))
				.and(Organizationusermap.ORGANIZATIONUSERMAP.AWSSUB.eq(this.getAwsSub()))
				.limit(1)
				.fetch();

		return result.size() > 0;
	}


	public boolean hasPermissionForFirmware(String orgUuidIn, String devTypeUuidIn, String procTypeUuidIn, String fwUuidIn, DSLContext dslContextIn)
	{
		Result<Record1<String>> result = dslContextIn.select(Firmwareimages.FIRMWAREIMAGES.UUID)
				.from(Firmwareimages.FIRMWAREIMAGES)
				.join(Processortypes.PROCESSORTYPES)
				.on(Firmwareimages.FIRMWAREIMAGES.PROCTYPEID.eq(Processortypes.PROCESSORTYPES.ID))
				.join(Devicetypes.DEVICETYPES)
				.on(Processortypes.PROCESSORTYPES.DEVTYPEID.eq(Devicetypes.DEVICETYPES.ID))
				.join(Organizations.ORGANIZATIONS)
				.on(Devicetypes.DEVICETYPES.ORGID.eq(Organizations.ORGANIZATIONS.ID))
				.join(Organizationusermap.ORGANIZATIONUSERMAP)
				.on(Organizations.ORGANIZATIONS.ID.eq(Organizationusermap.ORGANIZATIONUSERMAP.ORGANIZATIONID))
				.where(Firmwareimages.FIRMWAREIMAGES.UUID.eq(fwUuidIn))
				.and(Processortypes.PROCESSORTYPES.UUID.eq(procTypeUuidIn))
				.and(Devicetypes.DEVICETYPES.UUID.eq(devTypeUuidIn))
				.and(Organizations.ORGANIZATIONS.UUID.eq(orgUuidIn))
				.and(Organizationusermap.ORGANIZATIONUSERMAP.AWSSUB.eq(this.getAwsSub()))
				.limit(1)
				.fetch();

		return result.size() > 0;
	}
}