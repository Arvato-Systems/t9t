module com.arvatosystems.t9t.io.jpa {
	exports com.arvatosystems.t9t.io.jpa.persistence.impl;
	exports com.arvatosystems.t9t.io.jpa.mapping;
	exports com.arvatosystems.t9t.io.jpa.persistence;
	exports com.arvatosystems.t9t.io.jpa.request;
	exports com.arvatosystems.t9t.io.jpa.mapping.impl;
	exports com.arvatosystems.t9t.out.jpa.impl;
	exports com.arvatosystems.t9t.io.jpa.entities;

	requires transitive com.arvatosystems.t9t.annotations.jpa;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.jpa;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive com.arvatosystems.t9t.core.api;
	requires transitive com.arvatosystems.t9t.core.sapi;
	requires transitive com.arvatosystems.t9t.io.api;
	requires transitive com.arvatosystems.t9t.io.sapi;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive de.jpaw.persistence.core;
	requires transitive de.jpaw.persistence.refs;
	requires transitive java.persistence;
	requires transitive java.validation;
	requires transitive org.joda.time;
	requires org.slf4j;
}