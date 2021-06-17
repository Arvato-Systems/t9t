module com.arvatosystems.t9t.auth.sapi {
	exports com.arvatosystems.t9t.auth.services;
	exports com.arvatosystems.t9t.auth.hooks;

	requires com.arvatosystems.t9t.auth.apiext;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.server;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.auth;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires com.arvatosystems.t9t.auth.api;
}