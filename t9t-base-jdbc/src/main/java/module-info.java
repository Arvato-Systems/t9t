module com.arvatosystems.t9t.base.jpa.rl {
	exports com.arvatosystems.t9t.base.jdbc;
	exports com.arvatosystems.t9t.base.jdbc.impl;

	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive com.arvatosystems.t9t.init;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.bonaparte.refs;
	requires transitive de.jpaw.jdp;
	requires transitive java.sql;
	requires org.slf4j;
	requires com.zaxxer.hikari;
}