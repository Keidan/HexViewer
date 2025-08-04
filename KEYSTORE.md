# KEYSTORE

## Creation

```
keytool -genkeypair -v \
  -keystore app/keystore.jks \
  -alias HexViewer \
  -storetype JKS \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
  -dname "CN=YourCN, OU=YourOrganizationUnit, O=YourOrganization, L=YourCity, ST=YourState, C=XX"
```

## GitHub Actions

Create the following keys in Settings > Secrets and variables > Actions > New repository secret:
* `SIGNING_KEY` -> Base64-encoded content of the app/keystore.jks file.
* `KEYSTORE_PROPERTIES` -> The contents of the properties file (see Local section).

To convert the jks file to base64:

```base64 -w 0 app/keystore.jks > app/keystore.b64```

## Local

Create a keystore.properties file with the following content:
```
storeFile=keystore.jks
storePassword=Password for the keystore.
keyAlias=Alias of the key.
keyPassword=Password for the key.
```
