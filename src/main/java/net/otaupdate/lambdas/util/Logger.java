package net.otaupdate.lambdas.util;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class Logger
{
	private static Logger SINGLETON = null;
	
	
	private final LambdaLogger logger;
	
	
	public Logger(Context contextIn)
	{
		this.logger = contextIn.getLogger();
		SINGLETON = this;
	}
	
	
	public void error(String stringIn)
	{
		this.logger.log("ERROR " + stringIn);
	}
	
	
	public void warn(String stringIn)
	{
		this.logger.log("WARN " + stringIn);
	}
	
	
	public void info(String stringIn)
	{
		this.logger.log("INFO " + stringIn);
	}
	
	
	public void debug(String stringIn)
	{
		this.logger.log("DEBUG " + stringIn);
	}
	
	
	public static Logger getSingleton()
	{
		return SINGLETON;
	}
}
