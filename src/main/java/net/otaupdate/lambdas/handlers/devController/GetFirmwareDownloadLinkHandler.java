package net.otaupdate.lambdas.handlers.devController;

import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.AbstractRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.FirmwareImage;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.FirmwareIdentifier;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class GetFirmwareDownloadLinkHandler extends AbstractAuthorizedRequestHandler
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
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		return true;
	}
	

	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, int userIdIn)
	{
//		// parse our parameters
//    	Object currentFirmwareUuid_raw = paramsIn.get("targetFirmwareUuid");
//    	if( (currentFirmwareUuid_raw == null) || !(currentFirmwareUuid_raw instanceof String) )
//    	{
//    		ErrorManager.throwError(ErrorType.BadRequest, "problem parsing input parameters");
//    	}
//    
//    	// get our firmware identifier
//    	FirmwareIdentifier fi = new FirmwareIdentifier((String)currentFirmwareUuid_raw);
//    	
//    	
//    	ReturnValue retVal = new ReturnValue();
//    	FirmwareImage dfi = dbManIn.getDownloadableFirmwareImageForFirmwareId(fi);
//		if( (dfi != null) && dfi.hasStoredFirmwareFile() )
//		{
//			// firmware version available...get our URL
//			retVal = new ReturnValue(dfi.getName(), dfi.getUuid(), dfi.getLimitedAccessDownloadUrl());
//		}
//    	return retVal;
		
		return null;
	}

}
