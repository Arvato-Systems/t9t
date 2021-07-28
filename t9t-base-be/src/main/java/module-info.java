module com.arvatosystems.t9t.base.be {
	exports com.arvatosystems.t9t.base.be.stubs;
	exports com.arvatosystems.t9t.base.be.impl;
	exports com.arvatosystems.t9t.base.be.lifecycle;
	exports com.arvatosystems.t9t.base.be.execution;
	exports com.arvatosystems.t9t.base.be.request;
	exports com.arvatosystems.t9t.base.be.events;
	exports com.arvatosystems.t9t.base.be.eventhandler;
	exports com.arvatosystems.t9t.base.be.search;

	requires transitive com.arvatosystems.t9t.annotations;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.auth;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive org.joda.time;
	requires org.eclipse.xtend.lib;
	requires org.slf4j;
	requires transitive reflections;
}
