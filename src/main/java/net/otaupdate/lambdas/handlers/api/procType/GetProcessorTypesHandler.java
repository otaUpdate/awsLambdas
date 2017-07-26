package net.otaupdate.lambdas.handlers.api.procType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.jooq.Record3;
import org.jooq.Result;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Devicetypes;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.util.ObjectHelper;


public class GetProcessorTypesHandler extends AbstractAuthorizedRequestHandler
{
	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final String uuid;
		public final String name;
		public final String latestFirmwareUuid;

		public ReturnValue(String uuidIn, String nameIn, String latestFirmwareUuidIn)
		{
			this.uuid = uuidIn;
			this.name = nameIn;
			this.latestFirmwareUuid = latestFirmwareUuidIn;
		}
	}


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
		List<ReturnValue> retVal = new ArrayList<ReturnValue>();

		// check user permissions
		if( !dbManIn.doesUserHavePermissionForDeviceType(userIdIn, this.orgUuid, this.devTypeUuid) ) return retVal;

		Result<Record3<String, String, String>> result = 
				dslContextIn.select(Processortypes.PROCESSORTYPES.UUID, Processortypes.PROCESSORTYPES.NAME, Processortypes.PROCESSORTYPES.LATESTFIRMWAREUUID)
				.from(Processortypes.PROCESSORTYPES)
				.join(Devicetypes.DEVICETYPES)
				.on(Processortypes.PROCESSORTYPES.DEVICETYPEUUID.eq(Devicetypes.DEVICETYPES.UUID))
				.join(Organizations.ORGANIZATIONS)
				.on(Devicetypes.DEVICETYPES.ORGANIZATIONUUID.eq(Organizations.ORGANIZATIONS.UUID))
				.and(Devicetypes.DEVICETYPES.UUID.eq(this.devTypeUuid))
				.and(Organizations.ORGANIZATIONS.UUID.eq(this.orgUuid))
				.fetch();

		for( Record3<String, String, String> currEntry : result )
		{
			retVal.add( new ReturnValue(currEntry.get(Processortypes.PROCESSORTYPES.UUID), 
					currEntry.get(Processortypes.PROCESSORTYPES.NAME), 
					currEntry.get(Processortypes.PROCESSORTYPES.LATESTFIRMWAREUUID)) );
		}

		return retVal;
	}
}
