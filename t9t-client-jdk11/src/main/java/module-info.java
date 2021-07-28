module com.arvatosystems.t9t.client {
	exports com.arvatosystems.t9t.client.init;
	exports com.arvatosystems.t9t.client.connection;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.util;
	requires transitive java.naming;
	requires org.slf4j;
	requires transitive java.net.http;
	requires java.base;
}