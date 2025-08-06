# KEYSTORE

## Creation

```
keytool -genkeypair -v \
  -keystore keystore.jks \
  -alias hexviewer \
  -storetype JKS \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
  -dname "CN=YourCN, OU=YourOrganizationUnit, O=YourOrganization, L=YourCity, ST=YourState, C=XX"
```

## GitHub Actions

Create the following keys in Create the following keys in the `GitHub web project > Secrets and variables > Actions > New repository secret`:
* `SIGNING_KEY` -> Base64-encoded content of the keystore.jks file.
* `KEYSTORE_PROPERTIES` -> Base64-encoded content of the keystore.properties file.

To convert the jks file to base64:

```
base64 -w 0 keystore.jks > keystore-jks.b64
base64 -w 0 keystore.properties > keystore-properties.b64
```

## Local

Create a keystore.properties file with the following content:
```
storeFile=keystore.jks
storePassword=Password for the keystore.
keyAlias=Alias of the key.
keyPassword=Password for the key.
```

Please use Unix line end encoding (LF).
