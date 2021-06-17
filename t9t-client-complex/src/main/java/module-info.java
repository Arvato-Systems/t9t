module com.arvatosystems.t9t.client {
	exports com.arvatosystems.t9t.client.init;
	exports com.arvatosystems.t9t.client.connection;

	requires com.arvatosystems.t9t.base.api;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.util;
	requires java.naming;
	requires org.slf4j;
}