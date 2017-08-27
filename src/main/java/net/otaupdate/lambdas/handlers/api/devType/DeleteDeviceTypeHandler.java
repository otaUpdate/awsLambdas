package net.otaupdate.lambdas.handlers.api.devType;

import java.util.Map;

import org.jooq.DSLContext;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devicetypes;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;

public class DeleteDeviceTypeHandler extends AbstractAuthorizedRequestHandler
{
	private static final String ERR_STRING = "error deleting device type";
	
	
	private String orgUuid = null;
	private String devTypeUuid = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> pathParameters = paramsIn.getPathParameters();
		if( pathParameters == null ) return false;

		this.orgUuid = ObjectHelper.parseObjectFromMap(pathParameters, "orgUuid", String.class);
		if( (orgUuid == null) || orgUuid.isEmpty() ) return false;
		
		this.devTypeUuid = ObjectHelper.parseObjectFromMap(pathParameters, "devTypeUuid", String.class);
		if( (this.devTypeUuid == null) || this.devTypeUuid.isEmpty() ) return false;

		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{	
		// check user permissions
		if( !userIn.hasPermissionForDeviceType(this.orgUuid, this.devTypeUuid, dslContextIn) ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STRING);
		
		// delete the device type (will cascade to all other tables appropriately)
		int numRecordsModified = 
				dslContextIn.delete(Devicetypes.DEVICETYPES)
				.where(Devicetypes.DEVICETYPES.UUID.eq(this.devTypeUuid))
				.execute();
		if( numRecordsModified < 1 ) throw new BreakwallAwsException(ErrorType.ServerError, ERR_STRING);
		
		return null;
	}

}