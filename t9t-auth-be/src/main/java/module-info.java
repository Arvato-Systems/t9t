module com.arvatosystems.t9t.auth.be {
	exports com.arvatosystems.t9t.base.be.auth;
	exports com.arvatosystems.t9t.authz.be.api;
	exports com.arvatosystems.t9t.authc.be.stubs;
	exports com.arvatosystems.t9t.auth.be.jwt;
	exports com.arvatosystems.t9t.authc.be.api;
	exports com.arvatosystems.t9t.auth.be.impl;
	exports com.arvatosystems.t9t.auth.be.request;

	requires transitive com.arvatosystems.t9t.auth.api;
	requires transitive com.arvatosystems.t9t.auth.apiext;
	requires transitive com.arvatosystems.t9t.auth.jwt;
	requires transitive com.arvatosystems.t9t.auth.sapi;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.be;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive com.arvatosystems.t9t.doc.api;
	requires transitive com.arvatosystems.t9t.email.api;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.auth;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive java.naming;
	requires transitive org.eclipse.xtend.lib;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive org.joda.time;
	requires org.slf4j;
}
