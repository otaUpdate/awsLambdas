package net.otaupdate.lambdas.handlers.api.fwImages;

import java.util.Map;

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

public class UpdateFwImageHandler extends AbstractAuthorizedRequestHandler
{
	private static final String ERR_STR = "error updating processor type";
	
	
	private String orgUuid = null;
	private String devTypeUuid = null;
	private String procTypeUuid = null;
	private String fwUuid = null;
	
	private String name = null;
	private String toVersionUuid = null;


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
		
		this.fwUuid = ObjectHelper.parseObjectFromMap(pathParameters, "fwUuid", String.class);
		if( (this.fwUuid == null) || this.fwUuid.isEmpty() ) return false;
		
		this.name = ObjectHelper.parseObjectFromMap(jsonBodyMap, "name", String.class);
		if( (this.name == null) || this.name.isEmpty() ) return false;
		
		this.toVersionUuid = ObjectHelper.parseObjectFromMap(jsonBodyMap, "toVersionUuid", String.class);
		if( (this.toVersionUuid != null) && this.toVersionUuid.isEmpty() ) this.toVersionUuid = null;

		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{	
		// check user permissions
		if( !userIn.hasPermissionForFirmware(this.orgUuid, this.devTypeUuid, this.procTypeUuid, this.fwUuid, dslContextIn) ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);
		
		UInteger toVersionId = dbManIn.getFirmwareImageIdForUuid(this.toVersionUuid);
		
		int numRecordsModified = 
				dslContextIn.update(Firmwareimages.FIRMWAREIMAGES)
				.set(Firmwareimages.FIRMWAREIMAGES.NAME, this.name)
				.set(Firmwareimages.FIRMWAREIMAGES.TOVERSIONID, toVersionId)
				.where(Firmwareimages.FIRMWAREIMAGES.UUID.eq(this.fwUuid))
				.execute();
		if( numRecordsModified < 1 ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);
		
		return null;
	}
}