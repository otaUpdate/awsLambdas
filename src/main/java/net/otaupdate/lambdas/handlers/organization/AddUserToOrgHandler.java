package net.otaupdate.lambdas.handlers.organization;

import java.sql.SQLException;
import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractMultiplexedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class AddUserToOrgHandler extends AbstractMultiplexedRequestHandler
{
	@Override
	public Object handleRequestWithParameters(HashMap<String, Object> paramsIn)
	{
		// parse our parameters
		String emailAddress = ObjectHelper.parseObjectFromMap(paramsIn, "emailAddress", String.class);
    	if( emailAddress == null )
    	{
    		ErrorManager.throwError(ErrorType.BadRequest, "problem parsing input parameters");
    	}
    	
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
    	if( (authToken == null) || (dbMan.getUserIdForLoginToken(authToken) == null) )
    	{
    		ErrorManager.throwError(ErrorType.Unauthorized, "invalid authorization token for resource");
    	}
    	
    	// do our update
    	boolean retVal = dbMan.addUserToOrganization(emailAddress, organizationUuid);
    	
    	// do some logging
		if( retVal ) Logger.getSingleton().debug(String.format("added user %s to organzation %s", emailAddress, organizationUuid));
		else Logger.getSingleton().debug(String.format("failed to add user %s to organzation %s", emailAddress, organizationUuid));
    	
		if( !retVal ) ErrorManager.throwError(ErrorType.BadRequest, "problem adding user");
		
    	return null;
	}
}
