'use strict';

function CrypticService(url) {

  var self = this;

  if (url === undefined) {
    url = 'wss://' + location.host + location.pathname + 'api';
  }

  var session = new ServerSession(url);

  session.onOpen = onOpen;
  session.onAuthenticate = onAuthenticate;
  session.onMessage = onMessage;
  session.onClose = onClose;

  var clients = {};

  function onOpen() {
    self.onOpen();
  }

  function onAuthenticate() {
    self.onAuthenticate();
  }

  function onMessage(message) {
    console.log('onMessage');
    console.log(message);
  }

  function onClose(event) {
    self.onClose(event);
  }

  self.onOpen = function() {
  }
  self.onAuthenticate = function() {
  }
  self.onClose = function(event) {
  }

  self.getUrl = function() {
    return url;
  }

  self.identify = function(email) {
    session.identify(email);
  }

  self.authenticate = function(totp) {
    session.authenticate(totp);
  }

  self.close = function() {
    server.close();
  }
}

/**
 * Definition of Curve25519 in Weierstrass form for Java server compatibility.
 * 
 * See: https://github.com/indutny/elliptic/pull/113
 */

function Curve25519() {
}

Curve25519.curveName = 'curve25519-weier';
Curve25519.defineCurve = function(curves, hashjs) {

  var name = Curve25519.curveName;

  var options = {
    type : 'short',
    prime : 'p25519',
    p : '7fffffffffffffff ffffffffffffffff ffffffffffffffff ffffffffffffffed',
    a : '2aaaaaaaaaaaaaaa aaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaa aaaaaa984914a144',
    b : '7b425ed097b425ed 097b425ed097b425 ed097b425ed097b4 260b5e9c7710c864',
    n : '1000000000000000 0000000000000000 14def9dea2f79cd6 5812631a5cf5d3ed',
    hash : hashjs.sha256,
    gRed : false,
    g : [ '2aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaad245a',
        '20ae19a1b8a086b4e01edd2c7748d14c923d4d7e6d7c61b229e9c5a27eced3d9' ]
  };

  Object.defineProperty(curves, name, {
    configurable : true,
    enumerable : true,
    get : function() {
      var curve = new curves.PresetCurve(options);
      Object.defineProperty(curves, name, {
        configurable : true,
        enumerable : true,
        value : curve
      });
      return curve;
    }
  });
}

function Cryptic() {
}

Cryptic.elliptic = require('elliptic');
Cryptic.hashjs = require('hash.js');
Cryptic.aesjs = require('aesjs');

Curve25519.defineCurve(Cryptic.elliptic.curves, Cryptic.hashjs);

function CrypticCloseCode() {
}

CrypticCloseCode.DUPLICATE_AUTHENTICATION = 4100;
CrypticCloseCode.CLIENT_ERROR = 4400;
CrypticCloseCode.CLIENT_INVALID_COMMAND = 4401;
CrypticCloseCode.CLIENT_INVALID_SIGNATURE = 4402;
CrypticCloseCode.CLIENT_INVALID_KEY = 4403;
CrypticCloseCode.SERVER_ERROR = 4500;
CrypticCloseCode.SERVER_INVALID_COMMAND = 4501;
CrypticCloseCode.SERVER_INVALID_SIGNATURE = 4502;
CrypticCloseCode.SERVER_INVALID_KEY = 4503;