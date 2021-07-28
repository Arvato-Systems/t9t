module com.arvatosystems.t9t.msglog.jpa {
//	exports com.arvatosystems.t9t.msglog.jpa.entities;
	exports com.arvatosystems.t9t.msglog.jpa.request;
//	exports com.arvatosystems.t9t.msglog.jpa.persistence.impl;
//	exports com.arvatosystems.t9t.msglog.jpa.persistence;
//	exports com.arvatosystems.t9t.msglog.jpa.mapping;
//	exports com.arvatosystems.t9t.msglog.jpa.mapping.impl;
//	exports com.arvatosystems.t9t.msglog.jpa.impl;

	requires transitive com.arvatosystems.t9t.annotations.jpa;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.jpa;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.msglog.api;
	requires transitive com.arvatosystems.t9t.msglog.sapi;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive de.jpaw.persistence.core;
	requires transitive de.jpaw.persistence.refs;
	requires transitive java.validation;
	requires transitive java.persistence;
	requires transitive org.joda.time;
	requires org.slf4j;
}