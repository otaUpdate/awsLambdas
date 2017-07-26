package net.otaupdate.lambdas.handlers.api.devType;

import java.util.Map;
import java.util.UUID;

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

public class CreateDeviceTypeHandler extends AbstractAuthorizedRequestHandler
{
	private static final String ERR_STRING = "error creating device type";


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
	private String deviceTypeName = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> pathParameters = paramsIn.getPathParameters();
		if( pathParameters == null ) return false;

		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;

		this.orgUuid = ObjectHelper.parseObjectFromMap(pathParameters, "orgUuid", String.class);
		if( (this.orgUuid == null) || this.orgUuid.isEmpty() ) return false;

		this.deviceTypeName = ObjectHelper.parseObjectFromMap(jsonBodyMap, "name", String.class);
		if( (this.deviceTypeName == null) || this.deviceTypeName.isEmpty() ) return false;

		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{
		String devTypeUuid = UUID.randomUUID().toString();

		// check user permissions
		if( !dbManIn.doesUserHavePermissionForOrganization(userIdIn, this.orgUuid) ) ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING);

		// create the device type
		int numRecordsModified = dslContextIn.insertInto(Devicetypes.DEVICETYPES, Devicetypes.DEVICETYPES.UUID, Devicetypes.DEVICETYPES.ORGANIZATIONUUID, Devicetypes.DEVICETYPES.NAME)
				.values(devTypeUuid, this.orgUuid, this.deviceTypeName)
				.execute();
		if( numRecordsModified < 1 ) ErrorManager.throwError(ErrorType.ServerError, ERR_STRING);

		return new ReturnValue(devTypeUuid);
	}

}
