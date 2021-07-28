module com.arvatosystems.t9t.email.be.commons {
	exports com.arvatosystems.t9t.email.be.commons.impl;

	requires transitive com.arvatosystems.t9t.base.api;
	requires transitive com.arvatosystems.t9t.email.api;
	requires transitive com.arvatosystems.t9t.email.sapi;
	requires transitive com.arvatosystems.t9t.email.be.smtp;
	requires transitive com.google.common;
	requires transitive commons.email;
	requires transitive de.jpaw.annotations;
	requires transitive de.jpaw.bonaparte.api;
	requires transitive de.jpaw.bonaparte.api.media;
	requires transitive de.jpaw.bonaparte.core;
	requires transitive de.jpaw.jdp;
	requires transitive jakarta.activation;
	requires transitive jakarta.mail;
	requires transitive org.eclipse.xtext.xbase.lib;
	requires org.slf4j;
}