package net.otaupdate.lambdas.handlers.api.devType;

import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devicetypes;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;

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
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{
		String devTypeUuid = UUID.randomUUID().toString();

		// check user permissions
		if( !userIn.hasPermissionForOrganization(this.orgUuid, dslContextIn) ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STRING);

		// get the organization id
		UInteger orgId = dbManIn.getOrganizationIdForUuid(this.orgUuid);
		if( orgId == null ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STRING);

		// create the device type
		int numRecordsModified = 
				dslContextIn.insertInto(Devicetypes.DEVICETYPES, Devicetypes.DEVICETYPES.UUID, Devicetypes.DEVICETYPES.NAME, Devicetypes.DEVICETYPES.ORGID)
				.values(devTypeUuid, this.deviceTypeName, orgId)
				.execute();
		if( numRecordsModified < 1 ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STRING);

		return new ReturnValue(devTypeUuid);
	}

}
