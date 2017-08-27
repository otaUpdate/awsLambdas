package net.otaupdate.lambdas;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.AbstractRequestHandler;
import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.handlers.api.devType.CreateDeviceTypeHandler;
import net.otaupdate.lambdas.handlers.api.devType.DeleteDeviceTypeHandler;
import net.otaupdate.lambdas.handlers.api.devType.GetDeviceTypesHandler;
import net.otaupdate.lambdas.handlers.api.devType.UpdateDeviceTypeHandler;
import net.otaupdate.lambdas.handlers.api.devType.devices.CreateDeviceHandler;
import net.otaupdate.lambdas.handlers.api.devType.devices.DeleteDeviceHandler;
import net.otaupdate.lambdas.handlers.api.devType.devices.GetDevicesHandler;
import net.otaupdate.lambdas.handlers.api.devType.devices.GetUnprovisionedProcessorsHandler;
import net.otaupdate.lambdas.handlers.api.fwImages.CreateFwImageHandler;
import net.otaupdate.lambdas.handlers.api.fwImages.DeleteFwImageHandler;
import net.otaupdate.lambdas.handlers.api.fwImages.GetFwImageUploadLinkHandler;
import net.otaupdate.lambdas.handlers.api.fwImages.GetFwImagesHandler;
import net.otaupdate.lambdas.handlers.api.fwImages.UpdateFwImageHandler;
import net.otaupdate.lambdas.handlers.api.organizations.CreateOrganizationHandler;
import net.otaupdate.lambdas.handlers.api.organizations.DeleteOrganizationHandler;
import net.otaupdate.lambdas.handlers.api.organizations.GetOrganizationsHandler;
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
import net.otaupdate.lambdas.handlers.testing.TestingLoginHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;


public class MainLambda implements RequestHandler<HashMap<?,?>, Object>
{	
	private static final String TAG = MainLambda.class.getSimpleName();
	private static final String API_ID_API = "jdamk5vbud";
	
	private static final Map<String, Class<? extends AbstractRequestHandler>> HANDLER_MAP = new HashMap<String, Class<? extends AbstractRequestHandler>>();
	static
	{
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "owzznb", "POST"),  CheckForUpdateHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "x0i3pr", "POST"),  GetFwDataHandler.class);
		
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "ymduq0", "POST"),   AddUserToOrgHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "nilj0n", "POST"),   CreateDeviceHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "5i14ml", "POST"),   CreateDeviceTypeHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "eyj0he", "POST"),   CreateFwImageHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "w4f4i0", "POST"),   CreateOrganizationHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "4dhrn9", "POST"),   CreateProcessorTypeHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "34fuko", "DELETE"), DeleteDeviceHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "2i366h", "DELETE"), DeleteDeviceTypeHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "qm0pbd", "DELETE"), DeleteFwImageHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "11jlcw", "DELETE"), DeleteOrganizationHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "t97iqg", "DELETE"), DeleteProcessorTypeHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "nilj0n", "GET"),    GetDevicesHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "5i14ml", "GET"),    GetDeviceTypesHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "eyj0he", "GET"),    GetFwImagesHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "dy2f54", "GET"),    GetFwImageUploadLinkHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "w4f4i0", "GET"),    GetOrganizationsHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "4dhrn9", "GET"),    GetProcessorTypesHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "xbu7rb", "GET"),    GetUnprovisionedProcessorsHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "ymduq0", "GET"),    GetUsersInOrgHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "od7spe", "POST"),   RemoveUserFromOrgHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "dfi3su", "POST"),   TestingLoginHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "2i366h", "POST"),   UpdateDeviceTypeHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "qm0pbd", "POST"),   UpdateFwImageHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "11jlcw", "POST"),   UpdateOrganizationHandler.class);
		HANDLER_MAP.put(generateHandlerMapString(API_ID_API, "t97iqg", "POST"),   UpdateProcessorTypeHandler.class);
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
			throw new BreakwallAwsException(ErrorType.ServerError, "problem parsing method information");
		}
		
		
		// now find our handler
		Logger.getSingleton().debug(TAG, String.format("resourceId: %s  method: %s", resourceId, httpMethod));
		Class<? extends AbstractRequestHandler> targetClass = HANDLER_MAP.get(generateHandlerMapString(apiId, resourceId, httpMethod));
		if( targetClass == null )
		{
			throw new BreakwallAwsException(ErrorType.ServerError, "unknown method");
		}
		
		
		// parse the parameters and body (last so we don't waste processor time on unknown methods)
		AwsPassThroughParameters params = AwsPassThroughParameters.getParametersFromLambdaInput(input);
		AwsPassThroughBody body = AwsPassThroughBody.getBodyFromLambdaInput(input);
		if( (params == null) || (body == null) )
		{
			throw new BreakwallAwsException(ErrorType.ServerError, "problem reorganizing parameters");
		}
		
		
		// create an instance of our handler
		AbstractRequestHandler handlerInstance = null;
		try
		{
			handlerInstance = targetClass.newInstance();
		} 
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new BreakwallAwsException(ErrorType.ServerError, "problem creating handler instance");
		}
		// handlerInstance should not be null at this point


		// let the handler try to parse the parameters
		if( !handlerInstance.parseAndValidateParameters(params, body) ) throw new BreakwallAwsException(ErrorType.BadRequest, "problem parsing input parameters");


		// now setup a connection to our database
		DatabaseManager dbMan = null;
		try{ dbMan = new DatabaseManager(); } 
		catch( SQLException e ) { throw new BreakwallAwsException(ErrorType.ServerError, e.getMessage()); }

		
		// authorized handler...check our authorization token first
		ExecutingUser execUser = null;
		if( handlerInstance instanceof AbstractAuthorizedRequestHandler )
		{
			String token = ObjectHelper.parseObjectFromMap(params.getHeaderParameters(), "Authorization", String.class);
			if( token == null ) throw new BreakwallAwsException(ErrorType.Unauthorized, "error parsing authorization token");
			
			// if we made it into our Lambda, API Gateway verifier should have verified the token's authenticity
			execUser = new ExecutingUser(token);
			
			// if we made it here, the user authorization token is valid...check whether the user is a super user
		}


		// now perform our operation
		Object retVal = null;
		try
		{
			// check what kind of handler we have
			if( handlerInstance instanceof AbstractAuthorizedRequestHandler )
			{
				retVal = ((AbstractAuthorizedRequestHandler)handlerInstance).processRequestWithDatabaseManager(dbMan, dbMan.getDslContext(), execUser);
			}
			else
			{
				retVal = ((AbstractUnauthorizedRequestHandler)handlerInstance).processRequestWithDatabase(dbMan, dbMan.getDslContext());
			}
		}
		catch(BreakwallAwsException e)
		{
			// rethrow it
			throw e;
		}
		catch(Exception e)
		{
			throw new BreakwallAwsException(ErrorType.ServerError, String.format("unhandled exception: '%s::%s'", e.getClass().getName(), e.getMessage()));
		}
		finally
		{
			dbMan.close();
		}

		return retVal;
	}
	
	
	private static String generateHandlerMapString(String apiIdIn, String resourceIdIn, String httpMethodIn)
	{
		return apiIdIn + resourceIdIn + httpMethodIn;
	}
}
