package net.otaupdate.lambdas.handlers;


import org.jooq.DSLContext;

import net.otaupdate.lambdas.model.DatabaseManager;


public abstract class AbstractUnauthorizedRequestHandler extends AbstractRequestHandler
{
	@Deprecated
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn) { return null; }
	
	//TODO make abstract again
	public Object processRequestWithDatabase(DatabaseManager dbManIn, DSLContext dslContextIn) { return null; };
}