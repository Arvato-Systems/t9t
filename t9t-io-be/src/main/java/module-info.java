module com.arvatosystems.t9t.io.be {
	exports com.arvatosystems.t9t.out.be.impl.output;
	exports com.arvatosystems.t9t.in.be.impl;
	exports com.arvatosystems.t9t.in.be.impl.formatparser;
	exports com.arvatosystems.t9t.out.be.impl;
	exports com.arvatosystems.t9t.out.be.impl.formatgenerator;
	exports com.arvatosystems.t9t.out.be;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.io.api;
	requires transitive com.arvatosystems.t9t.io.sapi;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive java.xml;
	requires transitive java.xml.bind;
	requires transitive org.apache.commons.lang3;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive org.joda.time;
	requires org.slf4j;
}