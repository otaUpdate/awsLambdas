package net.otaupdate.lambdas.handlers.api.procType;

import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;

public class CreateProcessorTypeHandler extends AbstractAuthorizedRequestHandler
{
	private static final String ERR_STRING = "error creating processor type";
	
	
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
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{
		String procTypeUuid = UUID.randomUUID().toString();
		
		// check user permissions
		if( !dbManIn.doesUserHavePermissionForDeviceType(userIdIn, this.orgUuid, this.devTypeUuid) ) ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING);
		
		// create the device type
		int numRecordsModified = dslContextIn.insertInto(Processortypes.PROCESSORTYPES, Processortypes.PROCESSORTYPES.UUID, Processortypes.PROCESSORTYPES.DEVICETYPEUUID, Processortypes.PROCESSORTYPES.NAME)
				.values(procTypeUuid, this.devTypeUuid, this.processorTypeName)
				.execute();
		if( numRecordsModified < 1 ) ErrorManager.throwError(ErrorType.ServerError, ERR_STRING);
		
		return new ReturnValue(procTypeUuid);
	}

}
