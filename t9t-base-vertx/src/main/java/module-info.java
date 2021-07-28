module com.arvatosystems.t9t.vertx.base {
	exports com.arvatosystems.t9t.base.vertx.impl;
	exports com.arvatosystems.t9t.base.vertx;

	requires transitive com.arvatosystems.t9t.annotations;
	requires transitive com.arvatosystems.t9t.auth.jwt;
	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.base.sapi;
	requires transitive com.arvatosystems.t9t.cfg;
	requires transitive com.arvatosystems.t9t.jdp;
	requires transitive com.arvatosystems.t9t.server;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.auth;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.util;
	requires transitive io.netty.buffer;
	requires transitive io.netty.common;
	requires transitive io.vertx.auth.common;
	requires transitive io.vertx.core;
	requires transitive io.vertx.web;
	requires transitive jsap;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires org.slf4j;
}