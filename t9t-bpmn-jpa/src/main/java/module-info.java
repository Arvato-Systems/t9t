module com.arvatosystems.t9t.bpmn.jpa {
	exports com.arvatosystems.t9t.bpmn.jpa.entities;
	exports com.arvatosystems.t9t.bpmn.jpa.engine.impl;
	exports com.arvatosystems.t9t.bpmn.jpa.impl;
	exports com.arvatosystems.t9t.bpmn.jpa.mapping;
	exports com.arvatosystems.t9t.bpmn.jpa.mapping.impl;
	exports com.arvatosystems.t9t.bpmn.jpa.persistence;
	exports com.arvatosystems.t9t.bpmn.jpa.persistence.impl;
	exports com.arvatosystems.t9t.bpmn.jpa.request;

	requires transitive com.arvatosystems.t9t.annotations.jpa;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.jpa;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.bpmn;
	requires transitive com.arvatosystems.t9t.bpmn.sapi;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.util;
	requires transitive de.jpaw.persistence.core;
	requires transitive de.jpaw.persistence.refs;
	requires transitive java.persistence;
	requires transitive java.validation;
	requires transitive org.joda.time;
	requires org.slf4j;
}