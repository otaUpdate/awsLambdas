package net.otaupdate.lambdas.handlers.api.fwImages;

import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Firmwareimages;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.S3Helper;

public class DeleteFwImageHandler extends AbstractAuthorizedRequestHandler
{
	private static final String TAG = DeleteFwImageHandler.class.getSimpleName();
	private static final String ERR_STRING = "error deleting firmware image";


	private String orgUuid = null;
	private String devTypeUuid = null;
	private String procTypeUuid = null;
	private String fwUuid = null;


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

		this.fwUuid = ObjectHelper.parseObjectFromMap(pathParameters, "fwUuid", String.class);
		if( (this.fwUuid == null) || this.fwUuid.isEmpty() ) return false;

		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{	
		// check user permissions
		if( !dbManIn.doesUserHavePermissionForFirmware(userIdIn, this.orgUuid, this.devTypeUuid, this.procTypeUuid, this.fwUuid) ) ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING);

		// always delete the s3 key first so we don't leave any dangling files
		// billing-wise we'd prefer a dangling database entry than an S3 entry
		Logger.getSingleton().debug(TAG, String.format("deleting firmware image file '%s'", this.fwUuid));
		S3Helper.deleteImageForFirmwareWithUuid(this.fwUuid);

		// delete the device type (will cascade to all other tables appropriately)
		int numRecordsModified = 
				dslContextIn.delete(Firmwareimages.FIRMWAREIMAGES)
				.where(Firmwareimages.FIRMWAREIMAGES.UUID.eq(this.fwUuid))
				.execute();
		if( numRecordsModified < 1 ) ErrorManager.throwError(ErrorType.ServerError, ERR_STRING);

		return null;
	}

}