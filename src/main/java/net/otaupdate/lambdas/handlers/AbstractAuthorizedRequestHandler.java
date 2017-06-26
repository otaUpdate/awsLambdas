package net.otaupdate.lambdas.handlers;


import net.otaupdate.lambdas.model.DatabaseManager;


public abstract class AbstractAuthorizedRequestHandler extends AbstractRequestHandler
{
	public abstract Object processRequestWithDatabaseManager(DatabaseManager dbManIn, int userIdIn);
}