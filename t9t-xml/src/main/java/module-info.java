module com.arvatosystems.t9t.xml {
	exports com.arvatosystems.t9t.xml;
	exports com.arvatosystems.t9t.xml.exports;
	exports com.arvatosystems.t9t.xml.imports;

	requires com.arvatosystems.t9t.auth.api;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.io.api;
	requires com.arvatosystems.t9t.io.be;
	requires com.arvatosystems.t9t.io.sapi;
	requires com.arvatosystems.t9t.server;
	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.xml;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires de.jpaw.jpaw.xml;
	requires java.xml;
	requires java.xml.bind;
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires org.slf4j;
}