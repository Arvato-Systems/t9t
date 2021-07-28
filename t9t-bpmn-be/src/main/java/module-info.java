module com.arvatosystems.t9t.bpmn.be {
	exports com.arvatosystems.t9t.bpmn.be.steps;
	exports com.arvatosystems.t9t.bpmn.be.services.impl;
	exports com.arvatosystems.t9t.bpmn.be.request;

	requires transitive com.arvatosystems.t9t.annotations;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.bpmn;
	requires transitive com.arvatosystems.t9t.bpmn.sapi;
	requires transitive com.arvatosystems.t9t.core.api;
	requires transitive com.arvatosystems.t9t.core.sapi;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.util;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires transitive org.joda.time;
	requires org.slf4j;
}