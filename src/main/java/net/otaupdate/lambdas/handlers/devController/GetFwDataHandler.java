package net.otaupdate.lambdas.handlers.devController;

import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.S3Helper;


public class GetFwDataHandler extends AbstractUnauthorizedRequestHandler
{
	private String currentFwUuid = null;
	private Integer offset = null;
	private Integer maxNumBytes = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		this.currentFwUuid = ObjectHelper.parseObjectFromMap(paramsIn, "targetFwUuid", String.class);
		if( this.currentFwUuid == null ) return false;
		
		this.offset = ObjectHelper.parseObjectFromMap(paramsIn, "offset", Integer.class);
		if( this.offset == null ) return false;
		
		this.maxNumBytes = ObjectHelper.parseObjectFromMap(paramsIn, "maxNumBytes", Integer.class);
		if( this.maxNumBytes == null ) return false;
		
		return true;
	}
	

	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn)
	{	
		byte[] retVal = null;
		
		Logger.getSingleton().debug("1");
		try
		{
			Logger.getSingleton().debug("2");
			retVal = S3Helper.getBytesForFirmwareUuid(this.currentFwUuid, this.offset, this.maxNumBytes);
			Logger.getSingleton().debug("3");
		}
		catch( Exception e )
		{
			Logger.getSingleton().debug("4");
			ErrorManager.throwError(ErrorType.BadRequest, "error fetching requested bytes");
			Logger.getSingleton().debug("5");
		}
		
		Logger.getSingleton().debug("6");
		
		Logger.getSingleton().debug(String.format("retBytes: '%s'", bytesToHex(retVal)));
		
		return retVal;
	}
	
	
	private static String bytesToHex(byte[] in)
	{
	    final StringBuilder builder = new StringBuilder();
	    for(byte b : in)
	    {
	        builder.append(String.format("%02x", b));
	    }
	    return builder.toString();
	}
}
