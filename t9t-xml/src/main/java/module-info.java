module com.arvatosystems.t9t.xml {
	exports com.arvatosystems.t9t.xml;
	exports com.arvatosystems.t9t.xml.exports;
	exports com.arvatosystems.t9t.xml.imports;

	requires transitive com.arvatosystems.t9t.auth.api;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.io.api;
	requires transitive com.arvatosystems.t9t.io.be;
	requires transitive com.arvatosystems.t9t.io.sapi;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.xml;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive de.jpaw.jpaw.xml;
	requires transitive java.xml;
	requires transitive java.xml.bind;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive org.joda.time;
	requires org.slf4j;
}