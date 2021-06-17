module com.arvatosystems.t9t.base.jpa.jta {
	exports com.arvatosystems.t9t.base.jpa.jta.impl;

	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.cfg;
	requires com.arvatosystems.t9t.init;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.persistence.core;
	requires de.jpaw.persistence.refs;
	requires java.naming;
	requires java.persistence;
	requires java.transaction;
	requires org.slf4j;
}