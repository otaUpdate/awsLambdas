package net.otaupdate.lambdas.handlers.api.procType;

import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Processortypes;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;

public class DeleteProcessorTypeHandler extends AbstractAuthorizedRequestHandler
{
	private static final String ERR_STRING = "error deleting processor type";
	
	
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
		// check user permissions
		if( !dbManIn.doesUserHavePermissionForProcessorType(userIdIn, this.orgUuid, this.devTypeUuid, this.procTypeUuid) ) ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING);
		
		// delete the processor type (will cascade to all other tables appropriately)
		int numRecordsModified = 
				dslContextIn.delete(Processortypes.PROCESSORTYPES)
				.where(Processortypes.PROCESSORTYPES.UUID.eq(this.procTypeUuid))
				.execute();
		if( numRecordsModified < 1 ) ErrorManager.throwError(ErrorType.ServerError, ERR_STRING);
		
		return null;
	}

}