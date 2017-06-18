package net.otaupdate.lambdas.handlers.organization;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.otaupdate.lambdas.handlers.AbstractMultiplexedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class ListUsersInOrg extends AbstractMultiplexedRequestHandler
{
	@Override
	public Object handleRequestWithParameters(HashMap<String, Object> paramsIn)
	{
		// parse our parameters
    	String organizationUuid = ObjectHelper.parseObjectFromMap(paramsIn, "organizationUuid", String.class);
    	if( organizationUuid == null )
    	{
    		ErrorManager.throwError(ErrorType.BadRequest, "problem parsing input parameters");
    	}
    	
    	String authToken = this.parseAuthToken(paramsIn);
    	
    	// setup a connection to our database
    	DatabaseManager dbMan = null;
    	try{ dbMan = new DatabaseManager(); } 
    	catch( SQLException e ) { ErrorManager.throwError(ErrorType.ServerError, "problem connecting to database"); }
    	
    	// get the userId (and make sure the authToken is still valid)
    	Integer userId = null;
    	if( (authToken == null) || (userId = dbMan.getUserIdForLoginToken(authToken)) == null )
    	{
    		ErrorManager.throwError(ErrorType.Unauthorized, "invalid authorization token for resource");
    	}
    	
    	// make user the user is actually a member of this organization
    	if( !dbMan.isUserPartOfOrganization(userId, organizationUuid) ) ErrorManager.throwError(ErrorType.Unauthorized, "not authorized to access this resource");
    	
    	// do our update
    	List<Map<String, String>> retVal = dbMan.listUsersInOrganization(organizationUuid);
    	
		if( retVal == null ) ErrorManager.throwError(ErrorType.BadRequest, "problem listing users");
		
    	return retVal;
	}
}
