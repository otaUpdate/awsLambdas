package net.otaupdate.lambdas.handlers.organization;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractMultiplexedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class RemoveUserFromOrgHandler extends AbstractMultiplexedRequestHandler
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
    	try{ emailAddress = URLDecoder.decode(emailAddress, "UTF-8"); } catch (UnsupportedEncodingException e1) { }
    	
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
    	boolean retVal = dbMan.removeUserFromOrganization(emailAddress, organizationUuid);
    	
    	// do some logging
		if( retVal ) Logger.getSingleton().debug(String.format("removed user %s from organzation %s", emailAddress, organizationUuid));
		else Logger.getSingleton().debug(String.format("failed to remove user %s from organzation %s", emailAddress, organizationUuid));
    	
		if( !retVal ) ErrorManager.throwError(ErrorType.BadRequest, "problem removing user");
		
    	return null;
	}
}
