module com.arvatosystems.t9t.io.be.jackson {
	exports com.arvatosystems.t9t.in.be.jackson.impl;
	exports com.arvatosystems.t9t.out.be.jackson;
	exports com.arvatosystems.t9t.in.be.jackson;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.io.api;
	requires com.arvatosystems.t9t.io.be;
	requires com.arvatosystems.t9t.io.sapi;
	requires com.arvatosystems.t9t.server;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.datatype.joda;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.util;
	requires org.slf4j;
}