package net.otaupdate.lambdas.handlers.api.procType;

import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;

public class CreateProcessorTypeHandler extends AbstractAuthorizedRequestHandler
{
	private static final String ERR_STR = "error creating processor type";
	
	
	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final String uuid;

		public ReturnValue(String uuidIn)
		{
			this.uuid = uuidIn;
		}
	}
	
	
	private String orgUuid = null;
	private String devTypeUuid = null;
	private String processorTypeName = null;
	
	
	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> pathParameters = paramsIn.getPathParameters();
		if( pathParameters == null ) return false;

		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;
		
		this.orgUuid = ObjectHelper.parseObjectFromMap(pathParameters, "orgUuid", String.class);
		if( (orgUuid == null) || orgUuid.isEmpty() ) return false;
		
		this.devTypeUuid = ObjectHelper.parseObjectFromMap(pathParameters, "devTypeUuid", String.class);
		if( (this.devTypeUuid == null) || this.devTypeUuid.isEmpty() ) return false;
		
		this.processorTypeName = ObjectHelper.parseObjectFromMap(jsonBodyMap, "name", String.class);
		if( (this.processorTypeName == null) || this.processorTypeName.isEmpty() ) return false;
		
		return true;
	}
	
	
	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{
		String procTypeUuid = UUID.randomUUID().toString();
		
		// check user permissions
		if( !userIn.hasPermissionForDeviceType(this.orgUuid, this.devTypeUuid, dslContextIn) ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);
		
		// get the device type id
		UInteger devTypeId = dbManIn.getDevTypeIdForUuid(this.devTypeUuid);
		if( devTypeId == null ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);
		
		// create the device type
		int numRecordsModified = dslContextIn.insertInto(Processortypes.PROCESSORTYPES, Processortypes.PROCESSORTYPES.UUID, Processortypes.PROCESSORTYPES.NAME, Processortypes.PROCESSORTYPES.DEVTYPEID)
				.values(procTypeUuid, this.processorTypeName, devTypeId)
				.execute();
		if( numRecordsModified < 1 ) throw new BreakwallAwsException(ErrorType.ServerError, ERR_STR);
		
		return new ReturnValue(procTypeUuid);
	}

}
