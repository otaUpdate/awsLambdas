package net.otaupdate.lambdas.handlers.api.devType.devices;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.jooq.DSLContext;
import org.jooq.Record7;
import org.jooq.types.UInteger;
import org.jooq.Result;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devices;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devicetypes;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareimages;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareupdatehistory;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processors;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;
import net.otaupdate.lambdas.util.Logger;


public class GetDevicesHandler extends AbstractAuthorizedRequestHandler
{		
	static final String TAG = GetDevicesHandler.class.getSimpleName();
	static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	{
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	private final String ERR_STR = "error listing devices";
	
	
	@SuppressWarnings("unused")
	private class ReturnValue
	{
		class ProcInfo
		{
			public final String typeName;
			public final String serialNumber;
			public String lastSeenUTC = null;
			public String fwImageName = null;
			public Boolean isUpToDate = null;
			
			private Timestamp lastSeenDate = null;
			
			ProcInfo(String typeNameIn, String serialNumberIn)
			{
				this.typeName = typeNameIn;
				this.serialNumber = serialNumberIn;
			}
			
			
			void addFirmwareEntry(Timestamp lastSeenIn, String fwImageNameIn, Boolean isUpToDateIn)
			{
				if( (this.lastSeenDate == null) || this.lastSeenDate.before(lastSeenIn) )
				{
					this.lastSeenDate = lastSeenIn;
					this.lastSeenUTC = DATE_FORMAT.format(lastSeenIn);
					this.fwImageName = fwImageNameIn;
					this.isUpToDate = isUpToDateIn;
				}
			}
		}
		
		
		public final String typeName;
		public final String serialNumber;
		public final List<ProcInfo> processorInfo = new ArrayList<ProcInfo>();
		
		ReturnValue(String typeNameIn, String serialNumberIn)
		{
			this.typeName = typeNameIn;
			this.serialNumber = serialNumberIn;
		}
		
		
		void addProcessorFirmwareEntry(String typeNameIn, String serialNumberIn, Timestamp lastSeenIn, String fwImageNameIn, Boolean isUpToDate)
		{	
			ProcInfo currEntry = this.getEntryForProcSerialNumber(serialNumberIn);
			if( currEntry == null )
			{
				currEntry = new ProcInfo(typeNameIn, serialNumberIn);
				this.processorInfo.add(currEntry);
			}
			if( (lastSeenIn != null) && (fwImageNameIn != null) && (isUpToDate != null) )
			{
				currEntry.addFirmwareEntry(lastSeenIn, fwImageNameIn, isUpToDate);
			}
		}
		
		
		private ProcInfo getEntryForProcSerialNumber(String serialNumberIn)
		{
			for( ProcInfo currEntry : this.processorInfo )
			{
				if( currEntry.serialNumber.equals(serialNumberIn) ) return currEntry;
			}
			return null;
		}
	}
	
	
	private String orgUuid = null;
	private String devTypeUuid = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{	
		Map<String, Object> pathParameters = paramsIn.getPathParameters();
		if( pathParameters == null ) return false;

		this.orgUuid = ObjectHelper.parseObjectFromMap(pathParameters, "orgUuid", String.class);
		if( (this.orgUuid == null) || this.orgUuid.isEmpty() ) return false;
		
		this.devTypeUuid = ObjectHelper.parseObjectFromMap(pathParameters, "devTypeUuid", String.class);
		if( (this.devTypeUuid == null) || this.devTypeUuid.isEmpty() ) return false;
		
		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{	
		Map<String, ReturnValue> retVals = new HashMap<String, ReturnValue>();
		
		Logger.getSingleton().debug(TAG, "1");
		
		// check user permissions
		if( !dbManIn.doesUserHavePermissionForDeviceType(userIdIn, this.orgUuid, this.devTypeUuid) ) return retVals.values();
		
		Logger.getSingleton().debug(TAG, "2");
		
		// get device type id
		UInteger devTypeId = dbManIn.getDevTypeIdForUuid(this.devTypeUuid);
		if( devTypeId == null ) ErrorManager.throwError(ErrorType.BadRequest, ERR_STR);
		
		Logger.getSingleton().debug(TAG, "3");
		
		// iterate over our devices
		Result<Record7<String, String, String, String, Timestamp, String, UInteger>> result =
				dslContextIn.select(Devicetypes.DEVICETYPES.NAME, Devices.DEVICES.SERIALNUMBER, 
						Processortypes.PROCESSORTYPES.NAME, Processors.PROCESSORS.SERIALNUMBER, 
						Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.TIMESTAMP, Firmwareimages.FIRMWAREIMAGES.NAME, Firmwareimages.FIRMWAREIMAGES.TOVERSIONID)
				.from(Devices.DEVICES)
				.join(Devicetypes.DEVICETYPES)
				.on(Devices.DEVICES.DEVTYPEID.eq(Devicetypes.DEVICETYPES.ID))
				.join(Processors.PROCESSORS)
				.on(Devices.DEVICES.ID.eq(Processors.PROCESSORS.DEVID))
				.join(Processortypes.PROCESSORTYPES)
				.on(Processors.PROCESSORS.PROCTYPEID.eq(Processortypes.PROCESSORTYPES.ID))
				.leftJoin(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY)
				.on(Processors.PROCESSORS.ID.eq(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.PROCID))
				.leftJoin(Firmwareimages.FIRMWAREIMAGES)
				.on(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.FWIMAGEID.eq(Firmwareimages.FIRMWAREIMAGES.ID))
				.where(Devicetypes.DEVICETYPES.ID.eq(devTypeId))
				.fetch();
			
		// result will be a collection of processor entries and firmware histories for each processor
		for( Record7<String, String, String, String, Timestamp, String, UInteger> currEntry : result )
		{
			Logger.getSingleton().debug(TAG, "4");
			
			// required for each entry
			String devTypeName = currEntry.getValue(Devicetypes.DEVICETYPES.NAME);
			String devSerialNumber = currEntry.getValue(Devices.DEVICES.SERIALNUMBER);
			String procTypeName = currEntry.getValue(Processortypes.PROCESSORTYPES.NAME);
			String procSerialNumber = currEntry.getValue(Processors.PROCESSORS.SERIALNUMBER);
			if( (devTypeName == null) || (devSerialNumber == null) || (procTypeName == null) || (procSerialNumber == null) ) continue;
			
			Logger.getSingleton().debug(TAG, "5");
			
			// optional for each entry (depends on whether the processors has checked in yet)
			Timestamp lastSeenUtc = currEntry.getValue(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.TIMESTAMP);
			String fwImageName = currEntry.getValue(Firmwareimages.FIRMWAREIMAGES.NAME);
			UInteger fwToVersionId = currEntry.getValue(Firmwareimages.FIRMWAREIMAGES.TOVERSIONID);
			
			Logger.getSingleton().debug(TAG, "6");
			
			// see if we already have an entry for this device...if not, create it
			ReturnValue currDevice = retVals.get(devSerialNumber);
			if( currDevice == null )
			{
				Logger.getSingleton().debug(TAG, "7");
				currDevice = new ReturnValue(devTypeName, devSerialNumber);
				retVals.put(devSerialNumber, currDevice);
			}
			Logger.getSingleton().debug(TAG, "8");
			currDevice.addProcessorFirmwareEntry(procTypeName, procSerialNumber, lastSeenUtc, fwImageName, (fwToVersionId == null));
			Logger.getSingleton().debug(TAG, "9");
		}
		
		return retVals.values();
	}
}
