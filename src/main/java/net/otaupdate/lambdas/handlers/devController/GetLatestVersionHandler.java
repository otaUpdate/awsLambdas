package net.otaupdate.lambdas.handlers.devController;

import java.sql.SQLException;
import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractMultiplexedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.HardwareIdentifier;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class GetLatestVersionHandler extends AbstractMultiplexedRequestHandler
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
    	Object currentHardwareUuid_raw = paramsIn.get("hardwareUuid");
    	if( (currentHardwareUuid_raw == null) || !(currentHardwareUuid_raw instanceof String) )
    	{
    		ErrorManager.throwError(ErrorType.BadRequest, "problem parsing input parameters");
    	}
    	
    	// get our hardware identifier
    	HardwareIdentifier hi = new HardwareIdentifier((String)currentHardwareUuid_raw);
    	
    	// setup a connection to our database
    	DatabaseManager dbMan = null;
    	try{ dbMan = new DatabaseManager(); } 
    	catch( SQLException e ) { ErrorManager.throwError(ErrorType.ServerError, "problem connecting to database"); }
    	
    	ReturnValue retVal = null;
    	try
    	{
    		// figure out our target version
    		String targetVersion = dbMan.getLatestFirmwareUuid(hi);
    	
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
