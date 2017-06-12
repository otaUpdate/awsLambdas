package net.otaupdate.lambdas;

import java.sql.SQLException;
import java.util.HashMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import net.otaupdate.lambdas.checkForUpdate.model.DatabaseManager;
import net.otaupdate.lambdas.checkForUpdate.model.FirmwareIdentifier;
import net.otaupdate.util.ErrorManager;
import net.otaupdate.util.ErrorManager.ErrorType;
import net.otaupdate.util.Logger;


public class CheckForUpdateLambda implements RequestHandler<HashMap<?,?>, Object>
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
    public ReturnValue handleRequest(HashMap<?,?> paramsIn, Context context)
    {
    	// initialize our logger
    	new Logger(context);
    	
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
