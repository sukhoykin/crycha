function CrypticClient(url) {

  var self = this;

  var elliptic = require('elliptic');
  var hashjs = require('hash.js');

  Curve25519.defineCurve(elliptic.curves, hashjs);

  var curve25519 = new elliptic.ec(Curve25519.name);
  var cipher = new CipherSuite(curve25519, hashjs);

  if (url === undefined) {
    url = 'wss://' + location.host + location.pathname + 'api';
  }

  var socket = new WebSocket(url);

  socket.onopen = function() {
    onOpen();
  }

  socket.onmessage = function(event) {
    onMessage(event);
  }

  socket.onclose = function(event) {
    onClose(event);
  }

  socket.onerror = function(error) {
    onError(error);
  }

  var onOpen = function() {
    console.log('Connected to %s', url);
    if (self.onReady) {
      self.onReady();
    }
  }

  var onMessage = function(event) {

    console.log('Message received');
    console.log(event.data);

    try {

      var command = JSON.parse(event.data);

      switch (command.command) {
      case 'debug-totp':
        if (self.onDebug) {
          self.onDebug(command.data);
        }
        break;
      }

    } catch (e) {
      console.error(e);
    }
  }

  var onClose = function(event) {
    console.log('Disconnected');
    console.log(event);
  }

  var onError = function(error) {
    console.log('Connection error');
    console.log(error);
  }

  var sendMessage = function(message) {

    socket.send(JSON.stringify(message));

    console.log('Message send');
    console.log(message);
  }

  var identify = function(email) {

    sendMessage({
      command : 'identify',
      email : email
    });
  }

  var authenticate = function(totp) {

    sendMessage({
      command : 'authenticate',
      dh : cipher.publicKeys.dh,
      dsa : cipher.publicKeys.dsa,
      signature : cipher.signPublicKeys(totp)
    });
  }

  self.onReady = null;
  self.onDebug = null;

  self.identify = identify;
  self.authenticate = authenticate;
}

/**
 * Definition of Curve25519 in Weierstrass form for Java server compatibility.
 * 
 * See: https://github.com/indutny/elliptic/pull/113
 */

function Curve25519() {
}

Curve25519.name = 'curve25519-weier';
Curve25519.defineCurve = function(curves, hashjs) {

  var name = Curve25519.name;

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
