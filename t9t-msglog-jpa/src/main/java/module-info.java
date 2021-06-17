module com.arvatosystems.t9t.msglog.jpa {
//	exports com.arvatosystems.t9t.msglog.jpa.entities;
	exports com.arvatosystems.t9t.msglog.jpa.request;
//	exports com.arvatosystems.t9t.msglog.jpa.persistence.impl;
//	exports com.arvatosystems.t9t.msglog.jpa.persistence;
//	exports com.arvatosystems.t9t.msglog.jpa.mapping;
//	exports com.arvatosystems.t9t.msglog.jpa.mapping.impl;
//	exports com.arvatosystems.t9t.msglog.jpa.impl;

	requires com.arvatosystems.t9t.annotations.jpa;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.jpa;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.msglog.api;
	requires com.arvatosystems.t9t.msglog.sapi;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.enums;
	requires de.jpaw.jpaw.util;
	requires de.jpaw.persistence.core;
	requires de.jpaw.persistence.refs;
	requires java.validation;
	requires java.persistence;
	requires org.joda.time;
	requires org.slf4j;
}