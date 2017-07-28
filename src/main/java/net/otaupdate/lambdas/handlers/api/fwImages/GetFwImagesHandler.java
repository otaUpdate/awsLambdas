package net.otaupdate.lambdas.handlers.api.fwImages;

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
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareimages;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.util.ObjectHelper;


public class GetFwImagesHandler extends AbstractAuthorizedRequestHandler
{
	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final String uuid;
		public final String name;
		public final String toVersionUuid;

		public ReturnValue(String uuidIn, String nameIn, String toVersionUuidIn)
		{
			this.uuid = uuidIn;
			this.name = nameIn;
			this.toVersionUuid = toVersionUuidIn;
		}
	}


	private String orgUuid = null;
	private String devTypeUuid = null;
	private String procTypeUuid = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{	
		Map<String, Object> pathParameters = paramsIn.getPathParameters();
		if( pathParameters == null ) return false;

		this.orgUuid = ObjectHelper.parseObjectFromMap(pathParameters, "orgUuid", String.class);
		if( (orgUuid == null) || orgUuid.isEmpty() ) return false;

		this.devTypeUuid = ObjectHelper.parseObjectFromMap(pathParameters, "devTypeUuid", String.class);
		if( (this.devTypeUuid == null) || this.devTypeUuid.isEmpty() ) return false;
		
		this.procTypeUuid = ObjectHelper.parseObjectFromMap(pathParameters, "procTypeUuid", String.class);
		if( (this.procTypeUuid == null) || this.procTypeUuid.isEmpty() ) return false;

		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{	
		List<ReturnValue> retVal = new ArrayList<ReturnValue>();

		// check user permissions
		if( !dbManIn.doesUserHavePermissionForProcessorType(userIdIn, this.orgUuid, this.devTypeUuid, this.procTypeUuid) ) return retVal;
		
		Result<Record3<String, String, UInteger>> result = 
				dslContextIn.select(Firmwareimages.FIRMWAREIMAGES.UUID, Firmwareimages.FIRMWAREIMAGES.NAME, Firmwareimages.FIRMWAREIMAGES.TOVERSIONID)
				.from(Firmwareimages.FIRMWAREIMAGES)
				.join(Processortypes.PROCESSORTYPES)
				.on(Firmwareimages.FIRMWAREIMAGES.PROCTYPEID.eq(Processortypes.PROCESSORTYPES.ID))
				.join(Devicetypes.DEVICETYPES)
				.on(Processortypes.PROCESSORTYPES.DEVTYPEID.eq(Devicetypes.DEVICETYPES.ID))
				.join(Organizations.ORGANIZATIONS)
				.on(Devicetypes.DEVICETYPES.ORGID.eq(Organizations.ORGANIZATIONS.ID))
				.and(Processortypes.PROCESSORTYPES.UUID.eq(this.procTypeUuid))
				.and(Devicetypes.DEVICETYPES.UUID.eq(this.devTypeUuid))
				.and(Organizations.ORGANIZATIONS.UUID.eq(this.orgUuid))
				.fetch();

		for( Record3<String, String, UInteger> currEntry : result )
		{
			String toVersionUuid = dbManIn.getFirmwareImageUuidForId(currEntry.getValue(Firmwareimages.FIRMWAREIMAGES.TOVERSIONID));
			
			retVal.add( new ReturnValue(currEntry.get(Firmwareimages.FIRMWAREIMAGES.UUID), 
					currEntry.get(Firmwareimages.FIRMWAREIMAGES.NAME), 
					toVersionUuid) );
		}

		return retVal;
	}
}
