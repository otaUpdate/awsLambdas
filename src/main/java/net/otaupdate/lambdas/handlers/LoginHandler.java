package net.otaupdate.lambdas.handlers;


import java.sql.SQLException;
import java.util.HashMap;

import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class LoginHandler extends AbstractMultiplexedRequestHandler
{
	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final String token;
		
		ReturnValue(String tokenIn)
		{
			this.token = tokenIn;
		}
	}
	
	
	@Override
	public Object handleRequestWithParameters(HashMap<String, Object> paramsIn)
	{
		// parse our parameters
		String emailAddress = ObjectHelper.parseObjectFromMap(paramsIn, "emailAddress", String.class);
    	if( emailAddress == null )
    	{
    		ErrorManager.throwError(ErrorType.BadRequest, "problem parsing input parameters");
    	}
    	
    	String password = ObjectHelper.parseObjectFromMap(paramsIn, "password", String.class);
    	if( password == null )
    	{
    		ErrorManager.throwError(ErrorType.BadRequest, "problem parsing input parameters");
    	}
    	
    	// setup a connection to our database
    	DatabaseManager dbMan = null;
    	try{ dbMan = new DatabaseManager(); } 
    	catch( SQLException e ) { ErrorManager.throwError(ErrorType.ServerError, "problem connecting to database"); }
    	
    	String loginToken = dbMan.authorizeUser(emailAddress, password);
    	
    	// do some logging
		if( loginToken != null ) Logger.getSingleton().debug(String.format("user authorized: '%s'", emailAddress));
		else Logger.getSingleton().debug(String.format("user authorization failed: '%s'", emailAddress));
		
		// return accordingly
		if( loginToken == null ) ErrorManager.throwError(ErrorType.Unauthorized, "invalid username or password");
    	return new ReturnValue(loginToken);
	}
}
