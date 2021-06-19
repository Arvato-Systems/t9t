module com.arvatosystems.t9t.io.jpa {
	exports com.arvatosystems.t9t.io.jpa.persistence.impl;
	exports com.arvatosystems.t9t.io.jpa.mapping;
	exports com.arvatosystems.t9t.io.jpa.persistence;
	exports com.arvatosystems.t9t.io.jpa.request;
	exports com.arvatosystems.t9t.io.jpa.mapping.impl;
	exports com.arvatosystems.t9t.out.jpa.impl;
	exports com.arvatosystems.t9t.io.jpa.entities;

	requires com.arvatosystems.t9t.annotations.jpa;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.jpa;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.cfg;
	requires com.arvatosystems.t9t.core.api;
	requires com.arvatosystems.t9t.core.sapi;
	requires com.arvatosystems.t9t.io.api;
	requires com.arvatosystems.t9t.io.sapi;
	requires com.google.common;
	requires de.jpaw.annotations;
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