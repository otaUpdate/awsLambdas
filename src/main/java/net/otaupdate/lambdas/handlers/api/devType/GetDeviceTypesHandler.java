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
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devicetypes;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.util.ObjectHelper;


public class GetDeviceTypesHandler extends AbstractAuthorizedRequestHandler
{
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
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{	
		List<ReturnValue> retVal = new ArrayList<ReturnValue>();
		
		// check user permissions
		if( !dbManIn.doesUserHavePermissionForOrganization(userIdIn, this.orgUuid) ) return retVal;

		Result<Record2<String, String>> result = 
				dslContextIn.select(Devicetypes.DEVICETYPES.UUID, Devicetypes.DEVICETYPES.NAME)
				.from(Devicetypes.DEVICETYPES)
				.join(Organizations.ORGANIZATIONS)
				.on(Devicetypes.DEVICETYPES.ORGANIZATIONUUID.eq(Organizations.ORGANIZATIONS.UUID))
				.and(Organizations.ORGANIZATIONS.UUID.eq(this.orgUuid))
				.fetch();

		for( Record2<String, String> currEntry : result )
		{
			retVal.add( new ReturnValue(currEntry.get(Devicetypes.DEVICETYPES.UUID), currEntry.get(Devicetypes.DEVICETYPES.NAME)) );
		}

		return retVal;
	}
}
