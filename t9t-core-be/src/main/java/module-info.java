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

	requires transitive com.arvatosystems.t9t.annotations;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.be;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive com.arvatosystems.t9t.core.api;
	requires transitive com.arvatosystems.t9t.core.sapi;
	requires transitive com.arvatosystems.t9t.init;
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
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive org.joda.time;
	requires org.slf4j;
}