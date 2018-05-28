'use strict';

//ClientSession
function ServerSession(url) {

  var self = this;

  window.addEventListener("beforeunload", function(event) {
    self.close();
  });

  var hashjs = Cryptic.hashjs;
  var aesjs = Cryptic.aesjs;

  var curve = new Cryptic.elliptic.ec(Curve25519.curveName);

  var dhKeyPair = curve.genKeyPair();
  var dsaKeyPair = curve.genKeyPair();

  var TOTP;
  var aesEncrypt;
  var aesDecrypt;
  var serverDsa;

  var socket = new WebSocket(url);

  socket.onopen = onOpen;
  socket.onmessage = onMessage;
  socket.onerror = onError;
  socket.onclose = onClose;

  function onOpen() {
    self.onOpen();
  }

  function onMessage(event) {

    var message;

    try {

      message = JSON.parse(event.data);

    } catch (e) {
      console.error('%s %s: %s', e.name, e.message, event.data);
      socket.close(CrypticCloseCode.SERVER_INVALID_COMMAND, e.message);
      return;
    }

    console.log('RECEIVE');
    console.log(message);

    switch (message.command) {

    case 'debug':
      self.authenticate(message.data);
      break;

    case 'authenticate':

      var dh, dsa;

      try {

        dh = curve.keyFromPublic(message.dh, "hex");
        dsa = curve.keyFromPublic(message.dsa, "hex");

      } catch (e) {
        console.error('%s %s', e.name, e.message);
        socket.close(CrypticCloseCode.SERVER_INVALID_KEY, e.message);
        return;
      }

      var signature = signPublicKeys(dh.getPublic(), dsa.getPublic());

      if (signature !== message.signature) {
        socket.close(CrypticCloseCode.SERVER_INVALID_SIGNATURE);
        return;
      }

      var sharedSecret = dhKeyPair.derive(dh.getPublic());

      var sha256 = hashjs.sha256();
      sha256.update(sharedSecret.toArray());
      sha256.update(dh.getPublic().encode());
      sha256.update(dhKeyPair.getPublic().encode());

      var sharedSecret = sha256.digest();
      var iv = aesjs.utils.hex.toBytes(TOTP);

      serverDsa = dsa;

      aesEncrypt = new aesjs.ModeOfOperation.cbc(sharedSecret, iv);
      aesDecrypt = new aesjs.ModeOfOperation.cbc(sharedSecret, iv);

      self.sendMessage = sendMessage;
      self.onAuthenticate();

      break;

    case 'envelope':

      if (!verify(message.payload, message.signature)) {
        socket.close(CrypticCloseCode.SERVER_INVALID_SIGNATURE);
        return;
      }

      message = decrypt(message.payload);
      message = JSON.parse(message);

      self.onMessage(message);

    default:
      console.error('Invalid command: %s', message.command);
      socket.close(CrypticCloseCode.SERVER_INVALID_COMMAND, message.command);
      return;
    }
  }

  function onError(error) {
    console.error(error);
  }

  function onClose(event) {
    self.onClose(event);
  }

  function sendMessage(message) {

    try {

      if (aesEncrypt) {

        message = JSON.stringify(message);
        message = encrypt(message);

        message = {
          command : 'envelope',
          payload : message,
          signature : sign(message)
        };
      }

      socket.send(JSON.stringify(message));

      console.log('SEND');
      console.log(message);

    } catch (e) {
      console.error(e);
      socket.close(CrypticCloseCode.CLIENT_ERROR, e.message);
    }
  }

  function signPublicKeys(dh, dsa) {

    var mac = hashjs.hmac(hashjs.sha256, TOTP, 'hex');

    mac.update(dh.encodeCompressed());
    mac.update(dsa.encodeCompressed());

    return mac.digest('hex');
  }

  function encrypt(message) {

    message = aesjs.utils.utf8.toBytes(message);
    message = aesjs.padding.pkcs7.pad(message);
    message = aesEncrypt.encrypt(message);
    message = aesjs.utils.hex.fromBytes(message);

    return message;
  }

  function decrypt(message) {

    message = aesjs.utils.hex.toBytes(message);
    message = aesDecrypt.decrypt(message);
    message = aesjs.padding.pkcs7.strip(message);
    message = aesjs.utils.utf8.fromBytes(message);

    return message;
  }

  function sign(message) {

    message = aesjs.utils.hex.toBytes(message);

    var sha256 = hashjs.sha256();
    sha256.update(message);

    var signature = dsaKeyPair.sign(sha256.digest());
    signature = aesjs.utils.hex.fromBytes(signature.toDER());

    return signature;
  }

  function verify(message, signature) {

    message = aesjs.utils.hex.toBytes(message);

    var sha256 = hashjs.sha256();
    sha256.update(message);

    return serverDsa.verify(sha256.digest(), signature);
  }

  self.onOpen = function() {
  }
  self.onClose = function(event) {
  }
  self.onAuthenticate = function(event) {
  }
  self.onMessage = function(message) {
  }

  self.identify = function(email) {
    sendMessage({
      command : 'identify',
      email : email
    });
  }

  self.authenticate = function(totp) {

    TOTP = totp;

    var dh = dhKeyPair.getPublic().encodeCompressed('hex');
    var dsa = dsaKeyPair.getPublic().encodeCompressed('hex');
    var signature = signPublicKeys(dhKeyPair.getPublic(), dsaKeyPair.getPublic());

    sendMessage({
      command : 'authenticate',
      dh : dh,
      dsa : dsa,
      signature : signature
    });
  }

  self.sendMessage = function(message) {
    message = 'Secure session is not established';
    console.error(message);
    socket.close(CrypticCloseCode.CLIENT_ERROR, message);
  }

  self.close = function() {
    socket.onclose = function() {
    }
    socket.close();
  }
}
