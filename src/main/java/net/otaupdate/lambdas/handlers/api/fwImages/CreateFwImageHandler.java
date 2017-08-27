package net.otaupdate.lambdas.handlers.api.fwImages;

import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareimages;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;

public class CreateFwImageHandler extends AbstractAuthorizedRequestHandler
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
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{
		String fwUuid = UUID.randomUUID().toString();
		
		// check user permissions
		if( !userIn.hasPermissionForProcessorType(this.orgUuid, this.devTypeUuid, this.procTypeUuid, dslContextIn) ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);
		
		// get the processor type id
		UInteger procTypeId = dbManIn.getProcTypeIdForUuid(this.procTypeUuid);
		if( procTypeId == null ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);
		
		// create the firmware image
		int numRecordsModified = dslContextIn.insertInto(Firmwareimages.FIRMWAREIMAGES, Firmwareimages.FIRMWAREIMAGES.UUID, Firmwareimages.FIRMWAREIMAGES.NAME, Firmwareimages.FIRMWAREIMAGES.PROCTYPEID)
				.values(fwUuid, this.fwImageName, procTypeId)
				.execute();
		if( numRecordsModified < 1 ) throw new BreakwallAwsException(ErrorType.ServerError, ERR_STR);
		
		return new ReturnValue(fwUuid);
	}

}
