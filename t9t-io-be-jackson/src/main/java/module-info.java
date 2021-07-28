module com.arvatosystems.t9t.io.be.jackson {
	exports com.arvatosystems.t9t.in.be.jackson.impl;
	exports com.arvatosystems.t9t.out.be.jackson;
	exports com.arvatosystems.t9t.in.be.jackson;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.io.api;
	requires transitive com.arvatosystems.t9t.io.be;
	requires transitive com.arvatosystems.t9t.io.sapi;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.fasterxml.jackson.annotation;
	requires transitive com.fasterxml.jackson.core;
	requires transitive com.fasterxml.jackson.databind;
	requires transitive com.fasterxml.jackson.datatype.joda;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.util;
	requires org.slf4j;
}