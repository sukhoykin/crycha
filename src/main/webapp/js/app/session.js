'use strict';

function ServiceSession(url) {

  var self = this;

  window.addEventListener("beforeunload", function(event) {
    self.close();
  });

  var hashjs = Cryptic.hashjs;
  var aesjs = Cryptic.aesjs;

  var curve = Cryptic.curve;

  var clientDh = curve.genKeyPair();
  var clientDsa = curve.genKeyPair();

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
      self.close(CrypticCloseCode.SERVER_INVALID_COMMAND, e.message);
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
        self.close(CrypticCloseCode.SERVER_INVALID_KEY, e.message);
        return;
      }

      var signature = signPublicKeys(dh.getPublic(), dsa.getPublic());

      if (signature !== message.signature) {
        self.close(CrypticCloseCode.SERVER_INVALID_SIGNATURE);
        return;
      }

      var sharedSecret = clientDh.derive(dh.getPublic());

      var sha256 = hashjs.sha256();
      sha256.update(sharedSecret.toArray());
      sha256.update(dh.getPublic().encode());
      sha256.update(clientDh.getPublic().encode());

      var sharedSecret = sha256.digest();
      var IV = aesjs.utils.hex.toBytes(TOTP);

      serverDsa = dsa;

      aesEncrypt = new aesjs.ModeOfOperation.cbc(sharedSecret, IV);
      aesDecrypt = new aesjs.ModeOfOperation.cbc(sharedSecret, IV);

      self.sendMessage = sendMessage;
      self.onAuthenticate();

      break;

    case 'envelope':

      if (!verify(message.payload, message.signature)) {
        self.close(CrypticCloseCode.SERVER_INVALID_SIGNATURE);
        return;
      }

      message = decrypt(message.payload);
      message = JSON.parse(message);

      self.onMessage(message);
      break;

    default:
      console.error('Invalid command: %s', message.command);
      self.close(CrypticCloseCode.SERVER_INVALID_COMMAND, message.command);
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

        console.log('COMMAND');
        console.log(message);

        message = JSON.stringify(message);
        message = encrypt(message);

        message = {
          command : 'envelope',
          payload : message,
          signature : sign(message)
        };
      }

      if (socket.readyState != 1) {
        throw new Error('Socket is not open');
      }

      socket.send(JSON.stringify(message));

      console.log('SEND');
      console.log(message);

    } catch (e) {
      console.error(e);
      self.close(CrypticCloseCode.CLIENT_ERROR, e.message);
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

    var signature = clientDsa.sign(sha256.digest());
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
  self.onAuthenticate = function(event) {
  }
  self.onMessage = function(message) {
  }
  self.onClose = function(event) {
  }

  self.identify = function(email) {
    sendMessage({
      command : 'identify',
      email : email
    });
  }

  self.authenticate = function(totp) {

    TOTP = totp;

    var dh = clientDh.getPublic().encodeCompressed('hex');
    var dsa = clientDsa.getPublic().encodeCompressed('hex');
    var signature = signPublicKeys(clientDh.getPublic(), clientDsa.getPublic());

    sendMessage({
      command : 'authenticate',
      dh : dh,
      dsa : dsa,
      signature : signature
    });
  }

  self.getDh = function() {
    return clientDh;
  }

  self.getDsa = function() {
    return clientDsa;
  }

  self.sendMessage = function(message) {
    message = 'Secure session is not established';
    console.error(message);
    self.close(CrypticCloseCode.CLIENT_ERROR, message);
  }

  self.close = function(closeCode, closeReason) {

    if (closeCode === undefined) {
      socket.onclose = function() {
      }
      socket.close();
    } else {
      socket.close(closeCode, closeReason);
    }
  }
}
