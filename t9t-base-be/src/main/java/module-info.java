module com.arvatosystems.t9t.base.be {
	exports com.arvatosystems.t9t.base.be.stubs;
	exports com.arvatosystems.t9t.base.be.impl;
	exports com.arvatosystems.t9t.base.be.lifecycle;
	exports com.arvatosystems.t9t.base.be.execution;
	exports com.arvatosystems.t9t.base.be.request;
	exports com.arvatosystems.t9t.base.be.events;
	exports com.arvatosystems.t9t.base.be.eventhandler;
	exports com.arvatosystems.t9t.base.be.search;

	requires com.arvatosystems.t9t.annotations;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.cfg;
	requires com.arvatosystems.t9t.server;
	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.auth;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires org.slf4j;
	requires reflections;
}
