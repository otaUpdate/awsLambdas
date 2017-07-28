package net.otaupdate.lambdas.handlers.api.fwImages;

import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareimages;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;

public class CreateFwImageHandler extends AbstractAuthorizedRequestHandler
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
	private String procTypeUuid = null;
	private String fwImageName = null;
	
	
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
		
		this.procTypeUuid = ObjectHelper.parseObjectFromMap(pathParameters, "procTypeUuid", String.class);
		if( (this.procTypeUuid == null) || this.procTypeUuid.isEmpty() ) return false;
		
		this.fwImageName = ObjectHelper.parseObjectFromMap(jsonBodyMap, "name", String.class);
		if( (this.fwImageName == null) || this.fwImageName.isEmpty() ) return false;
		
		return true;
	}
	
	
	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{
		String fwUuid = UUID.randomUUID().toString();
		
		// check user permissions
		if( !dbManIn.doesUserHavePermissionForProcessorType(userIdIn, this.orgUuid, this.devTypeUuid, this.procTypeUuid) ) ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING);
		
		// get the processor type id
		UInteger procTypeId = dbManIn.getProcTypeIdForUuid(this.procTypeUuid);
		if( procTypeId == null ) ErrorManager.throwError(ErrorType.ServerError, ERR_STRING);
		
		// create the firmware image
		int numRecordsModified = dslContextIn.insertInto(Firmwareimages.FIRMWAREIMAGES, Firmwareimages.FIRMWAREIMAGES.UUID, Firmwareimages.FIRMWAREIMAGES.NAME, Firmwareimages.FIRMWAREIMAGES.PROCTYPEID)
				.values(fwUuid, this.fwImageName, procTypeId)
				.execute();
		if( numRecordsModified < 1 ) ErrorManager.throwError(ErrorType.ServerError, ERR_STRING);
		
		return new ReturnValue(fwUuid);
	}

}
