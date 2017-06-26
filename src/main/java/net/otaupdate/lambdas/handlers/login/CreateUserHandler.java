package net.otaupdate.lambdas.handlers.login;

import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class CreateUserHandler extends AbstractUnauthorizedRequestHandler
{	
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
    	boolean retVal = false;
    	
    	// make sure the user doesn't already exist
    	if( dbManIn.doesUserExist(this.emailAddress) ) ErrorManager.throwError(ErrorType.Conflict, "user already exists");
    	
    	// if we made it here, user is new...try to add it
    	retVal = dbManIn.addUser(this.emailAddress, this.password);
    	
    	// do some logging
		if( retVal ) Logger.getSingleton().debug(String.format("user created: '%s'", this.emailAddress));
		else Logger.getSingleton().warn(String.format("failed to create user: '%s'", this.emailAddress));
    	
		if( !retVal ) ErrorManager.throwError(ErrorType.ServerError, "problem adding user");
		
    	return null;
	}
}