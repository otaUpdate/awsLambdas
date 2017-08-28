package net.otaupdate.lambdas.handlers.ext;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.Record7;
import org.jooq.Result;
import org.jooq.types.UInteger;

import com.amazonaws.util.DateUtils;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractApiKeyAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devices;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devicetypes;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareimages;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareupdatehistory;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processors;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;

public class DeviceStatusHandler extends AbstractApiKeyAuthorizedRequestHandler
{
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
					this.lastSeenUTC = DateUtils.formatISO8601Date(lastSeenIn);
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
	private String devSerialNumber = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;

		this.orgUuid = ObjectHelper.parseObjectFromMap(jsonBodyMap, "orgUuid", String.class);
		if( (this.orgUuid == null) || this.orgUuid.isEmpty() ) return false;

		this.devTypeUuid = ObjectHelper.parseObjectFromMap(jsonBodyMap, "devTypeUuid", String.class);
		if( (this.devTypeUuid == null) || this.devTypeUuid.isEmpty() ) return false;

		this.devSerialNumber = ObjectHelper.parseObjectFromMap(jsonBodyMap, "devSerialNumber", String.class);
		if( (this.devSerialNumber == null) || this.devSerialNumber.isEmpty() ) return false;

		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger orgIdIn)
			throws BreakwallAwsException
	{
		ReturnValue retVal = null;

		// check organization permissions
		if( dbManIn.getOrganizationIdForUuid(this.orgUuid) != orgIdIn ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);

		// get device type id
		UInteger devTypeId = dbManIn.getDevTypeIdForUuid(this.devTypeUuid);
		if( devTypeId == null ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);

		// lookup the device
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
				.and(Devices.DEVICES.SERIALNUMBER.eq(this.devSerialNumber))
				.fetch();
		if( result.size() < 1 ) 	throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);

		// result will be a collection of processor entries and firmware histories for our target processor
		for( Record7<String, String, String, String, Timestamp, String, UInteger> currEntry : result )
		{
			// required for each entry
			String devTypeName = currEntry.getValue(Devicetypes.DEVICETYPES.NAME);
			String devSerialNumber = currEntry.getValue(Devices.DEVICES.SERIALNUMBER);
			String procTypeName = currEntry.getValue(Processortypes.PROCESSORTYPES.NAME);
			String procSerialNumber = currEntry.getValue(Processors.PROCESSORS.SERIALNUMBER);
			if( (devTypeName == null) || (devSerialNumber == null) || (procTypeName == null) || (procSerialNumber == null) ) continue;

			// optional for each entry (depends on whether the processors has checked in yet)
			Timestamp lastSeenUtc = currEntry.getValue(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.TIMESTAMP);
			String fwImageName = currEntry.getValue(Firmwareimages.FIRMWAREIMAGES.NAME);
			UInteger fwToVersionId = currEntry.getValue(Firmwareimages.FIRMWAREIMAGES.TOVERSIONID);

			// see if we already have an entry for this device...if not, create it
			if( retVal == null )
			{
				retVal = new ReturnValue(devTypeName, devSerialNumber);
			}
			retVal.addProcessorFirmwareEntry(procTypeName, procSerialNumber, lastSeenUtc, fwImageName, (fwToVersionId == null));
		}

		return retVal;
	}
}
