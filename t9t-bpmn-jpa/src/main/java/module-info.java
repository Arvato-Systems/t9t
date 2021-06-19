module com.arvatosystems.t9t.bpmn.jpa {
	exports com.arvatosystems.t9t.bpmn.jpa.entities;
	exports com.arvatosystems.t9t.bpmn.jpa.engine.impl;
	exports com.arvatosystems.t9t.bpmn.jpa.impl;
	exports com.arvatosystems.t9t.bpmn.jpa.mapping;
	exports com.arvatosystems.t9t.bpmn.jpa.mapping.impl;
	exports com.arvatosystems.t9t.bpmn.jpa.persistence;
	exports com.arvatosystems.t9t.bpmn.jpa.persistence.impl;
	exports com.arvatosystems.t9t.bpmn.jpa.request;

	requires com.arvatosystems.t9t.annotations.jpa;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.jpa;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.bpmn;
	requires com.arvatosystems.t9t.bpmn.sapi;
	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.util;
	requires de.jpaw.persistence.core;
	requires de.jpaw.persistence.refs;
	requires java.persistence;
	requires java.validation;
	requires org.joda.time;
	requires org.slf4j;
}