package net.otaupdate.lambdas;

import java.util.HashMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import net.otaupdate.lambdas.handlers.AbstractMultiplexedRequestHandler;
import net.otaupdate.lambdas.handlers.CheckForUpdateHandler;
import net.otaupdate.lambdas.handlers.GetFirmwareDownloadLinkHandler;
import net.otaupdate.lambdas.handlers.GetLatestVersionHandler;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class MainLambda implements RequestHandler<HashMap<?,?>, Object>
{
	private static final HashMap<String, Class<? extends AbstractMultiplexedRequestHandler>> HANDLER_MAP = new HashMap<String, Class<? extends AbstractMultiplexedRequestHandler>>();
	static
	{
		HANDLER_MAP.put("checkForUpdate", CheckForUpdateHandler.class);
		HANDLER_MAP.put("getFirmwareDownloadLink", GetFirmwareDownloadLinkHandler.class);
		HANDLER_MAP.put("getLatestVersion", GetLatestVersionHandler.class);
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
    	Class<? extends AbstractMultiplexedRequestHandler> targetClass = HANDLER_MAP.get(method);
    	if( targetClass == null )
    	{
    		ErrorManager.throwError(ErrorType.ServerError, "incorrect transformed parameters");
    		return null;
    	}
    	Logger.getSingleton().debug(String.format("targetClass: '%s'", targetClass.getName()));
    	
    	// create an instance of our handler and run it
		AbstractMultiplexedRequestHandler handlerInstance = null;
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
		return handlerInstance.handleRequestWithParameters(methodParams);
	}

}
