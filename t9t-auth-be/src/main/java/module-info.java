module com.arvatosystems.t9t.auth.be {
	exports com.arvatosystems.t9t.base.be.auth;
	exports com.arvatosystems.t9t.authz.be.api;
	exports com.arvatosystems.t9t.authc.be.stubs;
	exports com.arvatosystems.t9t.auth.be.jwt;
	exports com.arvatosystems.t9t.authc.be.api;
	exports com.arvatosystems.t9t.auth.be.impl;
	exports com.arvatosystems.t9t.auth.be.request;

	requires com.arvatosystems.t9t.auth.api;
	requires com.arvatosystems.t9t.auth.apiext;
	requires com.arvatosystems.t9t.auth.jwt;
	requires com.arvatosystems.t9t.auth.sapi;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.be;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.cfg;
	requires com.arvatosystems.t9t.doc.api;
	requires com.arvatosystems.t9t.email.api;
	requires com.arvatosystems.t9t.server;
	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.auth;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires java.naming;
	requires org.eclipse.xtend.lib;
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires org.slf4j;
}
