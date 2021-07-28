module com.arvatosystems.t9t.bpmn.sapi {
	exports com.arvatosystems.t9t.bpmn.pojo;
	exports com.arvatosystems.t9t.bpmn.services;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.bpmn;
	requires transitive com.arvatosystems.t9t.core.sapi;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.joda.time;
}