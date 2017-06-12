package net.otaupdate.lambdas.handlers;

import java.sql.SQLException;
import java.util.HashMap;

import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.FirmwareIdentifier;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class CheckForUpdateHandler extends AbstractMultiplexedRequestHandler
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
	public Object handleRequestWithParameters(HashMap<String, Object> paramsIn)
	{
		// parse our parameters
    	Object currentFirmwareUuid_raw = paramsIn.get("currentFirmwareUuid");
    	if( (currentFirmwareUuid_raw == null) || !(currentFirmwareUuid_raw instanceof String) )
    	{
    		ErrorManager.throwError(ErrorType.BadRequest, "problem parsing input parameters");
    	}
    
    	// get our firmware identifier
    	FirmwareIdentifier fi = new FirmwareIdentifier((String)currentFirmwareUuid_raw);
    	
    	// setup a connection to our database
    	DatabaseManager dbMan = null;
    	try{ dbMan = new DatabaseManager(); } 
    	catch( SQLException e ) { ErrorManager.throwError(ErrorType.ServerError, "problem connecting to database"); }
    	
    	ReturnValue retVal = null;
    	try
    	{
    		// figure out our target version
    		String targetVersion = dbMan.getLatestFirmwareUuid(fi);
    		
	    	// 	encode our return value
    		retVal = new ReturnValue(targetVersion);
    	}
    	catch( Exception e )
    	{
    		ErrorManager.throwError(ErrorType.ServerError, "unhandled exception");
    	}
    	finally
    	{
    		dbMan.close();
    	}
    	
    	return retVal;
	}

}
