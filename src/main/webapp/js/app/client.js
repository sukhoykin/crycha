'use strict';

function ClientSession(session, email, originDh, originDsa) {

  var self = this;

  var hashjs = Cryptic.hashjs;
  var aesjs = Cryptic.aesjs;

  var isOrigin = originDh === undefined;
  var isAuthorized = false;

  var dh, dsa;
  var aesEncrypt;
  var aesDecrypt;

  self.getEmail = function() {
    return email;
  }

  self.authorize = function(recipDh, recipDsa) {

    if (recipDh === undefined || recipDsa === undefined) {

      recipDh = session.getDh();
      recipDsa = session.getDsa();
      dh = originDh;
      dsa = originDsa;

    } else {

      originDh = session.getDh();
      originDsa = session.getDsa();
      dh = recipDh
      dsa = recipDsa;
    }

    var sharedSecret = session.getDh().derive(dh.getPublic());

    var sha256 = hashjs.sha256();
    sha256.update(sharedSecret.toArray());
    sha256.update(originDh.getPublic().encode());
    sha256.update(recipDh.getPublic().encode());

    var sharedSecret = sha256.digest();
    var IV = session.getDsa().derive(dsa.getPublic()).toArray().slice(0, 16);

    aesEncrypt = new aesjs.ModeOfOperation.ctr(sharedSecret, IV);
    aesDecrypt = new aesjs.ModeOfOperation.ctr(sharedSecret, IV);

    isAuthorized = true;
  }

  function encrypt(message) {

    message = aesjs.utils.utf8.toBytes(message);
    message = aesEncrypt.encrypt(message);
    message = aesjs.utils.hex.fromBytes(message);

    return message;
  }

  function decrypt(message) {

    message = aesjs.utils.hex.toBytes(message);
    message = aesDecrypt.decrypt(message);
    message = aesjs.utils.utf8.fromBytes(message);

    return message;
  }

  function sign(message) {

    message = aesjs.utils.hex.toBytes(message);

    var sha256 = hashjs.sha256();
    sha256.update(message);

    var signature = session.getDsa().sign(sha256.digest());
    signature = aesjs.utils.hex.fromBytes(signature.toDER());

    return signature;
  }

  function verify(message, signature) {

    message = aesjs.utils.hex.toBytes(message);

    var sha256 = hashjs.sha256();
    sha256.update(message);

    return dsa.verify(sha256.digest(), signature);
  }

  self.onDeliver = function(client, message) {
  }

  self.onDeliverFail = function(client, error) {
  }

  self.isAuthorized = function() {
    return isAuthorized;
  }

  self.isOrigin = function() {
    return isOrigin;
  }

  self.deliver = function(message) {

    message = JSON.stringify(message);
    message = encrypt(message);

    message = {
      command : 'deliver',
      email : email,
      payload : message,
      signature : sign(message)
    };

    session.sendMessage(message);
  }

  self.onMessage = function(message) {

    try {

      if (!verify(message.payload, message.signature)) {
        throw new Error('Invalid message signature');
      }

      message = decrypt(message.payload);
      message = JSON.parse(message);

      self.onDeliver(self, message);

    } catch (e) {
      self.onDeliverFail(self, e);
    }
  }
}
