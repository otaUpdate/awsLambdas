package net.otaupdate.lambdas.handlers.devs;

import java.sql.Timestamp;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareimages;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareupdatehistory;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.S3Helper;


public class CheckForUpdateHandler extends AbstractUnauthorizedRequestHandler
{
	private String currentFwUuid = null;
	private String serialNum = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;

		this.currentFwUuid = ObjectHelper.parseObjectFromMap(jsonBodyMap, "currFwUuid", String.class);
		if( this.currentFwUuid == null ) return false;

		this.serialNum = ObjectHelper.parseObjectFromMap(jsonBodyMap, "serialNum", String.class);
		if( this.serialNum == null ) return false;

		return true;
	}


	@Override
	public Object processRequestWithDatabase(DatabaseManager dbManIn, DSLContext dslContextIn)
	{	
		Object retVal = null;
		
		// get the target firmware id
		Result<Record1<String>> result =
				dslContextIn.select(Firmwareimages.FIRMWAREIMAGES.TOVERSIONUUID)
				.from(Firmwareimages.FIRMWAREIMAGES)
				.where(Firmwareimages.FIRMWAREIMAGES.UUID.eq(this.currentFwUuid))
				.fetch();
		if( result.size() > 0 )
		{
			String targetFwUuid = result.get(0).getValue(Firmwareimages.FIRMWAREIMAGES.TOVERSIONUUID);
			if( targetFwUuid != null )
			{
				// firmware id was correct...lookup firmware size
				Long fwSize_bytes = S3Helper.getFirmwareSizeInBytes(targetFwUuid);
				if( fwSize_bytes != null )
				{
					// we have a stored image for this firmware...create our return value
					retVal = String.format("%s %d", targetFwUuid, fwSize_bytes);
				}
				
				// regardless of firmware size success, update the firmware history table
				Timestamp now = DatabaseManager.getNow();
				dslContextIn.insertInto(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY, Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.FIRMWAREUUID, 
						Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.SERIALNUMBER, Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.TIMESTAMP)
				.values(this.currentFwUuid, this.serialNum, now)
				.onDuplicateKeyUpdate()
				.set(Firmwareupdatehistory.FIRMWAREUPDATEHISTORY.TIMESTAMP, now)
				.execute();
			}
		}

		return retVal; 
	}
}
