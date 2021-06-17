module com.arvatosystems.t9t.vertx.base {
	exports com.arvatosystems.t9t.base.vertx.impl;
	exports com.arvatosystems.t9t.base.vertx;

	requires com.arvatosystems.t9t.annotations;
	requires com.arvatosystems.t9t.auth.jwt;
	requires transitive com.arvatosystems.t9t.base.api;
	requires com.arvatosystems.t9t.base.sapi;
	requires com.arvatosystems.t9t.cfg;
	requires com.arvatosystems.t9t.jdp;
	requires com.arvatosystems.t9t.server;
	requires com.google.common;
	requires de.jpaw.annotations;
	requires de.jpaw.bonaparte.api;
	requires de.jpaw.bonaparte.api.auth;
	requires transitive de.jpaw.bonaparte.core;
	requires de.jpaw.jdp;
	requires de.jpaw.jpaw.util;
	requires io.netty.buffer;
	requires io.netty.common;
	requires io.vertx.auth.common;
	requires transitive io.vertx.core;
	requires transitive io.vertx.web;
	requires jsap;
	requires org.eclipse.xtext.xbase.lib;
	requires org.slf4j;
}