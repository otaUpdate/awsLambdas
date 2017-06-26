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
	
	
    public static void throwError(ErrorType errTypeIn, String messageIn) throws RuntimeException
    {
    	String errorMessage = String.format("[%s] %s", errTypeIn.toString(), messageIn);
    	Logger.getSingleton().error(errorMessage);
    	throw new RuntimeException(errorMessage);
    }
}
