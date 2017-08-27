package net.otaupdate.lambdas.handlers.api.procType;

import java.util.Map;

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

public class UpdateProcessorTypeHandler extends AbstractAuthorizedRequestHandler
{
	private static final String ERR_STR = "error updating processor type";
	
	
	private String orgUuid = null;
	private String devTypeUuid = null;
	private String procTypeUuid = null;
	
	private String name = null;
	private String latestFirmwareUuid = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> pathParameters = paramsIn.getPathParameters();
		if( pathParameters == null ) return false;
		
		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;

		this.orgUuid = ObjectHelper.parseObjectFromMap(pathParameters, "orgUuid", String.class);
		if( (this.orgUuid == null) || this.orgUuid.isEmpty() ) return false;
		
		this.devTypeUuid = ObjectHelper.parseObjectFromMap(pathParameters, "devTypeUuid", String.class);
		if( (this.devTypeUuid == null) || this.devTypeUuid.isEmpty() ) return false;
		
		this.procTypeUuid = ObjectHelper.parseObjectFromMap(pathParameters, "procTypeUuid", String.class);
		if( (this.procTypeUuid == null) || this.procTypeUuid.isEmpty() ) return false;
		
		this.name = ObjectHelper.parseObjectFromMap(jsonBodyMap, "name", String.class);
		if( (this.name == null) || this.name.isEmpty() ) return false;
		
		this.latestFirmwareUuid = ObjectHelper.parseObjectFromMap(jsonBodyMap, "latestFirmwareUuid", String.class);		
		if( (this.latestFirmwareUuid != null) && this.latestFirmwareUuid.isEmpty() ) this.latestFirmwareUuid = null;
		
		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{	
		// check user permissions
		if( !userIn.hasPermissionForProcessorType(this.orgUuid, this.devTypeUuid, this.procTypeUuid, dslContextIn) ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);
		
		UInteger latestFwId = dbManIn.getFirmwareImageIdForUuid(this.latestFirmwareUuid);
		
		int numRecordsModified = 
				dslContextIn.update(Processortypes.PROCESSORTYPES)
				.set(Processortypes.PROCESSORTYPES.NAME, this.name)
				.set(Processortypes.PROCESSORTYPES.LATESTFIRMWAREID, latestFwId)
				.where(Processortypes.PROCESSORTYPES.UUID.eq(this.procTypeUuid))
				.execute();
		if( numRecordsModified < 1 ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);
		
		return null;
	}
}