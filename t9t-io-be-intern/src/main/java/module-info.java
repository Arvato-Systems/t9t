module com.arvatosystems.t9t.io.be.intern {
	exports com.arvatosystems.t9t.io.be.request;
	exports com.arvatosystems.t9t.out.be.async;
	exports com.arvatosystems.t9t.out.be.impl.internal;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.genconf.api;
	requires com.arvatosystems.t9t.io.api;
	requires com.arvatosystems.t9t.io.be;
	requires com.arvatosystems.t9t.io.sapi;
	requires com.arvatosystems.t9t.translation.sapi;
	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires org.slf4j;
}