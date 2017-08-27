package net.otaupdate.lambdas.handlers.testing;

import java.util.Map;

import org.jooq.DSLContext;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;
import net.otaupdate.lambdas.util.CognitoHelper;
import net.otaupdate.lambdas.util.CognitoHelper.LoginResult;
import net.otaupdate.lambdas.util.ObjectHelper;


public class TestingLoginHandler extends AbstractUnauthorizedRequestHandler
{
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
		if( (this.emailAddress == null) || this.emailAddress.isEmpty() ) return false;

		this.password = ObjectHelper.parseObjectFromMap(jsonBodyMap, "password", String.class);
		if( (this.password == null) || this.password.isEmpty() ) return false;

		return true;
	}


	@Override
	public Object processRequestWithDatabase(DatabaseManager dbManIn, DSLContext dslContextIn) throws BreakwallAwsException
	{
		LoginResult res = CognitoHelper.login(this.emailAddress, this.password);
		if( res == null ) throw new BreakwallAwsException(ErrorType.Unauthorized, ERR_STRING_UNAUTH);
		
		// if we made it here we have a couple different options to handle...
		// check to see if we need a password reset
		if( res.getNeedsPasswordReset() ) throw new BreakwallAwsException(ErrorType.Unauthorized, "NEEDS_PASSWORD_RESET", "You must change your password", res.getPasswordChangeToken());
		
		// if we made it here, login was successful
		return new ReturnValue(res.getToken());
	}
}
