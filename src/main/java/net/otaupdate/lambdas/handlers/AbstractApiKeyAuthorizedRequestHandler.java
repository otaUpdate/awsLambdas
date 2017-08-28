package net.otaupdate.lambdas.handlers;


import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.BreakwallAwsException;


public abstract class AbstractApiKeyAuthorizedRequestHandler extends AbstractRequestHandler
{
	public abstract Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger orgIdIn) throws BreakwallAwsException;
}