module com.arvatosystems.t9t.base.jpa.jta {
	exports com.arvatosystems.t9t.base.jpa.jta.impl;

	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive com.arvatosystems.t9t.init;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.persistence.core;
	requires transitive de.jpaw.persistence.refs;
	requires transitive java.naming;
	requires transitive java.persistence;
	requires transitive java.transaction;
	requires org.slf4j;
}