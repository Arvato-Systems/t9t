module com.arvatosystems.t9t.io.be.intern {
	exports com.arvatosystems.t9t.io.be.request;
	exports com.arvatosystems.t9t.out.be.async;
	exports com.arvatosystems.t9t.out.be.impl.internal;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.genconf.api;
	requires transitive com.arvatosystems.t9t.io.api;
	requires transitive com.arvatosystems.t9t.io.be;
	requires transitive com.arvatosystems.t9t.io.sapi;
	requires transitive com.arvatosystems.t9t.translation.sapi;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive org.joda.time;
	requires org.slf4j;
}