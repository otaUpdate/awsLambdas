package net.otaupdate.lambdas.handlers.devController;

import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.AbstractRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.HardwareIdentifier;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class GetLatestVersionHandler extends AbstractAuthorizedRequestHandler
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
//    	Object currentHardwareUuid_raw = paramsIn.get("hardwareUuid");
//    	if( (currentHardwareUuid_raw == null) || !(currentHardwareUuid_raw instanceof String) )
//    	{
//    		ErrorManager.throwError(ErrorType.BadRequest, "problem parsing input parameters");
//    	}
//    	
//    	// get our hardware identifier
//    	HardwareIdentifier hi = new HardwareIdentifier((String)currentHardwareUuid_raw);
//    	
//    	
//    	// figure out our target version
//		String targetVersion = dbManIn.getLatestFirmwareUuid(hi);
//    	
//    	// 	encode our return value
//		return new ReturnValue(targetVersion);
		
		return null;
	}

}
