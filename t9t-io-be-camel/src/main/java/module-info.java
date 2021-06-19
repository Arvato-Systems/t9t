module com.arvatosystems.t9t.io.be.camel {
	exports com.arvatosystems.t9t.io.be.camel.service;
	exports com.arvatosystems.t9t.io.be.camel.init;
	exports com.arvatosystems.t9t.out.be.impl.output.camel;
	exports com.arvatosystems.t9t.in.be.camel;

	requires camel.api;
	requires camel.attachments;
	requires camel.base.engine;
	requires camel.core.engine;
	requires camel.core.model;
	requires camel.file;
	requires camel.mail;
	requires camel.support;
	requires com.arvatosystems.t9t.annotations;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.cfg;
	requires com.arvatosystems.t9t.io.api;
	requires com.arvatosystems.t9t.io.sapi;
	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires jakarta.activation;
	requires org.apache.commons.io;
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires org.slf4j;
}