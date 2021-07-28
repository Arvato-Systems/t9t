module com.arvatosystems.t9t.io.be.camel {
	exports com.arvatosystems.t9t.io.be.camel.service;
	exports com.arvatosystems.t9t.io.be.camel.init;
	exports com.arvatosystems.t9t.out.be.impl.output.camel;
	exports com.arvatosystems.t9t.in.be.camel;

	requires transitive camel.api;
	requires transitive camel.attachments;
	requires transitive camel.base.engine;
	requires transitive camel.core.engine;
	requires transitive camel.core.model;
	requires transitive camel.file;
	requires transitive camel.mail;
	requires transitive camel.support;
	requires transitive com.arvatosystems.t9t.annotations;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive com.arvatosystems.t9t.io.api;
	requires transitive com.arvatosystems.t9t.io.sapi;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive jakarta.activation;
	requires transitive org.apache.commons.io;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive org.joda.time;
	requires org.slf4j;
}