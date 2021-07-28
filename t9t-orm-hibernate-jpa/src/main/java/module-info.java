module com.arvatosystems.t9t.orm.jpa.hibernate {
	exports com.arvatosystems.t9t.orm.jpa.hibernate.impl;

	requires c3p0;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.jpa;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive de.jpaw.jdp;
	requires transitive java.persistence;
	requires java.sql;
	requires java.activation;
	requires org.hibernate.orm.core;
	requires org.slf4j;
}
