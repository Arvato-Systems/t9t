module com.arvatosystems.t9t.io.be {
	exports com.arvatosystems.t9t.out.be.impl.output;
	exports com.arvatosystems.t9t.in.be.impl;
	exports com.arvatosystems.t9t.in.be.impl.formatparser;
	exports com.arvatosystems.t9t.out.be.impl;
	exports com.arvatosystems.t9t.out.be.impl.formatgenerator;
	exports com.arvatosystems.t9t.out.be;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.io.api;
	requires com.arvatosystems.t9t.io.sapi;
	requires com.arvatosystems.t9t.server;
	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires java.xml;
	requires java.xml.bind;
	requires org.apache.commons.lang3;
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires org.slf4j;
}