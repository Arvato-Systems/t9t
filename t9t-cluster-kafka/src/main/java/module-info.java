module com.arvatosystems.t9t.cluster.kafka {
	exports com.arvatosystems.t9t.cluster.be.kafka;

	requires com.arvatosystems.t9t.annotations;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.cfg;
	requires de.jpaw.bonaparte.core;
	requires de.jpaw.jdp;
	requires kafka.clients;
	requires org.slf4j;
}