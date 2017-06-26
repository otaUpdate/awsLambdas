package net.otaupdate.lambdas.handlers.organization;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class RemoveUserFromOrgHandler extends AbstractAuthorizedRequestHandler
{
	private String emailAddress = null;
	private String organizationUuid = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		// parse our parameters
		this.emailAddress = ObjectHelper.parseObjectFromMap(paramsIn, "emailAddress", String.class);
    	if( this.emailAddress == null ) return false;
    	try{ this.emailAddress = URLDecoder.decode(this.emailAddress, "UTF-8"); } catch (UnsupportedEncodingException e1) { return false; }
    	
    	this.organizationUuid = ObjectHelper.parseObjectFromMap(paramsIn, "organizationUuid", String.class);
    	if( this.organizationUuid == null ) return false;
    	
    	return true;
	}
	
	
	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, int userIdIn)
	{
    	// make user the user is actually a member of this organization
    	if( !dbManIn.isUserPartOfOrganization(userIdIn, this.organizationUuid) ) ErrorManager.throwError(ErrorType.Unauthorized, "not authorized to access this resource");
    	
    	// do our update
    	boolean retVal = dbManIn.removeUserFromOrganization(this.emailAddress, this.organizationUuid);
    	
    	// do some logging
		if( retVal ) Logger.getSingleton().debug(String.format("removed user %s from organzation %s", this.emailAddress, this.organizationUuid));
		else Logger.getSingleton().debug(String.format("failed to remove user %s from organzation %s", this.emailAddress, this.organizationUuid));
    	
		if( !retVal ) ErrorManager.throwError(ErrorType.BadRequest, "problem removing user");
		
    	return null;
	}
}
