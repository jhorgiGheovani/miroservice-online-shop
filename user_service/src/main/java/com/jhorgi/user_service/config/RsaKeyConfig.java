package com.jhorgi.user_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Base64;

@Component
public class RsaKeyConfig {

    @Value("${app.keys.public-key-path}")
    private String publicKeyPath;

    public void persistPublicKey(byte[] encoded) throws IOException {
        writePemFile(publicKeyPath, "PUBLIC KEY", encoded);
    }

    private void writePemFile(String path, String type, byte[] encoded) throws IOException {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded);
        String pem = "-----BEGIN " + type + "-----\n" + base64 + "\n-----END " + type + "-----\n";
        Files.writeString(Path.of(path), pem);
    }
}
