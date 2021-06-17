module com.arvatosystems.t9t.orm.jpa.hibernate {
	exports com.arvatosystems.t9t.orm.jpa.hibernate.impl;

	requires c3p0;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.jpa;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.cfg;
	requires de.jpaw.jdp;
	requires java.persistence;
	requires java.sql;
	requires org.hibernate.orm.core;
	requires org.slf4j;
}
