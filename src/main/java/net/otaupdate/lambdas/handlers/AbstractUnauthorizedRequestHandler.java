package net.otaupdate.lambdas.handlers;


import org.jooq.DSLContext;

import net.otaupdate.lambdas.model.DatabaseManager;


public abstract class AbstractUnauthorizedRequestHandler extends AbstractRequestHandler
{
	public abstract Object processRequestWithDatabase(DatabaseManager dbManIn, DSLContext dslContextIn);
}