package net.otaupdate.lambdas.handlers;

import java.sql.SQLException;
import java.util.HashMap;

import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.DownloadableFirmwareImage;
import net.otaupdate.lambdas.model.FirmwareIdentifier;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class GetFirmwareDownloadLinkHandler extends AbstractMultiplexedRequestHandler
{
	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final boolean downloadAvailable;
		public final String name;
		public final String targetVersionUuid;
		public final String url;
		
		ReturnValue()
		{
			this.downloadAvailable = false;
			this.name = null;
			this.targetVersionUuid = null;
			this.url = null;
		}
		
		
		ReturnValue(String nameIn, String targetVersionUuidIn, String urlIn)
		{
			this.downloadAvailable = true;
			this.name = nameIn;
			this.targetVersionUuid = targetVersionUuidIn;
			this.url = urlIn;
		}
	}
	

	@Override
	public Object handleRequestWithParameters(HashMap<String, Object> paramsIn)
	{
		// parse our parameters
    	Object currentFirmwareUuid_raw = paramsIn.get("targetFirmwareUuid");
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
    		DownloadableFirmwareImage dfi = dbMan.getDownloadableFirmwareImageForFirmwareId(fi);
    		if( (dfi == null) || !dfi.hasStoredFirmwareFile() )
    		{
    			// no firmware image available
    			retVal = new ReturnValue();
    		}
    		else
    		{
    			// firmware version available...get our URL
    			retVal = new ReturnValue(dfi.getName(), dfi.getUuid(), dfi.getLimitedAccessUrl());
    		}
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
