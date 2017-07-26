package net.otaupdate.lambdas;

import java.util.Map;

import net.otaupdate.lambdas.util.Logger;


public class AwsPassThroughBody
{
	private static final String TAG = AwsPassThroughBody.class.getSimpleName();
	
	
	private final Object body;
	
	
	private AwsPassThroughBody(Object bodyIn)
	{
		this.body = bodyIn;
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getBodyAsJsonMap()
	{
		Map<String, Object> retVal = null;
		if( body instanceof Map<?,?> ) retVal = (Map<String, Object>)this.body;
		return retVal;
	}
	
	
	public static AwsPassThroughBody getBodyFromLambdaInput(Map<?, ?> input)
	{
		// parse the body
		Object body_raw = input.get("body-json");
		Logger.getSingleton().debug(TAG, String.format("body: '%s'", body_raw.toString()));
		
		// create our return value
		return new AwsPassThroughBody(body_raw);
	}
}
