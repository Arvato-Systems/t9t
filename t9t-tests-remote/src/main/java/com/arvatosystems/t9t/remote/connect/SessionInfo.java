package com.arvatosystems.t9t.remote.connect;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import jakarta.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SessionInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionInfo.class);

    private SessionInfo() { }

    /*
     * Logs SSL session information.
     * To extract the DN, we would need extra JARs from org.bouncycastle.asn1, for example:
     * public static String certToCn(X509Certificate cert) {
     *     X500Principal principal = cert.getSubjectX500Principal();
     *     X500Name x500name = new X500Name( principal.getName() );
     *     RDN cn = x500name.getRDNs(BCStyle.CN)[0];
     *     return IETFUtils.valueToString(cn.getFirst().getValue());
     * }
     */

    public static void logSessionInfo(@Nonnull final SSLSession session, @Nonnull final String who) {
        // print the certificate chain (if any)
        try {
            LOGGER.info("{}'s principal name is {}", who, session.getPeerPrincipal().getName());
            final Certificate[] peerCerts = session.getPeerCertificates();
            if ((peerCerts != null) && (peerCerts.length > 0)) {
                // have at least one peer certificate - index 0 is the peer itself, further entries are the chain entries, the last entry is the root.
                for (int i = 0; i < peerCerts.length; i++) {
                    LOGGER.debug("{}'s certificate chain[{}] = {}", who, i, peerCerts[i]);
                }
                if (peerCerts[0] instanceof X509Certificate cert) {
                    LOGGER.info("{}'s certificate DN is {}", who, cert.getSubjectX500Principal().getName()); // same output as the principal above!
                }
            }
        } catch (final SSLPeerUnverifiedException e) {
            LOGGER.info("{}: Using an SSL connection, but the peer could not be verified", who);
        }
    }
}
