module com.arvatosystems.t9t.email.be.smtp {
	exports com.arvatosystems.t9t.email.be.smtp.impl;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.email.api;
	requires transitive com.arvatosystems.t9t.email.sapi;
	requires transitive com.google.common;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jdp;
	requires transitive de.jpaw.jpaw.enums;
	requires transitive de.jpaw.jpaw.util;
	requires transitive jakarta.activation;
	requires transitive jakarta.mail;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires org.slf4j;
}