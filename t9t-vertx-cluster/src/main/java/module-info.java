module com.arvatosystems.t9t.vertx.cluster {
	exports com.arvatosystems.t9t.vertx.cluster;

	requires com.arvatosystems.t9t.jdp;
	requires com.arvatosystems.t9t.vertx.base;
	requires com.hazelcast.core;
	requires de.jpaw.annotations;
	requires io.vertx.clustermanager.hazelcast;
	requires io.vertx.core;
	requires org.eclipse.xtext.xbase.lib;
	requires org.slf4j;
}