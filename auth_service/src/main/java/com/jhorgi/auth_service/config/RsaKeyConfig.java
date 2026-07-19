package com.jhorgi.auth_service.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class RsaKeyConfig {

    @Value("${app.keys.private-key-path}")
    private String privateKeyPath;

    @Value("${app.keys.public-key-path}")
    private String publicKeyPath;

    private final AtomicReference<KeyPair> currentKeyPair = new AtomicReference<>();

    @PostConstruct
    public void init() throws Exception {
        PrivateKey privateKey = loadPrivateKey();
        PublicKey publicKey = loadPublicKey();
        currentKeyPair.set(new KeyPair(publicKey, privateKey));
    }

    public PrivateKey getPrivateKey() {
        return currentKeyPair.get().getPrivate();
    }

    public PublicKey getPublicKey() {
        return currentKeyPair.get().getPublic();
    }

    public void updateKeyPair(KeyPair newKeyPair) {
        currentKeyPair.set(newKeyPair);
    }

    public void persistKeyPair(KeyPair keyPair) throws IOException {
        writePemFile(privateKeyPath, "PRIVATE KEY", keyPair.getPrivate().getEncoded());
        writePemFile(publicKeyPath, "PUBLIC KEY", keyPair.getPublic().getEncoded());
    }

    private PrivateKey loadPrivateKey() throws Exception {
        byte[] keyBytes = readPemFile(privateKeyPath);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey() throws Exception {
        byte[] keyBytes = readPemFile(publicKeyPath);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private byte[] readPemFile(String path) throws Exception {
        String pem = Files.readString(Path.of(path))
                .replaceAll("-----.*-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(pem);
    }

    private void writePemFile(String path, String type, byte[] encoded) throws IOException {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded);
        String pem = "-----BEGIN " + type + "-----\n" + base64 + "\n-----END " + type + "-----\n";
        Files.writeString(Path.of(path), pem);
    }
}
