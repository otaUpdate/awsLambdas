package net.otaupdate.lambdas.handlers.fw;

import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.S3Helper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class DeleteFirmwareHandler extends AbstractAuthorizedRequestHandler
{	
	private String organizationUuid = null;
	private String deviceUuid = null;
	private String processorUuid = null;
	private String firmwareUuid = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{    	
    	this.organizationUuid = ObjectHelper.parseObjectFromMap(paramsIn, "organizationUuid", String.class);
    	if( this.organizationUuid == null ) return false;
    	
    	this.deviceUuid = ObjectHelper.parseObjectFromMap(paramsIn, "deviceUuid", String.class);
    	if( this.deviceUuid == null ) return false;
    	
    	this.processorUuid = ObjectHelper.parseObjectFromMap(paramsIn, "processorUuid", String.class);
    	if( this.processorUuid == null ) return false;
    	
    	this.firmwareUuid = ObjectHelper.parseObjectFromMap(paramsIn, "fwUuid", String.class);
    	if( this.firmwareUuid == null ) return false;
    	
    	return true;
	}
	
	
	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, int userIdIn)
	{    	
    	// make user the user is actually a member of this organization
    	if( !dbManIn.isUserPartOfOrganization(userIdIn, this.organizationUuid) ) ErrorManager.throwError(ErrorType.Unauthorized, "not authorized to access this resource");
    	
    	// always delete the s3 key first so we don't leave any dangling files
    	// billing-wise we'd prefer a dangling database entry than an S3 entry
    	Logger.getSingleton().debug(String.format("deleting firmware image '%s'", this.firmwareUuid));
    	S3Helper.deleteImageForFirmwareWithUuid(this.firmwareUuid);
    	
    	// now delete the database entry
    	Logger.getSingleton().debug(String.format("image '%s' deleted, removing database entry", this.firmwareUuid));
    	boolean retVal = dbManIn.deleteFirmwareImage(this.firmwareUuid, this.processorUuid, this.deviceUuid, this.organizationUuid);
    	if( !retVal ) Logger.getSingleton().debug(String.format("error deleting firmware image '%s'", this.firmwareUuid));
		
		return null;
	}
}
