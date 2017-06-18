package net.otaupdate.lambdas.handlers;

import java.sql.SQLException;
import java.util.HashMap;

import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class CreateUserHandler extends AbstractMultiplexedRequestHandler
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
    	
    	String password = ObjectHelper.parseObjectFromMap(paramsIn, "password", String.class);
    	if( password == null )
    	{
    		ErrorManager.throwError(ErrorType.BadRequest, "problem parsing input parameters");
    	}
    	
    	// setup a connection to our database
    	DatabaseManager dbMan = null;
    	try{ dbMan = new DatabaseManager(); } 
    	catch( SQLException e ) { ErrorManager.throwError(ErrorType.ServerError, "problem connecting to database"); }
    	
    	boolean retVal = false;
    	
    	// make sure the user doesn't already exist
    	if( dbMan.doesUserExist(emailAddress) ) ErrorManager.throwError(ErrorType.Conflict, "user already exists");
    	
    	// if we made it here, user is new...try to add it
    	retVal = dbMan.addUser(emailAddress, password);
    	
    	// do some logging
		if( retVal ) Logger.getSingleton().debug(String.format("user created: '%s'", emailAddress));
		else Logger.getSingleton().warn(String.format("failed to create user: '%s'", emailAddress));
    	
		if( !retVal ) ErrorManager.throwError(ErrorType.ServerError, "problem adding user");
		
    	return null;
	}
}
