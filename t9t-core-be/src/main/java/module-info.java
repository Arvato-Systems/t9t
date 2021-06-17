module com.arvatosystems.t9t.core.be {
	exports com.arvatosystems.t9t.event.be.impl;
	exports com.arvatosystems.t9t.bucket.be.impl;
	exports com.arvatosystems.t9t.batch.be.request;
	exports com.arvatosystems.t9t.statistics.be.impl;
	exports com.arvatosystems.t9t.bucket.be.request;
	exports com.arvatosystems.t9t.monitoring.services.impl;
	exports com.arvatosystems.t9t.core.be.impl;
	exports com.arvatosystems.t9t.monitoring.be.request;
	exports com.arvatosystems.t9t.plugins.be.request;
	exports com.arvatosystems.t9t.core.be.request;
	exports com.arvatosystems.t9t.plugins.be.impl;

	requires com.arvatosystems.t9t.annotations;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.be;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.cfg;
	requires com.arvatosystems.t9t.core.api;
	requires com.arvatosystems.t9t.core.sapi;
	requires com.arvatosystems.t9t.init;
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
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires org.slf4j;
}