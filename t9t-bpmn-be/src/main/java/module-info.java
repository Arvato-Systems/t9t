module com.arvatosystems.t9t.bpmn.be {
	exports com.arvatosystems.t9t.bpmn.be.steps;
	exports com.arvatosystems.t9t.bpmn.be.services.impl;
	exports com.arvatosystems.t9t.bpmn.be.request;

	requires com.arvatosystems.t9t.annotations;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.bpmn;
	requires com.arvatosystems.t9t.bpmn.sapi;
	requires com.arvatosystems.t9t.core.api;
	requires com.arvatosystems.t9t.core.sapi;
	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.util;
	requires org.eclipse.xtext.xbase.lib;
	requires org.joda.time;
	requires org.slf4j;
}