package net.otaupdate.lambdas.handlers.devs;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devicetypes;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareimages;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareupdatehistory;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.util.ObjectHelper;


public class GetProcInstanceInfoHandler extends AbstractUnauthorizedRequestHandler
{
	@SuppressWarnings("unused")
	private class GetProcInstanceInfoResponse
	{
		public final String lastCheckInUTC;
		public final String currFwUuid;
		public final String currFwName;
		public final Boolean updateAvailable;
		
		public GetProcInstanceInfoResponse(Date lastCheckInIn, String currFwUuidIn, String currFwNameIn, Boolean isUpdateAvailableIn)
		{
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			this.lastCheckInUTC = sdf.format(lastCheckInIn);
			
			this.currFwUuid = currFwUuidIn;
			this.currFwName = currFwNameIn;
			this.updateAvailable = isUpdateAvailableIn;
		}
	}
	
	
	private String organizationUuid = null;
	private String deviceUuid = null;
	private String processorUuid = null;
	private String serialNum = null;
	
	
	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;
		
		this.organizationUuid = ObjectHelper.parseObjectFromMap(jsonBodyMap, "organizationUuid", String.class);
		if( this.organizationUuid == null ) return false;
		
		this.deviceUuid = ObjectHelper.parseObjectFromMap(jsonBodyMap, "deviceTypeUuid", String.class);
		if( this.deviceUuid == null ) return false;
		
		this.processorUuid = ObjectHelper.parseObjectFromMap(jsonBodyMap, "processorTypeUuid", String.class);
		if( this.processorUuid == null ) return false;
		
		this.serialNum = ObjectHelper.parseObjectFromMap(jsonBodyMap, "serialNum", String.class);
		if( this.serialNum == null ) return false;
		
		return true;
	}
	

	@Override
	public Object processRequestWithDatabase(DatabaseManager dbManIn, DSLContext dslContextIn)
	{	
		Object retVal = new Object();
		
		// fetch
		Result<Record4<Timestamp, String, String, String>> result = 
				dslContextIn.select(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.TIMESTAMP, Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.FIRMWAREUUID,
						Firmwareimages.FIRMWAREIMAGES.NAME, Firmwareimages.FIRMWAREIMAGES.TOVERSIONUUID)
				.from(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY)
				.join(Firmwareimages.FIRMWAREIMAGES).on(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.FIRMWAREUUID.eq(Firmwareimages.FIRMWAREIMAGES.UUID))
				.join(Processortypes.PROCESSORTYPES).on(Firmwareimages.FIRMWAREIMAGES.PROCESSORTYPEUUID.eq(Processortypes.PROCESSORTYPES.UUID))
				.join(Devicetypes.DEVICETYPES).on(Processortypes.PROCESSORTYPES.DEVICETYPEUUID.eq(Devicetypes.DEVICETYPES.UUID))
				.join(Organizations.ORGANIZATIONS).on(Devicetypes.DEVICETYPES.ORGANIZATIONUUID.eq(Organizations.ORGANIZATIONS.UUID))
				.where(Organizations.ORGANIZATIONS.UUID.eq(GetProcInstanceInfoHandler.this.organizationUuid)
						.and(Devicetypes.DEVICETYPES.UUID.eq(GetProcInstanceInfoHandler.this.deviceUuid))
						.and(Processortypes.PROCESSORTYPES.UUID.eq(GetProcInstanceInfoHandler.this.processorUuid))
						.and(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.SERIALNUMBER.eq(GetProcInstanceInfoHandler.this.serialNum))
						)
				.fetch();

		// check the result for sanity
		if( result.size() > 0 )
		{
			Record4<Timestamp, String, String, String> firstRecord = result.get(0);

			// create our return value
			Date lastCheckIn = firstRecord.getValue(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.TIMESTAMP);
			String fwUuid = firstRecord.getValue(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.FIRMWAREUUID);
			String fwName = firstRecord.getValue(Firmwareimages.FIRMWAREIMAGES.NAME);
			String toVersionUuid = firstRecord.getValue(Firmwareimages.FIRMWAREIMAGES.TOVERSIONUUID);
	
			retVal = new GetProcInstanceInfoResponse(lastCheckIn, fwUuid, fwName, (toVersionUuid != null));
		}
		
		return retVal;
	}
}
