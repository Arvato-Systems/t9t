module com.arvatosystems.t9t.base.jpa.st {
	exports com.arvatosystems.t9t.base.jpa.st;
	exports com.arvatosystems.t9t.base.jpa.st.util;
	exports com.arvatosystems.t9t.base.jpa.st.impl;

	requires c3p0;
	requires com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.jpa;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.cfg;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.bonaparte.refs;
	requires de.jpaw.jdp;
	requires de.jpaw.persistence.refs;
	requires java.desktop;
	requires java.naming;
	requires java.sql;
	requires java.persistence;
	requires org.hibernate.orm.core;
	requires org.slf4j;
	requires spring.beans;
	requires spring.context;
	requires spring.jdbc;
	requires spring.orm;
	requires spring.tx;
}