module com.arvatosystems.t9t.orm.jpa.eclipselink {
	exports com.arvatosystems.t9t.orm.jpa.eclipselink.impl;

	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.jpa;
	requires com.arvatosystems.t9t.cfg;
	requires de.jpaw.jdp;
	requires java.sql;
	requires java.persistence;
	requires org.eclipse.persistence.core;
	requires org.slf4j;
}