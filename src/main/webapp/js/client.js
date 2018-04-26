'use strict';

var hash = require('hash.js');
var elliptic = require('elliptic');

/**
 * Definition of Curve25519 in Weierstrass form for Java server compatibility.
 * 
 * See: https://github.com/indutny/elliptic/pull/113
 */

var name = 'curve25519-weier';
var options = {
  type : 'short',
  prime : 'p25519',
  p : '7fffffffffffffff ffffffffffffffff ffffffffffffffff ffffffffffffffed',
  a : '2aaaaaaaaaaaaaaa aaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaa aaaaaa984914a144',
  b : '7b425ed097b425ed 097b425ed097b425 ed097b425ed097b4 260b5e9c7710c864',
  n : '1000000000000000 0000000000000000 14def9dea2f79cd6 5812631a5cf5d3ed',
  hash : hash.sha256,
  gRed : false,
  g : [ '2aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaad245a',
      '20ae19a1b8a086b4e01edd2c7748d14c923d4d7e6d7c61b229e9c5a27eced3d9' ]
};

Object.defineProperty(elliptic.curves, name, {
  configurable : true,
  enumerable : true,
  get : function() {
    var curve = new elliptic.curves.PresetCurve(options);
    Object.defineProperty(elliptic.curves, name, {
      configurable : true,
      enumerable : true,
      value : curve
    });
    return curve;
  }
});

/**
 * Derive of ECDH shared secret.
 */

var ec = new elliptic.ec('curve25519-weier');
var key = ec.genKeyPair();

var socket = new WebSocket('wss://' + location.host + location.pathname + 'crycha');

socket.onopen = function() {

  console.log('onopen');

  console.log('public key');
  console.log(key.getPublic());
  console.log('SEND: ' + key.getPublic().encodeCompressed('hex'));

  socket.send(key.getPublic().encodeCompressed('hex'));
}

socket.onclose = function(event) {
  console.log('onclose');
  console.log(event);
};

socket.onmessage = function(event) {
  console.log('onmessage');
  console.log(event.data);

  console.log('derived');
  var serverPublicKey = ec.keyFromPublic(event.data, "hex").getPublic();
  var derived = key.derive(serverPublicKey);
  console.log(derived.toString(16));
  console.log(' server: ' + serverPublicKey.encode('hex'));
  console.log(' client: ' + key.getPublic().encode('hex'));

  console.log('shared');
  var sha256 = hash.sha256();
  sha256.update(derived.toArray());
  sha256.update(serverPublicKey.encode());
  sha256.update(key.getPublic().encode());
  // var shared = sha256.digest();
  console.log(sha256.digest('hex'));
};

socket.onerror = function(error) {
  console.log('onerror');
  console.log(error);
};
