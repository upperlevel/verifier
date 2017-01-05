package xyz.upperlevel.verifier.proto.ssl;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class SslClientContext {
    public static final SslContext CONTEXT;

    //This is inside of the class to make it a little bit harder to replace the certificate
    private static final String CERT = "";

    static {
        SslContext context;

        if("".equals(CERT))
            context = null;
        else {
            X509Certificate certificate = decodeCer(CERT);

            try {
                context = SslContextBuilder
                        .forClient()
                        .trustManager(certificate)
                        .build();
            } catch (Exception e) {
                throw new Error(
                        "Failed to initialize the client-side SSLContext", e);
            }
        }

        CONTEXT = context;
    }

    private static X509Certificate decodeCer(String cert) {
        try {
            return (X509Certificate) CertificateFactory
                    .getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(cert)));
        } catch (CertificateException e) {
            throw new IllegalStateException("Cannot find X.509 encoder!", e);
        }
    }
}
