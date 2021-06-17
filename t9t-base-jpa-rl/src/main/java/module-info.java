module com.arvatosystems.t9t.base.jpa.rl {
	exports com.arvatosystems.t9t.base.jpa.rl.impl;

	requires com.arvatosystems.t9t.base.jpa;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.cfg;
	requires com.arvatosystems.t9t.init;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.persistence.core;
	requires de.jpaw.persistence.refs;
	requires java.persistence;
	requires org.slf4j;
}