package net.otaupdate.lambdas.handlers.api.devType;

import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devicetypes;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;
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
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{	
		// check user permissions
		if( !dbManIn.doesUserHavePermissionForDeviceType(userIdIn, this.orgUuid, this.devTypeUuid) ) ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING);
		
		// delete the device type (will cascade to all other tables appropriately)
		int numRecordsModified = 
				dslContextIn.delete(Devicetypes.DEVICETYPES)
				.where(Devicetypes.DEVICETYPES.UUID.eq(this.devTypeUuid))
				.execute();
		if( numRecordsModified < 1 ) ErrorManager.throwError(ErrorType.ServerError, ERR_STRING);
		
		return null;
	}

}