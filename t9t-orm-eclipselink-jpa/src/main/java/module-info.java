module com.arvatosystems.t9t.orm.jpa.eclipselink {
	exports com.arvatosystems.t9t.orm.jpa.eclipselink.impl;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.jpa;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive de.jpaw.jdp;
	requires java.sql;
	requires transitive java.persistence;
	requires org.eclipse.persistence.core;
	requires org.slf4j;
}
