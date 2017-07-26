package net.otaupdate.lambdas;

import java.util.HashMap;
import java.util.Map;

import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;


public class AwsPassThroughParameters
{
	private static final String TAG = AwsPassThroughParameters.class.getSimpleName();
	
	
	private final Map<String, Object> params_path;
	private final Map<String, Object> params_queryString;
	private final Map<String, Object> params_header;
	
	
	private AwsPassThroughParameters(Map<String, Object> params_pathIn, Map<String, Object> params_queryStringIn, Map<String, Object> params_headerIn)
	{
		this.params_path = params_pathIn;
		this.params_queryString = params_queryStringIn;
		this.params_header = params_headerIn;
	}
	
	
	public Map<String, Object> getPathParameters()
	{
		return this.params_path;
	}
	
	
	public Map<String, Object> getQueryStringParameters()
	{
		return this.params_queryString;
	}
	
	
	public Map<String, Object> getHeaderParameters()
	{
		return this.params_header;
	}
	
	
	public static AwsPassThroughParameters getParametersFromLambdaInput(HashMap<?, ?> input)
	{
		// path parameters
		@SuppressWarnings("unchecked")
		Map<String, Object> params_path = (Map<String, Object>)ObjectHelper.parseObjectFromNestedMaps(input, new String[]{"params", "path"}, Map.class);
		if( params_path == null ) return null;
		Logger.getSingleton().debug(TAG, String.format("params_path: '%s'", params_path.toString()));
		
		
		// queryString parameters
		@SuppressWarnings("unchecked")
		Map<String, Object> params_queryString = (Map<String, Object>)ObjectHelper.parseObjectFromNestedMaps(input, new String[]{"params", "querystring"}, Map.class);
		if( params_queryString == null ) return null;
		Logger.getSingleton().debug(TAG, String.format("params_queryString: '%s'", params_queryString.toString()));
		
		
		// header parameters
		@SuppressWarnings("unchecked")
		Map<String, Object> params_header = (Map<String, Object>)ObjectHelper.parseObjectFromNestedMaps(input, new String[]{"params", "header"}, Map.class);
		if( params_header == null ) return null;
		Logger.getSingleton().debug(TAG, String.format("params_header: '%s'", params_header.toString()));
		
		
		// create our return value
		return new AwsPassThroughParameters(params_path, params_queryString, params_header);
	}
}
