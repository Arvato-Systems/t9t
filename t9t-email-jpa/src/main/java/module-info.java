module com.arvatosystems.t9t.email.jpa {
	exports com.arvatosystems.t9t.email.jpa.request;
	exports com.arvatosystems.t9t.email.jpa.impl;
	exports com.arvatosystems.t9t.email.jpa.mapping;
	exports com.arvatosystems.t9t.email.jpa.persistence.impl;
	exports com.arvatosystems.t9t.email.jpa.mapping.impl;
	exports com.arvatosystems.t9t.email.jpa.persistence;
	exports com.arvatosystems.t9t.email.jpa.entities;

	requires transitive com.arvatosystems.t9t.annotations.jpa;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.jpa;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.core.jpa;
	requires transitive com.arvatosystems.t9t.email.api;
	requires transitive com.arvatosystems.t9t.email.sapi;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.google.common;
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