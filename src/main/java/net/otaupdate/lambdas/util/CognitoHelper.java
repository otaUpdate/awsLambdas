package net.otaupdate.lambdas.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.ChallengeNameType;
import com.amazonaws.services.cognitoidp.model.DeliveryMediumType;
import com.amazonaws.services.cognitoidp.model.InternalErrorException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;


public class CognitoHelper
{
	private static final String ATTR_NAME_EMAIL = "email";

	private static final String TAG = CognitoHelper.class.getSimpleName();
	private static final String CLIENT_ID = "658be8cnj6v6kupqid3mnqmsr8";
	private static final String POOL_ID = "us-east-2_9lt2r4XGQ";
	private static final AWSCognitoIdentityProvider COGNITO_CLIENT = AWSCognitoIdentityProviderClientBuilder.defaultClient();


	public static class LoginResult
	{
		private final String token;

		private final boolean needsPasswordChange;
		private final String passwordChangeToken;

		private LoginResult(String tokenIn, boolean needsPwdChangeIn, String pwdChangeTokenIn)
		{
			this.token = tokenIn;
			this.needsPasswordChange = needsPwdChangeIn;
			this.passwordChangeToken = pwdChangeTokenIn;
		}


		public String getToken()
		{
			return this.token;
		}


		public boolean getNeedsPasswordReset()
		{
			return this.needsPasswordChange;
		}


		public String getPasswordChangeToken()
		{
			return this.passwordChangeToken;
		}


		public static LoginResult getResultWithAuthToken(String authTokenIn)
		{
			return new LoginResult(authTokenIn, false, null);
		}


		public static LoginResult getResultWithPasswordChangeToken(String pwdChangeTokenIn)
		{
			return new LoginResult(null, true, pwdChangeTokenIn);
		}
	}


	/**
	 * @param emailIn
	 * @param passwordIn
	 * @return login result if the login should proceed (eg: successful, needs password reset, etc),
	 * 		null if login outright failed
	 */
	public static LoginResult login(String emailIn, String passwordIn)
	{
		return loginWithUsernameOrEmailGetToken(emailIn, null, passwordIn);
	}


	/**
	 * If user already exists, will simply return the username
	 * @param emailAddressIn
	 * @return
	 */
	public static String createUserForEmailGetUserName(String emailAddressIn)
	{
		String retVal = getUsernameFromEmail(emailAddressIn);
		if( retVal == null )
		{
			// need to create a new user first
			retVal = createUserGetUsername(emailAddressIn);
		}

		return retVal;
	}


	public static String getEmailFromAwsSub(String awsSubIn)
	{
		String retVal = null;

		try
		{
			AdminGetUserResult result = COGNITO_CLIENT.adminGetUser(new AdminGetUserRequest()
					.withUserPoolId(POOL_ID)
					.withUsername(awsSubIn)

					);
			
			retVal = getEmailFromUserAttributes(result.getUserAttributes());
		}
		catch( UserNotFoundException e ) { }
		catch( Exception e )
		{
			retVal = null;
			Logger.getSingleton().warn(TAG, String.format("getEmailFromAwsSub error: '%s'", e.toString()));
		}

		return retVal;
	}
	
	
	public static String getUsernameFromEmail(String emailIn)
	{
		AdminGetUserRequest agur = new AdminGetUserRequest()
				.withUserPoolId(POOL_ID)
				.withUsername(emailIn);

		String retVal = null;
		try
		{
			retVal = COGNITO_CLIENT.adminGetUser(agur).getUsername();
		}
		catch( UserNotFoundException e ) { }
		catch( Exception e )
		{
			Logger.getSingleton().warn(TAG, String.format("username lookup error: '%s'", e.toString()));
		}
		return retVal;
	}


	private static String createUserGetUsername(String emailAddressIn)
	{
		List<AttributeType> attributes = new ArrayList<AttributeType>();
		attributes.add(new AttributeType()
				.withName(ATTR_NAME_EMAIL)
				.withValue(emailAddressIn));

		String retVal = null;
		try
		{
			AdminCreateUserResult result = COGNITO_CLIENT.adminCreateUser(new AdminCreateUserRequest()
					.withUserPoolId(POOL_ID)
					.withUsername(emailAddressIn)
					.withDesiredDeliveryMediums(DeliveryMediumType.EMAIL)
					.withUserAttributes(attributes)
					);

			retVal = result.getUser().getUsername();
		}
		catch( Exception e )
		{
			Logger.getSingleton().warn(TAG, String.format("createUser error: '%s'", e.toString()));
		}

		return retVal;
	}


	private static LoginResult loginWithUsernameOrEmailGetToken(String emailIn, String usernameIn, String passwordIn)
	{
		// give preference to the username
		if( usernameIn != null) Logger.getSingleton().debug(TAG, String.format("trying login with username: '%s'\n", usernameIn));
		else if( emailIn != null ) Logger.getSingleton().debug(TAG, String.format("trying login with email: '%s'\n", emailIn));
		else return null;

		Map<String, String> authParams = new HashMap<String, String>();
		authParams.put("USERNAME",  (usernameIn != null) ? usernameIn : emailIn);
		authParams.put("PASSWORD", passwordIn);

		AdminInitiateAuthRequest authReq = new AdminInitiateAuthRequest()
				.withClientId(CLIENT_ID)
				.withUserPoolId(POOL_ID)
				.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
				.withAuthParameters(authParams);

		LoginResult retVal = null;
		try
		{
			AdminInitiateAuthResult result = COGNITO_CLIENT.adminInitiateAuth(authReq);

			AuthenticationResultType authResult = result.getAuthenticationResult();
			if( authResult != null )
			{
				retVal = LoginResult.getResultWithAuthToken(authResult.getIdToken());
			}
			else
			{
				if( (result.getChallengeName() != null) && result.getChallengeName().equals(ChallengeNameType.NEW_PASSWORD_REQUIRED.toString()) )
				{
					retVal = LoginResult.getResultWithPasswordChangeToken(result.getSession());
				}
			}
		}
		catch( InternalErrorException e )
		{
			// this occurs for first-time logins...try using the username.sub instead of the email...
			if( emailIn != null )
			{
				String username = getUsernameFromEmail(emailIn);
				if( username != null ) retVal = loginWithUsernameOrEmailGetToken(emailIn, username, passwordIn);
			}
		}
		catch( Exception e )
		{
			Logger.getSingleton().warn(TAG, String.format("login error: '%s'", e.toString()));
		}
		return retVal;
	}
	
	
	private static String getEmailFromUserAttributes(List<AttributeType> attrsIn)
	{
		for( AttributeType currAttr : attrsIn )
		{
			if( currAttr.getName().equals(ATTR_NAME_EMAIL) )
			{
				return currAttr.getValue();
			}
		}
		
		// if we made it here we've failed
		return null;
	}
}
