package net.otaupdate.lambdas.handlers;

import java.util.HashMap;

public abstract class AbstractMultiplexedRequestHandler
{
	public abstract Object handleRequestWithParameters(HashMap<String, Object> paramsIn);
}
