package net.otaupdate.lambdas.handlers.api.login;

import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Users;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class CreateUserHandler extends AbstractUnauthorizedRequestHandler
{	
	private static final String TAG = CreateUserHandler.class.getSimpleName();

	private String emailAddress = null;
	private String password = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;

		this.emailAddress = ObjectHelper.parseObjectFromMap(jsonBodyMap, "emailAddress", String.class);
		if( (this.emailAddress == null) || this.emailAddress.isEmpty() ) return false;

		this.password = ObjectHelper.parseObjectFromMap(jsonBodyMap, "password", String.class);
		if( (this.password == null) || this.password.isEmpty() ) return false;

		return true;
	}


	@Override
	public Object processRequestWithDatabase(DatabaseManager dbManIn, DSLContext dslContextIn)
	{
		String salt = UUID.randomUUID().toString();
		int numModifiedRecords = 
				dslContextIn.execute(String.format("INSERT IGNORE INTO %s (%s, %s, %s) VALUES(?, ENCRYPT(?, ?), ?)",
						Users.USERS.getName(), Users.USERS.EMAIL.getName(), Users.USERS.PASSWORDHASH.getName(), Users.USERS.SALT.getName()),
						this.emailAddress, this.password, salt, salt);
		if( numModifiedRecords < 1 ) ErrorManager.throwError(ErrorType.Conflict, "user already exists");

		// do some logging
		Logger.getSingleton().debug(TAG, String.format("user created: '%s'", this.emailAddress));

		return null;
	}
}