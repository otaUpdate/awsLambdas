package net.otaupdate.lambdas.util;


public class ErrorManager
{
	public enum ErrorType
	{
		BadRequest("BadRequest"),
		Unauthorized("Unauthorized"),
		Conflict("Conflict"),
		ServerError("ServerError");
		
		private final String stringRep;
		private ErrorType(String stringIn) { this.stringRep = stringIn; }
		public String toString() { return this.stringRep; }
	}
	
	
	public static class ErrorManagerException extends RuntimeException
	{
		private static final long serialVersionUID = 8192868254466705600L;

		public ErrorManagerException(String messageIn)
		{
			super(messageIn);
		}
	}
	
	
    public static void throwError(ErrorType errTypeIn, String messageIn) throws ErrorManagerException
    {
    	String errorMessage = String.format("[%s] %s", errTypeIn.toString(), messageIn);
    	Logger.getSingleton().error(errorMessage);
    	throw new ErrorManagerException(errorMessage);
    }
}
