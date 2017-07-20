package net.otaupdate.lambdas.handlers.devController;

import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.S3Helper;


public class CheckForUpdateHandler extends AbstractUnauthorizedRequestHandler
{
	private String currentFwUuid = null;
	private String serialNum = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		this.currentFwUuid = ObjectHelper.parseObjectFromMap(paramsIn, "currFwUuid", String.class);
		if( this.currentFwUuid == null ) return false;
		
		this.serialNum = ObjectHelper.parseObjectFromMap(paramsIn, "serialNum", String.class);
		if( this.serialNum == null ) return false;
		
		return true;
	}
	

	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn)
	{	
		// log our information to the update history
		dbManIn.updateUpdateHistory(this.currentFwUuid, this.serialNum);
		
		String targetFwUuid = dbManIn.getNextFirmwareForFirmwareUuid(this.currentFwUuid);
		if( targetFwUuid == null ) return null;
		
		Long fwSize_bytes = S3Helper.getFirmwareSizeInBytes(targetFwUuid);
		if( fwSize_bytes == null ) return null;
		
		return String.format("%s %d", targetFwUuid, fwSize_bytes);
	}
}
