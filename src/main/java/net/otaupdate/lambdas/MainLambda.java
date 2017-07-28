package net.otaupdate.lambdas;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.jooq.types.UInteger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.AbstractRequestHandler;
import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.api.devType.CreateDeviceTypeHandler;
import net.otaupdate.lambdas.handlers.api.devType.DeleteDeviceTypeHandler;
import net.otaupdate.lambdas.handlers.api.devType.GetDeviceTypesHandler;
import net.otaupdate.lambdas.handlers.api.devType.UpdateDeviceTypeHandler;
import net.otaupdate.lambdas.handlers.api.devType.devices.CreateDeviceHandler;
import net.otaupdate.lambdas.handlers.api.devType.devices.DeleteDeviceHandler;
import net.otaupdate.lambdas.handlers.api.devType.devices.GetDevicesHandler;
import net.otaupdate.lambdas.handlers.api.fwImages.CreateFwImageHandler;
import net.otaupdate.lambdas.handlers.api.fwImages.DeleteFwImageHandler;
import net.otaupdate.lambdas.handlers.api.fwImages.GetFwImageUploadLinkHandler;
import net.otaupdate.lambdas.handlers.api.fwImages.GetFwImagesHandler;
import net.otaupdate.lambdas.handlers.api.fwImages.UpdateFwImageHandler;
import net.otaupdate.lambdas.handlers.api.login.CreateUserHandler;
import net.otaupdate.lambdas.handlers.api.login.LoginHandler;
import net.otaupdate.lambdas.handlers.api.organizations.CreateOrganizationHandler;
import net.otaupdate.lambdas.handlers.api.organizations.DeleteOrganizationHandler;
import net.otaupdate.lambdas.handlers.api.organizations.GetOrganizations;
import net.otaupdate.lambdas.handlers.api.organizations.UpdateOrganizationHandler;
import net.otaupdate.lambdas.handlers.api.organizations.users.AddUserToOrgHandler;
import net.otaupdate.lambdas.handlers.api.organizations.users.GetUsersInOrgHandler;
import net.otaupdate.lambdas.handlers.api.organizations.users.RemoveUserFromOrgHandler;
import net.otaupdate.lambdas.handlers.api.procType.CreateProcessorTypeHandler;
import net.otaupdate.lambdas.handlers.api.procType.DeleteProcessorTypeHandler;
import net.otaupdate.lambdas.handlers.api.procType.GetProcessorTypesHandler;
import net.otaupdate.lambdas.handlers.api.procType.UpdateProcessorTypeHandler;
import net.otaupdate.lambdas.handlers.devs.CheckForUpdateHandler;
import net.otaupdate.lambdas.handlers.devs.GetFwDataHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorManagerException;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class MainLambda implements RequestHandler<HashMap<?,?>, Object>
{	
	private static final String API_ID_DEVS = "wvanw383h9";
	private static final String API_ID_API = "jdamk5vbud";
	
	private static final Map<String, Class<? extends AbstractRequestHandler>> HANDLER_MAP = new HashMap<String, Class<? extends AbstractRequestHandler>>();
	static
	{
		HANDLER_MAP.put(generateHandlerMapString(API_ID_DEVS, "ks18rv", "POST"), CheckForUpdateHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_DEVS, "kj4yeq", "POST"), GetFwDataHandler.class);
		
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "ymduq0", "POST"), AddUserToOrgHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "nilj0n", "POST"), CreateDeviceHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "5i14ml", "POST"), CreateDeviceTypeHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "eyj0he", "POST"), CreateFwImageHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "w4f4i0", "POST"), CreateOrganizationHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "4dhrn9", "POST"), CreateProcessorTypeHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "immv9r", "POST"), CreateUserHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "34fuko", "DELETE"), DeleteDeviceHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "2i366h", "DELETE"), DeleteDeviceTypeHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "qm0pbd", "DELETE"), DeleteFwImageHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "11jlcw", "DELETE"), DeleteOrganizationHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "t97iqg", "DELETE"), DeleteProcessorTypeHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "nilj0n", "GET"), GetDevicesHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "5i14ml", "GET"), GetDeviceTypesHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "eyj0he", "GET"), GetFwImagesHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "dy2f54", "GET"), GetFwImageUploadLinkHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "w4f4i0", "GET"), GetOrganizations.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "4dhrn9", "GET"), GetProcessorTypesHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "ymduq0", "GET"), GetUsersInOrgHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "w5mumg", "POST"), LoginHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "od7spe", "POST"), RemoveUserFromOrgHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "2i366h", "POST"), UpdateDeviceTypeHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "qm0pbd", "POST"), UpdateFwImageHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "11jlcw", "POST"), UpdateOrganizationHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "t97iqg", "POST"), UpdateProcessorTypeHandler.class);
	}
	

	@Override
	public Object handleRequest(HashMap<?, ?> input, Context contextIn)
	{
		// initialize our logger and stop JOOQ logging
		new Logger(contextIn);
		System.setProperty("org.jooq.no-logo", "true");
		
		
		// figure out our apiId, resourceId, and httpMethod
		String apiId = ObjectHelper.parseObjectFromNestedMaps(input, new String[]{"context",  "api-id"}, String.class);
		String resourceId = ObjectHelper.parseObjectFromNestedMaps(input, new String[]{"context",  "resource-id"}, String.class);
		String httpMethod = ObjectHelper.parseObjectFromNestedMaps(input, new String[]{"context", "http-method"}, String.class);
		if( (apiId == null) || (resourceId == null) || (httpMethod == null) )
		{
			ErrorManager.throwError(ErrorType.ServerError, "problem parsing method information");
			return null;
		}
		
		
		// now find our handler
		Class<? extends AbstractRequestHandler> targetClass = HANDLER_MAP.get(generateHandlerMapString(apiId, resourceId, httpMethod));
		if( targetClass == null )
		{
			ErrorManager.throwError(ErrorType.ServerError, "unknown method");
			return null;
		}
		
		
		// parse the parameters and body (last so we don't waste processor time on unknown methods)
		AwsPassThroughParameters params = AwsPassThroughParameters.getParametersFromLambdaInput(input);
		AwsPassThroughBody body = AwsPassThroughBody.getBodyFromLambdaInput(input);
		if( (params == null) || (body == null) )
		{
			ErrorManager.throwError(ErrorType.ServerError, "problem reorganizing parameters");
			return null;
		}
		
		
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
		if( !handlerInstance.parseAndValidateParameters(params, body) ) ErrorManager.throwError(ErrorType.BadRequest, "problem parsing input parameters");


		// now setup a connection to our database
		DatabaseManager dbMan = null;
		try{ dbMan = new DatabaseManager(); } 
		catch( SQLException e ) { ErrorManager.throwError(ErrorType.ServerError, "problem connecting to database"); }

		
		// authorized handler...check our authorization token first
		UInteger userId = null;
		if( handlerInstance instanceof AbstractAuthorizedRequestHandler )
		{
			String authToken = this.parseAuthToken(params);
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
				retVal = ((AbstractAuthorizedRequestHandler)handlerInstance).processRequestWithDatabaseManager(dbMan, dbMan.getDslContext(), userId);
			}
			else
			{
				retVal = ((AbstractUnauthorizedRequestHandler)handlerInstance).processRequestWithDatabase(dbMan, dbMan.getDslContext());
			}
		}
		catch(ErrorManagerException e)
		{
			// rethrow it
			throw e;
		}
		catch(Exception e)
		{
			ErrorManager.throwError(ErrorType.ServerError, String.format("unhandled exception: '%s::%s'", e.getClass().getName(), e.getMessage()));
		}
		finally
		{
			dbMan.close();
		}

		return retVal;
	}


	private String parseAuthToken(AwsPassThroughParameters paramsIn)
	{
		String retVal = ObjectHelper.parseObjectFromMap(paramsIn.getHeaderParameters(), "Authorization", String.class);

		// should be prefixed with "Basic "
		String basicHeader = "Basic ";

		return ((retVal != null) && (retVal.length() > basicHeader.length())) ? retVal.substring(basicHeader.length()) : null; 
	}
	
	
	private static String generateHandlerMapString(String apiIdIn, String resourceIdIn, String httpMethodIn)
	{
		return apiIdIn + resourceIdIn + httpMethodIn;
	}

}
