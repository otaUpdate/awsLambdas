package net.otaupdate.lambdas.handlers.api.devType.devices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devices;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processors;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.model.db.otaupdates.tables.records.DevicesRecord;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;
import net.otaupdate.lambdas.util.Logger;


public class CreateDeviceHandler extends AbstractAuthorizedRequestHandler
{		
	private static final String TAG = CreateDeviceHandler.class.getSimpleName();
	private static final String ERR_STRING_GENERAL = "error creating device";


	private class ProcInfoEntry
	{
		final String serialNumber;
		final String procTypeUuid;
		UInteger procTypeId = null;

		ProcInfoEntry(String serialNumberIn, String procTypeUuidIn)
		{
			this.serialNumber = serialNumberIn;
			this.procTypeUuid = procTypeUuidIn;
		}
	}


	private String orgUuid = null;
	private String devTypeUuid = null;

	private String serialNumber = null;
	private List<ProcInfoEntry> processors = new ArrayList<ProcInfoEntry>();


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{	
		Map<String, Object> pathParameters = paramsIn.getPathParameters();
		if( pathParameters == null ) return false;

		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;

		this.orgUuid = ObjectHelper.parseObjectFromMap(pathParameters, "orgUuid", String.class);
		if( (this.orgUuid == null) || this.orgUuid.isEmpty() ) return false;

		this.devTypeUuid = ObjectHelper.parseObjectFromMap(pathParameters, "devTypeUuid", String.class);
		if( (this.devTypeUuid == null) || this.devTypeUuid.isEmpty() ) return false;

		this.serialNumber = ObjectHelper.parseObjectFromMap(jsonBodyMap, "serialNumber", String.class);
		if( (this.serialNumber == null) || this.serialNumber.isEmpty() ) return false;

		@SuppressWarnings("unchecked")
		List<Map<String, String>> procInfo_raw = ObjectHelper.parseObjectFromMap(jsonBodyMap, "processors"	, List.class);
		if( procInfo_raw == null ) return false;
		for( Map<String, String> currProcEntry_raw : procInfo_raw )
		{
			String serialNumber = ObjectHelper.parseObjectFromMap(currProcEntry_raw, "serialNumber", String.class);
			String procTypeUuid = ObjectHelper.parseObjectFromMap(currProcEntry_raw, "procTypeUuid", String.class);
			if( (serialNumber == null) || (procTypeUuid == null) ) return false;

			this.processors.add(new ProcInfoEntry(serialNumber, procTypeUuid));
		}

		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{	
		// check user permissions
		if( !dbManIn.doesUserHavePermissionForDeviceType(userIdIn, this.orgUuid, this.devTypeUuid) ) ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING_GENERAL);

		// get devType id
		UInteger devTypeId = dbManIn.getDevTypeIdForUuid(this.devTypeUuid);
		if( devTypeId == null ) ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING_GENERAL);

		// first, we should verify that we have all of the needed procTypeUuids in the provided array
		Result<Record2<UInteger, String>> result = 
				dslContextIn.select(Processortypes.PROCESSORTYPES.ID, Processortypes.PROCESSORTYPES.UUID)
				.from(Processortypes.PROCESSORTYPES)
				.where(Processortypes.PROCESSORTYPES.DEVTYPEID.eq(devTypeId))
				.fetch();
		for( Record2<UInteger, String> currRecord : result )
		{
			boolean didMatch = false;
			for( ProcInfoEntry currPIE : this.processors )
			{
				if( currPIE.procTypeUuid.equals(currRecord.getValue(Processortypes.PROCESSORTYPES.UUID)) )
				{
					didMatch = true;
					currPIE.procTypeId = currRecord.getValue(Processortypes.PROCESSORTYPES.ID);
				}
			}
			
			if( !didMatch )
			{
				ErrorManager.throwError(ErrorType.BadRequest, String.format("missing processor entry for procTypeUuid '%s'", currRecord.getValue(Processortypes.PROCESSORTYPES.UUID)));
			}
		}
		
		// now we should make sure that we don't have any _extra_ procTypeUuids in the provided array
		for( ProcInfoEntry currPIE : this.processors )
		{
			if( currPIE.procTypeId == null )
			{
				ErrorManager.throwError(ErrorType.BadRequest, String.format("extraneuous processor entry for procTypeUuid '%s'", currPIE.procTypeUuid));
			}
		}
		
		// OK, everything looks good...now add the device type first...
		Result<DevicesRecord> result_devices = 
				dslContextIn.insertInto(Devices.DEVICES)
				.set(Devices.DEVICES.SERIALNUMBER, this.serialNumber)
				.set(Devices.DEVICES.DEVTYPEID, devTypeId)
				.returning()
				.fetch();
		if( result_devices.size() < 1 ) ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING_GENERAL);
		UInteger devId = result_devices.get(0).getId();
		
		// now add each of our processors
		for( ProcInfoEntry currPIE : this.processors )
		{
			int numModifedRecords = dslContextIn.insertInto(Processors.PROCESSORS)
					.set(Processors.PROCESSORS.SERIALNUMBER, currPIE.serialNumber)
					.set(Processors.PROCESSORS.PROCTYPEID, currPIE.procTypeId)
					.set(Processors.PROCESSORS.DEVID, devId)
					.execute();
			if( numModifedRecords < 1 )
			{
				Logger.getSingleton().warn(TAG, String.format("error creating processor after device, device incomplete sn:'%s'", this.serialNumber));
				ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING_GENERAL);
			}
		}

		return null;
	}
}
