package net.otaupdate.lambdas.handlers;


import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.model.DatabaseManager;


public abstract class AbstractAuthorizedRequestHandler extends AbstractRequestHandler
{
	public abstract Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn);
}