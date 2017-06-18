package net.otaupdate.lambdas.handlers;

import java.util.HashMap;

import net.otaupdate.lambdas.util.ObjectHelper;

public abstract class AbstractMultiplexedRequestHandler
{
	public abstract Object handleRequestWithParameters(HashMap<String, Object> paramsIn);
	
	
	protected String parseAuthToken(HashMap<String, Object> paramsIn)
	{
		String retVal = ObjectHelper.parseObjectFromMap(paramsIn, "authToken", String.class);
		
		// should be prefixed with "Basic "
		String basicHeader = "Basic ";
		
		return ((retVal != null) && (retVal.length() > basicHeader.length())) ? retVal.substring(basicHeader.length()) : null; 
	}
}
