package net.otaupdate.lambdas.handlers.api.devType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.jooq.Record2;
import org.jooq.Result;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devicetypes;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;


public class GetDeviceTypesHandler extends AbstractAuthorizedRequestHandler
{
	private final String ERR_STR = "error listing device types";
	
	
	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final String uuid;
		public final String name;

		public ReturnValue(String uuidIn, String nameIn)
		{
			this.uuid = uuidIn;
			this.name = nameIn;
		}
	}
	
	
	private String orgUuid = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{	
		Map<String, Object> pathParameters = paramsIn.getPathParameters();
		if( pathParameters == null ) return false;

		this.orgUuid = ObjectHelper.parseObjectFromMap(pathParameters, "orgUuid", String.class);
		if( (orgUuid == null) || orgUuid.isEmpty() ) return false;
		
		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{	
		List<ReturnValue> retVal = new ArrayList<ReturnValue>();
		
		// check user permissions
		if( !userIn.hasPermissionForOrganization(this.orgUuid, dslContextIn) ) return retVal;
		
		// get the organization id
		UInteger orgId = dbManIn.getOrganizationIdForUuid(this.orgUuid);
		if( orgId == null ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);

		Result<Record2<String, String>> result = 
				dslContextIn.select(Devicetypes.DEVICETYPES.UUID, Devicetypes.DEVICETYPES.NAME)
				.from(Devicetypes.DEVICETYPES)
				.join(Organizations.ORGANIZATIONS)
				.on(Devicetypes.DEVICETYPES.ORGID.eq(orgId))
				.and(Organizations.ORGANIZATIONS.UUID.eq(this.orgUuid))
				.fetch();

		for( Record2<String, String> currEntry : result )
		{
			retVal.add( new ReturnValue(currEntry.get(Devicetypes.DEVICETYPES.UUID), currEntry.get(Devicetypes.DEVICETYPES.NAME)) );
		}

		return retVal;
	}
}
