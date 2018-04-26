# (DRAFT)

## Development
### Requirements
* JDK
* NPM

```
git clone
npm install
bower install
grunt
```

## Communication protocol

3-tier protocol encryption:
* Authentication tier (HTTPS)
* Authorization and deliver tier (Client-Server TLS)
* Messaging tier (Client-Client TLS)

three encryption tiers
* Secured WebSocket
* Client-Server hybrid encryption (ECDHE-ECDSA-AES)
* Client-Client end-to-end hybrid encryption (ECDHE-EdDSA-AES)

### Authentication

Client initiate [secured](https://en.wikipedia.org/wiki/Transport_Layer_Security) WebScoket ([wss](https://en.wikipedia.org/wiki/HTTPS)) connection to the server.

### Authorization and deliver


### Messaging
