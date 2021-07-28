module com.arvatosystems.t9t.vertx.cluster {
	exports com.arvatosystems.t9t.vertx.cluster;

	requires transitive com.arvatosystems.t9t.jdp;
	requires transitive com.arvatosystems.t9t.vertx.base;
	requires transitive com.hazelcast.core;
	requires transitive de.jpaw.annotations;
	requires transitive io.vertx.clustermanager.hazelcast;
	requires transitive io.vertx.core;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires org.slf4j;
}