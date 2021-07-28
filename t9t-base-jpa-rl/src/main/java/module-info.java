module com.arvatosystems.t9t.base.jpa.rl {
	exports com.arvatosystems.t9t.base.jpa.rl.impl;

	requires transitive com.arvatosystems.t9t.base.jpa;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive com.arvatosystems.t9t.init;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.persistence.core;
	requires transitive de.jpaw.persistence.refs;
	requires transitive java.persistence;
	requires org.slf4j;
}