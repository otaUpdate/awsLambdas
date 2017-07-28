package net.otaupdate.lambdas.handlers.devs;

import java.sql.Timestamp;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareimages;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareupdatehistory;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processors;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.S3Helper;


public class CheckForUpdateHandler extends AbstractUnauthorizedRequestHandler
{
	private String currentFwUuid = null;
	private String procSerialNum = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;

		this.currentFwUuid = ObjectHelper.parseObjectFromMap(jsonBodyMap, "currFwUuid", String.class);
		if( (this.currentFwUuid == null) || this.currentFwUuid.isEmpty() ) return false;

		this.procSerialNum = ObjectHelper.parseObjectFromMap(jsonBodyMap, "procSerialNum", String.class);
		if( (this.procSerialNum == null) || this.procSerialNum.isEmpty() ) return false;

		return true;
	}


	@Override
	public Object processRequestWithDatabase(DatabaseManager dbManIn, DSLContext dslContextIn)
	{	
		Object retVal = null;
		
		// before we get to the actual update check, record our current version information
		Result<Record3<UInteger, UInteger, UInteger>> result =
				dslContextIn.select(Firmwareimages.FIRMWAREIMAGES.ID, Firmwareimages.FIRMWAREIMAGES.TOVERSIONID, Processors.PROCESSORS.ID)
				.from(Firmwareimages.FIRMWAREIMAGES)
				.join(Processortypes.PROCESSORTYPES)
				.on(Firmwareimages.FIRMWAREIMAGES.PROCTYPEID.eq(Processortypes.PROCESSORTYPES.ID))
				.leftJoin(Processors.PROCESSORS)
				.on(Processortypes.PROCESSORTYPES.ID.eq(Processors.PROCESSORS.PROCTYPEID))
				.and(Processors.PROCESSORS.SERIALNUMBER.eq(this.procSerialNum))
				.where(Firmwareimages.FIRMWAREIMAGES.UUID.eq(this.currentFwUuid))
				.limit(1)
				.fetch();
		if( result.size() > 0 )
		{	
			UInteger procId = result.get(0).getValue(Processors.PROCESSORS.ID);
			UInteger fwImageId = result.get(0).getValue(Firmwareimages.FIRMWAREIMAGES.ID);

			Timestamp ts = DatabaseManager.getNow();
			
			// now that we have the processorId (or know that it does not exist) record our check-in
			dslContextIn.insertInto(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY, 
					Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.PROCID, Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.FWIMAGEID,
					Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.TIMESTAMP, Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.UNPROVISIONEDPROCSERIALNUM)
			.values(procId, fwImageId, ts, (procId == null) ? this.procSerialNum : null)
			.onDuplicateKeyUpdate()
			.set(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.TIMESTAMP, ts)
			.execute();
			

			// finally, start constructing our result
			String targetFwUuid = dbManIn.getFirmwareImageUuidForId(result.get(0).getValue(Firmwareimages.FIRMWAREIMAGES.TOVERSIONID));
			if( targetFwUuid != null )
			{
				// firmware id was correct...lookup firmware size
				Long fwSize_bytes = S3Helper.getFirmwareSizeInBytes(targetFwUuid);
				if( fwSize_bytes != null )
				{
					// we have a stored image for this firmware...create our return value
					retVal = String.format("%s %d", targetFwUuid, fwSize_bytes);
				}
			}
		}

		return retVal; 
	}
}
