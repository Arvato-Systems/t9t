module com.arvatosystems.t9t.cluster.kafka {
	exports com.arvatosystems.t9t.cluster.be.kafka;

	requires transitive com.arvatosystems.t9t.annotations;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jdp;
	requires transitive kafka.clients;
	requires org.slf4j;
}