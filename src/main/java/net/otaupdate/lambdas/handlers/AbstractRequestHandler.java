package net.otaupdate.lambdas.handlers;


import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;


public abstract class AbstractRequestHandler
{
	public abstract boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn);
}
