module com.arvatosystems.t9t.bpmn.sapi {
	exports com.arvatosystems.t9t.bpmn.pojo;
	exports com.arvatosystems.t9t.bpmn.services;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.bpmn;
	requires com.arvatosystems.t9t.core.sapi;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jpaw.util;
	requires org.joda.time;
}