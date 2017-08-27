package net.otaupdate.lambdas.handlers;


import org.jooq.DSLContext;

import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.BreakwallAwsException;


public abstract class AbstractAuthorizedRequestHandler extends AbstractRequestHandler
{
	public abstract Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException;
}