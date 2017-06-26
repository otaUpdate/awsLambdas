package net.otaupdate.lambdas.handlers;


import net.otaupdate.lambdas.model.DatabaseManager;


public abstract class AbstractUnauthorizedRequestHandler extends AbstractRequestHandler
{
	public abstract Object processRequestWithDatabaseManager(DatabaseManager dbManIn);
}