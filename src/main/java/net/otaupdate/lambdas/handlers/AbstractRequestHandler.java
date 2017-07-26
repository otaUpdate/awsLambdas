package net.otaupdate.lambdas.handlers;

import java.util.HashMap;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;


public abstract class AbstractRequestHandler
{
	@Deprecated
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn) { return true; }
	
	
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn) { return true; }
}
