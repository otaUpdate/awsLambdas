package net.otaupdate.lambdas.handlers.fw;

import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.S3Helper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class GetUploadLinkHandler extends AbstractAuthorizedRequestHandler
{	

	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final String link;
		
		ReturnValue(String linkIn)
		{
			this.link = linkIn;
		}
	}
	
	
	private String organizationUuid = null;
	private String deviceUuid = null;
	private String processorUuid = null;
	private String fwUuid = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		// parse our parameters    	
    	this.organizationUuid = ObjectHelper.parseObjectFromMap(paramsIn, "organizationUuid", String.class);
    	if( this.organizationUuid == null ) return false;
    	
    	this.deviceUuid = ObjectHelper.parseObjectFromMap(paramsIn, "deviceUuid", String.class);
    	if( this.deviceUuid == null ) return false;
    	
    	this.processorUuid = ObjectHelper.parseObjectFromMap(paramsIn, "processorUuid", String.class);
    	if( this.processorUuid == null ) return false;
    	
    	this.fwUuid = ObjectHelper.parseObjectFromMap(paramsIn, "fwUuid", String.class);
    	if( this.fwUuid == null ) return false;
    	
    	return true;
	}
	
	
	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, int userIdIn)
	{    	
    	// make user the user is actually a member of this organization
    	if( !dbManIn.isUserPartOfOrganization(userIdIn, this.organizationUuid) ) ErrorManager.throwError(ErrorType.Unauthorized, "not authorized to access this resource");
    	
    	// if we made it here, user is part of this organization...proceed

		// we're creating a new firmware image
    	String url = S3Helper.getLimitedAccessUploadUrlForFirmwareWithUuid(this.fwUuid);
    	if( url == null ) ErrorManager.throwError(ErrorType.BadRequest, "unable to generate url for given parameters");
    	
		return new ReturnValue(url);
	}
}
