package net.otaupdate.lambdas.handlers;

import java.util.HashMap;


public abstract class AbstractRequestHandler
{
	public abstract boolean parseAndValidateParameters(HashMap<String, Object> paramsIn);
}
