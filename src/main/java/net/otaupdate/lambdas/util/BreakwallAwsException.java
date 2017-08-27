package net.otaupdate.lambdas.util;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BreakwallAwsException extends RuntimeException
{
	private static final long serialVersionUID = -4604895873322676529L;
	private static final String TAG = BreakwallAwsException.class.getSimpleName();
	
	public enum ErrorType
	{
		BadRequest("BadRequest"),
		Unauthorized("Unauthorized"),
		Conflict("Conflict"),
		ServerError("ServerError");

		private final String stringRep;
		private ErrorType(String stringIn) { this.stringRep = stringIn; }
		private String getEscapedString() { return String.format("[%s]", this.stringRep); }
	}
	
	
	private final String message;


	public BreakwallAwsException(ErrorType errTypeIn, String reasonCodeIn, String messageIn, String moreInfoIn)
	{
		Map<String, Object> msgJsonMap = new HashMap<String, Object>();
		msgJsonMap.put("errorType", errTypeIn.getEscapedString());
		msgJsonMap.put("reasonCode", reasonCodeIn);
		msgJsonMap.put("message", messageIn);
		msgJsonMap.put("moreInfo", moreInfoIn);
		
		// set a reasonable default so we always generate an error (even if we fail to create json)
		String msgJson = ErrorType.ServerError.getEscapedString();
		try
		{
			msgJson = new ObjectMapper().writeValueAsString(msgJsonMap);
			Logger.getSingleton().error(TAG, msgJson);
		}
		catch (JsonProcessingException e)
		{
			Logger.getSingleton().error(TAG, "error creating error response");
		}
		this.message = msgJson;
	}
	
	
	public BreakwallAwsException(ErrorType errTypeIn, String messageIn)
	{
		this(errTypeIn, null, messageIn, null);
	}
	
	
	@Override
	public String getMessage()
	{
		return this.message;
	}
}
