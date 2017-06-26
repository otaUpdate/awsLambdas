package net.otaupdate.lambdas.handlers.organization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class ListUsersInOrg extends AbstractAuthorizedRequestHandler
{
	private String organizationUuid = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		// parse our parameters
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
    	List<Map<String, String>> retVal = dbManIn.listUsersInOrganization(this.organizationUuid);
    	
		if( retVal == null ) ErrorManager.throwError(ErrorType.BadRequest, "problem listing users");
		
    	return retVal;
	}
}
