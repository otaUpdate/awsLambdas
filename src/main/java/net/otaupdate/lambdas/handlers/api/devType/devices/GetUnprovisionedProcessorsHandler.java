package net.otaupdate.lambdas.handlers.api.devType.devices;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.Record5;
import org.jooq.types.UInteger;

import com.amazonaws.util.DateUtils;

import org.jooq.Result;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devicetypes;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareimages;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareupdatehistory;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;


public class GetUnprovisionedProcessorsHandler extends AbstractAuthorizedRequestHandler
{		
	static final String TAG = GetUnprovisionedProcessorsHandler.class.getSimpleName();
	private final String ERR_STR = "error listing unprovisioned processors";


	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final String typeName;
		public final String serialNumber;
		public final String fwImageName;
		public final Boolean isUpToDate;
		
		public  String lastSeenUTC = null;
		private Timestamp lastSeenDate = null;

		ReturnValue(String typeNameIn, String serialNumberIn, String fwImageNameIn, Boolean isUpToDateIn)
		{
			this.typeName = typeNameIn;
			this.serialNumber = serialNumberIn;
			this.fwImageName = fwImageNameIn;
			this.isUpToDate = isUpToDateIn;
		}


		void updateLastSeenDateIfNeeded(Timestamp lastSeenIn)
		{
			if( (this.lastSeenDate == null) || this.lastSeenDate.before(lastSeenIn) )
			{
				this.lastSeenDate = lastSeenIn;
				this.lastSeenUTC = DateUtils.formatISO8601Date(lastSeenIn);
			}
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
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{	
		Map<String, ReturnValue> retVals = new HashMap<String, ReturnValue>();

		// check user permissions
		if( !userIn.hasPermissionForDeviceType(this.orgUuid, this.devTypeUuid, dslContextIn) ) return retVals.values();

		// get device type id
		UInteger devTypeId = dbManIn.getDevTypeIdForUuid(this.devTypeUuid);
		if( devTypeId == null ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);

		// iterate over our devices
		Result<Record5<String, String, UInteger, String, Timestamp>> result =
				dslContextIn.select(Processortypes.PROCESSORTYPES.NAME,
						Firmwareimages.FIRMWAREIMAGES.NAME, Firmwareimages.FIRMWAREIMAGES.TOVERSIONID,
						Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.UNPROVISIONEDPROCSERIALNUM, Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.TIMESTAMP)
				.from(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY)
				.join(Firmwareimages.FIRMWAREIMAGES)
				.on(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.FWIMAGEID.eq(Firmwareimages.FIRMWAREIMAGES.ID))
				.join(Processortypes.PROCESSORTYPES)
				.on(Firmwareimages.FIRMWAREIMAGES.PROCTYPEID.eq(Processortypes.PROCESSORTYPES.ID))
				.join(Devicetypes.DEVICETYPES)
				.on(Processortypes.PROCESSORTYPES.DEVTYPEID.eq(Devicetypes.DEVICETYPES.ID))
				.where(Devicetypes.DEVICETYPES.ID.eq(devTypeId))
				.and(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.UNPROVISIONEDPROCSERIALNUM.isNotNull())
				.fetch();
				

		// result will be a collection of processor entries and firmware histories for each processor
		for( Record5<String, String, UInteger, String, Timestamp> currEntry : result )
		{
			// required for each entry
			String procTypeName = currEntry.getValue(Processortypes.PROCESSORTYPES.NAME);
			String procSerialNumber = currEntry.getValue(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.UNPROVISIONEDPROCSERIALNUM);
			if( (procTypeName == null) || (procSerialNumber == null) ) continue;

			// optional for each entry (depends on whether the processors has checked in yet)
			Timestamp lastSeenUtc = currEntry.getValue(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.TIMESTAMP);
			String fwImageName = currEntry.getValue(Firmwareimages.FIRMWAREIMAGES.NAME);
			UInteger fwToVersionId = currEntry.getValue(Firmwareimages.FIRMWAREIMAGES.TOVERSIONID);
			
			// see if we already have an entry for this processor...if not, create it
			ReturnValue currProc = retVals.get(procSerialNumber);
			if( currProc == null )
			{
				currProc = new ReturnValue(procTypeName, procSerialNumber, fwImageName, (fwToVersionId == null));
				retVals.put(procSerialNumber, currProc);
			}
			currProc.updateLastSeenDateIfNeeded(lastSeenUtc);
		}

		return retVals.values();
	}
}
