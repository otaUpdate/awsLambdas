package net.otaupdate.lambdas;

import java.sql.SQLException;
import java.util.HashMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.AbstractRequestHandler;
import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.devController.CheckForUpdateHandler;
import net.otaupdate.lambdas.handlers.devController.GetFirmwareDownloadLinkHandler;
import net.otaupdate.lambdas.handlers.devController.GetLatestVersionHandler;
import net.otaupdate.lambdas.handlers.fw.DeleteFirmwareHandler;
import net.otaupdate.lambdas.handlers.fw.PostFirmwareHandler;
import net.otaupdate.lambdas.handlers.general.CreateHandler;
import net.otaupdate.lambdas.handlers.general.DeleteHandler;
import net.otaupdate.lambdas.handlers.general.SelectHandler;
import net.otaupdate.lambdas.handlers.general.UpdateHandler;
import net.otaupdate.lambdas.handlers.login.CreateUserHandler;
import net.otaupdate.lambdas.handlers.login.LoginHandler;
import net.otaupdate.lambdas.handlers.organization.AddUserToOrgHandler;
import net.otaupdate.lambdas.handlers.organization.ListUsersInOrg;
import net.otaupdate.lambdas.handlers.organization.RemoveUserFromOrgHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class MainLambda implements RequestHandler<HashMap<?,?>, Object>
{
	private static final HashMap<String, Class<? extends AbstractRequestHandler>> HANDLER_MAP = new HashMap<String, Class<? extends AbstractRequestHandler>>();
	static
	{
		HANDLER_MAP.put("checkForUpdate", CheckForUpdateHandler.class);
		HANDLER_MAP.put("getFirmwareDownloadLink", GetFirmwareDownloadLinkHandler.class);
		HANDLER_MAP.put("getLatestVersion", GetLatestVersionHandler.class);

		HANDLER_MAP.put("select", SelectHandler.class);
		HANDLER_MAP.put("create", CreateHandler.class);
		HANDLER_MAP.put("update", UpdateHandler.class);
		HANDLER_MAP.put("delete", DeleteHandler.class);
		
		HANDLER_MAP.put("createUser", CreateUserHandler.class);
		HANDLER_MAP.put("login", LoginHandler.class);
		
		HANDLER_MAP.put("addUserToOrg", AddUserToOrgHandler.class);
		HANDLER_MAP.put("removeUserFromOrg", RemoveUserFromOrgHandler.class);
		HANDLER_MAP.put("listUsersInOrg", ListUsersInOrg.class);
		
		HANDLER_MAP.put("postFwImage", PostFirmwareHandler.class);
		HANDLER_MAP.put("deleteFwImage", DeleteFirmwareHandler.class);
	}
	

	@Override
	public Object handleRequest(HashMap<?, ?> input, Context contextIn)
	{
		// initialize our logger
    	new Logger(contextIn);
    	
    	// parse our target method
    	Object method_raw = input.get("method");
    	if( (method_raw == null) || !(method_raw instanceof String) )
    	{
    		ErrorManager.throwError(ErrorType.ServerError, "incorrect transformed parameters");
    		return null;
    	}
    	String method = (String)method_raw;
    	Logger.getSingleton().debug(String.format("method: '%s'", method));
    	
    	
    	// parse the parameters for said method
    	Object methodParams_raw = input.get("params");
    	if( (methodParams_raw == null) || !(methodParams_raw instanceof HashMap<?,?>) )
    	{
    		ErrorManager.throwError(ErrorType.ServerError, "incorrect transformed parameters");
    		return null;
    	}
    	@SuppressWarnings("unchecked")
		HashMap<String, Object> methodParams = (HashMap<String, Object>)methodParams_raw;
    	Logger.getSingleton().debug(String.format("params: '%s'", methodParams.toString()));
    	
    	
    	// figure out if we can handle this method
    	Class<? extends AbstractRequestHandler> targetClass = HANDLER_MAP.get(method);
    	if( targetClass == null )
    	{
    		ErrorManager.throwError(ErrorType.ServerError, "unknown method");
    		return null;
    	}
    	Logger.getSingleton().debug(String.format("targetClass: '%s'", targetClass.getName()));
    	
    	
    	// create an instance of our handler
		AbstractRequestHandler handlerInstance = null;
		try
		{
			handlerInstance = targetClass.newInstance();
		} 
		catch (InstantiationException | IllegalAccessException e)
		{
			ErrorManager.throwError(ErrorType.ServerError, "problem creating handler instance");
    		return null;
		}
		// handlerInstance should not be null at this point
		
		
		// let the handler try to parse the parameters
		if( !handlerInstance.parseAndValidateParameters(methodParams) ) ErrorManager.throwError(ErrorType.BadRequest, "problem parsing input parameters");
		
		
    	// now setup a connection to our database
    	DatabaseManager dbMan = null;
    	try{ dbMan = new DatabaseManager(); } 
    	catch( SQLException e ) { ErrorManager.throwError(ErrorType.ServerError, "problem connecting to database"); }

    	
    	// authorized handler...check our authorization token first
		Integer userId = null;
		if( handlerInstance instanceof AbstractAuthorizedRequestHandler )
		{
			String authToken = this.parseAuthToken(methodParams);
			if( (authToken == null) || ((userId = dbMan.getUserIdForLoginToken(authToken)) == null) )
			{
				ErrorManager.throwError(ErrorType.Unauthorized, "invalid authorization token");
			}
			// if we made it here, the user authorization token is valid
		}
		
    	
    	// now perform our operation
    	Object retVal = null;
    	try
    	{
    		// check what kind of handler we have
    		if( handlerInstance instanceof AbstractAuthorizedRequestHandler )
    		{
    			retVal = ((AbstractAuthorizedRequestHandler)handlerInstance).processRequestWithDatabaseManager(dbMan, userId.intValue());
    		}
    		else
    		{
    			retVal = ((AbstractUnauthorizedRequestHandler)handlerInstance).processRequestWithDatabaseManager(dbMan);
    		}
    	}
    	catch(Exception e)
    	{
    		ErrorManager.throwError(ErrorType.ServerError, "unhandled exception");
    	}
    	finally
    	{
    		dbMan.close();
    	}
    	
    	return retVal;
	}
	
	
	private String parseAuthToken(HashMap<String, Object> paramsIn)
	{
		String retVal = ObjectHelper.parseObjectFromMap(paramsIn, "authToken", String.class);
		
		// should be prefixed with "Basic "
		String basicHeader = "Basic ";
		
		return ((retVal != null) && (retVal.length() > basicHeader.length())) ? retVal.substring(basicHeader.length()) : null; 
	}

}
