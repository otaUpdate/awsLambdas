package net.otaupdate.lambdas.handlers;


import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.model.DatabaseManager;


public abstract class AbstractAuthorizedRequestHandler extends AbstractRequestHandler
{
	@Deprecated
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, int userIdIn) { return null; }
	
	
	//TODO make abstract again
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn) { return null; }
}