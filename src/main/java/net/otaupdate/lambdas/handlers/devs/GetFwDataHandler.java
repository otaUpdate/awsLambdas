package net.otaupdate.lambdas.handlers.devs;

import java.util.Map;

import org.jooq.DSLContext;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.S3Helper;


public class GetFwDataHandler extends AbstractUnauthorizedRequestHandler
{
	private String currentFwUuid = null;
	private Integer offset = null;
	private Integer maxNumBytes = null;
	
	
	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;
		
		this.currentFwUuid = ObjectHelper.parseObjectFromMap(jsonBodyMap, "targetFwUuid", String.class);
		if( this.currentFwUuid == null ) return false;
		
		this.offset = ObjectHelper.parseObjectFromMap(jsonBodyMap, "offset", Integer.class);
		if( this.offset == null ) return false;
		
		this.maxNumBytes = ObjectHelper.parseObjectFromMap(jsonBodyMap, "maxNumBytes", Integer.class);
		if( this.maxNumBytes == null ) return false;
		
		return true;
	}
	

	@Override
	public Object processRequestWithDatabase(DatabaseManager dbManIn, DSLContext dslContextIn)
	{	
		byte[] retVal = null;
		
		try
		{
			retVal = S3Helper.getBytesForFirmwareUuid(this.currentFwUuid, this.offset, this.maxNumBytes);
		}
		catch( Exception e ) { }
		if( retVal == null ) throw new BreakwallAwsException(ErrorType.BadRequest, "error fetching requested bytes");
		
		return retVal;
	}
}
