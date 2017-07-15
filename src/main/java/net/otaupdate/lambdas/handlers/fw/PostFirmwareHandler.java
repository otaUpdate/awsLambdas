package net.otaupdate.lambdas.handlers.fw;

import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class PostFirmwareHandler extends AbstractAuthorizedRequestHandler
{	

	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final String uuid;
		
		ReturnValue(String uuidIn)
		{
			this.uuid = uuidIn;
		}
	}
	
	
	private String name = null;
	private String organizationUuid = null;
	private String deviceUuid = null;
	private String processorUuid = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		// parse our parameters
		this.name = ObjectHelper.parseObjectFromMap(paramsIn, "name", String.class);
    	if( this.name == null ) return false;
    	
    	this.organizationUuid = ObjectHelper.parseObjectFromMap(paramsIn, "organizationUuid", String.class);
    	if( this.organizationUuid == null ) return false;
    	
    	this.deviceUuid = ObjectHelper.parseObjectFromMap(paramsIn, "deviceUuid", String.class);
    	if( this.deviceUuid == null ) return false;
    	
    	this.processorUuid = ObjectHelper.parseObjectFromMap(paramsIn, "processorUuid", String.class);
    	if( this.processorUuid == null ) return false;
    	
    	return true;
	}
	
	
	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, int userIdIn)
	{    	
    	// make user the user is actually a member of this organization
    	if( !dbManIn.isUserPartOfOrganization(userIdIn, this.organizationUuid) ) ErrorManager.throwError(ErrorType.Unauthorized, "not authorized to access this resource");
    	
    	// if we made it here, user is part of this organization...proceed

		// we're creating a new firmware image
		String fwUuid = dbManIn.insertFirmwareImageGetUuid(this.name, this.processorUuid, this.deviceUuid, this.organizationUuid);
		
    	// do some logging
		if( fwUuid != null ) Logger.getSingleton().debug(String.format("firmware image created: '%s'", fwUuid));
		else Logger.getSingleton().warn(String.format("failed to create firmware image '%s' for processor '%s'", this.name, this.processorUuid));
    	if( fwUuid == null ) ErrorManager.throwError(ErrorType.ServerError, "error creating/updating firmware image");
		
    	// do some logging
    	Logger.getSingleton().debug(String.format("created fw '%s' '%s'", this.name, fwUuid));
    	
    	return new ReturnValue(fwUuid); 
	}
}
