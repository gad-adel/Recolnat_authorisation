Create certificates for keycloak and docker images (for test ) CA for production
For keycloak
keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=server" -alias server -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -keystore server.keystore

Import to MS CM : 

keytool -exportcert -alias server -keystore server.keystore -rfc -file  cm-key-pair-cert.pem