package net.otaupdate.lambdas.handlers.login;

import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class LoginHandler extends AbstractUnauthorizedRequestHandler
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
	
	
	private String emailAddress = null;
	private String password = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		// parse our parameters
		this.emailAddress = ObjectHelper.parseObjectFromMap(paramsIn, "emailAddress", String.class);
    	if( this.emailAddress == null ) return false;
    	
    	this.password = ObjectHelper.parseObjectFromMap(paramsIn, "password", String.class);
    	if( this.password == null ) return false;
    	
    	return true;
	}
	
	
	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn)
	{
    	String loginToken = dbManIn.authorizeUser(this.emailAddress, this.password);
    	
    	// do some logging
		if( loginToken != null ) Logger.getSingleton().debug(String.format("user authorized: '%s'", this.emailAddress));
		else Logger.getSingleton().debug(String.format("user authorization failed: '%s'", this.emailAddress));
		
		// return accordingly
		if( loginToken == null ) ErrorManager.throwError(ErrorType.Unauthorized, "invalid username or password");
    	return new ReturnValue(loginToken);
	}
}
