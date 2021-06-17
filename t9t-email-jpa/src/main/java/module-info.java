module com.arvatosystems.t9t.email.jpa {
	exports com.arvatosystems.t9t.email.jpa.request;
	exports com.arvatosystems.t9t.email.jpa.impl;
	exports com.arvatosystems.t9t.email.jpa.mapping;
	exports com.arvatosystems.t9t.email.jpa.persistence.impl;
	exports com.arvatosystems.t9t.email.jpa.mapping.impl;
	exports com.arvatosystems.t9t.email.jpa.persistence;
	exports com.arvatosystems.t9t.email.jpa.entities;

	requires com.arvatosystems.t9t.annotations.jpa;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.jpa;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.core.jpa;
	requires com.arvatosystems.t9t.email.api;
	requires com.arvatosystems.t9t.email.sapi;
	requires com.arvatosystems.t9t.server;
	requires com.google.common;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.media;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires de.jpaw.persistence.core;
	requires de.jpaw.persistence.refs;
	requires java.persistence;
	requires java.validation;
	requires org.joda.time;
	requires org.slf4j;
}