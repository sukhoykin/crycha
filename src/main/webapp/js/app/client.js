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

    aesEncrypt = new aesjs.ModeOfOperation.cbc(sharedSecret, IV);
    aesDecrypt = new aesjs.ModeOfOperation.cbc(sharedSecret, IV);

    isAuthorized = true;
  }

  self.isAuthorized = function() {
    return isAuthorized;
  }

  self.isOrigin = function() {
    return isOrigin;
  }

  self.deliver = function(message) {

  }
}
