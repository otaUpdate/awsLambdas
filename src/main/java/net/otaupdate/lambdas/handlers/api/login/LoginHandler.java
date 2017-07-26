package net.otaupdate.lambdas.handlers.api.login;

import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Users;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class LoginHandler extends AbstractUnauthorizedRequestHandler
{
	private static final String TAG = LoginHandler.class.getSimpleName();
	private static final String ERR_STRING_UNAUTH = "invalid username or password";

	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final String token;

		ReturnValue(String tokenIn)
		{
			this.token = tokenIn;
		}
	}


	private String emailAddress = null;
	private String password = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;

		this.emailAddress = ObjectHelper.parseObjectFromMap(jsonBodyMap, "emailAddress", String.class);
		if( this.emailAddress == null ) return false;

		this.password = ObjectHelper.parseObjectFromMap(jsonBodyMap, "password", String.class);
		if( this.password == null ) return false;

		return true;
	}


	@Override
	public Object processRequestWithDatabase(DatabaseManager dbManIn, DSLContext dslContextIn)
	{
		// retrieve the salt for the given user
		Result<Record1<String>> result_salt =
				dslContextIn.select(Users.USERS.SALT)
				.from(Users.USERS)
				.where(Users.USERS.EMAIL.eq(this.emailAddress))
				.limit(1)
				.fetch();
		if( result_salt.size() < 1 ) ErrorManager.throwError(ErrorType.Unauthorized, ERR_STRING_UNAUTH);
		String salt = result_salt.get(0).get(Users.USERS.SALT);

		// validate the hash
		Result<Record> result_hash = 
				dslContextIn.fetch("SELECT * FROM " + Users.USERS.getName() + " WHERE email=? AND passwordHash=ENCRYPT(?, ?) LIMIT 1",
						this.emailAddress, this.password, salt);
		if( result_hash.size() < 1 ) ErrorManager.throwError(ErrorType.Unauthorized, ERR_STRING_UNAUTH);

		// if we made it here, we have a valid username and password...update our token
		String loginToken = UUID.randomUUID().toString();
		dslContextIn.update(Users.USERS)
		.set(Users.USERS.LOGINTOKEN, loginToken)
		.set(Users.USERS.LOGINTOKENCREATION, DatabaseManager.getNow())
		.where(Users.USERS.EMAIL.eq(this.emailAddress))
		.execute();

		// do some logging
		if( loginToken != null ) Logger.getSingleton().debug(TAG, String.format("user authorized: '%s'", this.emailAddress));
		else Logger.getSingleton().debug(TAG, String.format("user authorization failed: '%s'", this.emailAddress));

		// return accordingly
		if( loginToken == null ) ErrorManager.throwError(ErrorType.Unauthorized, "invalid username or password");
		return new ReturnValue(loginToken);
	}
}
