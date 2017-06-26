package net.otaupdate.lambdas.handlers.devController;

import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.AbstractRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.FirmwareIdentifier;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class CheckForUpdateHandler extends AbstractAuthorizedRequestHandler
{
	private class ReturnValue
	{
		@SuppressWarnings("unused")
		public final boolean updateAvailable;
		public final String targetVersionUuid;
		
		ReturnValue(String targetVersionUuidIn)
		{
			this.targetVersionUuid = targetVersionUuidIn;
			this.updateAvailable = (this.targetVersionUuid != null);
		}
	}
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		return true;
	}
	

	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, int userIdIn)
	{
//		// parse our parameters
//    	Object currentFirmwareUuid_raw = paramsIn.get("currentFirmwareUuid");
//    	if( (currentFirmwareUuid_raw == null) || !(currentFirmwareUuid_raw instanceof String) )
//    	{
//    		ErrorManager.throwError(ErrorType.BadRequest, "problem parsing input parameters");
//    	}
//    
//    	// get our firmware identifier
//    	FirmwareIdentifier fi = new FirmwareIdentifier((String)currentFirmwareUuid_raw);
//    	
//    	
//		// figure out our target version
//		String targetVersion = dbManIn.getLatestFirmwareUuid(fi);
//		
//    	// 	encode our return value
//		return new ReturnValue(targetVersion);
		
		return null;
	}

}
