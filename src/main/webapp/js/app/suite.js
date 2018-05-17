function CipherSuite() {

  var self = this;

  var elliptic = require('elliptic');
  var hashjs = require('hash.js');
  var aesjs = require('aesjs');

  Curve25519.defineCurve(elliptic.curves, hashjs);

  var curve = new elliptic.ec(Curve25519.name);

  var dh = curve.genKeyPair();
  var dsa = curve.genKeyPair();

  var totp;
  var serverDsa;
  var aesEncrypt;
  var aesDecrypt;

  var setTOTP = function(key) {
    totp = key;
  }

  var getDHPublicKey = function() {
    return dh.getPublic();
  }

  var getDSAPublicKey = function() {
    return dsa.getPublic();
  }

  var encodePublicKey = function(key) {
    return key.encodeCompressed('hex');
  }

  var decodePublicKey = function(key) {
    return curve.keyFromPublic(key, "hex").getPublic();
  }

  var signPublicKeys = function(dhPub, dsaPub) {

    var mac = hashjs.hmac(hashjs.sha256, totp, 'hex');

    mac.update(dhPub.encodeCompressed());
    mac.update(dsaPub.encodeCompressed());

    return mac.digest('hex');
  }

  var setServerKeys = function(dhPub, dhDsa) {

    var sharedSecret = dh.derive(dhPub);

    var sha256 = hashjs.sha256();
    sha256.update(sharedSecret.toArray());
    sha256.update(dhPub.encode());
    sha256.update(dh.getPublic().encode());

    var sharedSecret = sha256.digest();
    var iv = aesjs.utils.hex.toBytes(totp);

    console.log('sharedSecret: ' + aesjs.utils.hex.fromBytes(sharedSecret));
    console.log('iv: ' + aesjs.utils.hex.fromBytes(iv));

    serverDsa = dhDsa;
    aesEncrypt = new aesjs.ModeOfOperation.cbc(sharedSecret, iv);
    aesDecrypt = new aesjs.ModeOfOperation.cbc(sharedSecret, iv);
  }

  var encrypt = function(command) {

    console.log(command);
    command = aesjs.utils.utf8.toBytes(command);
    console.log(command);
    command = aesjs.padding.pkcs7.pad(command);
    console.log(command);
    command = aesEncrypt.encrypt(command);
    console.log(command);
    command = aesjs.utils.hex.fromBytes(command);
    console.log(command);

    return command;
  }

  var decrypt = function(command) {

    command = aesjs.utils.hex.toBytes(command);
    command = aesDecrypt.decrypt(command);
    command = aesjs.padding.pkcs7.strip(command);
    command = aesjs.utils.utf8.fromBytes(command);

    return command;
  }

  self.setTOTP = setTOTP;
  self.getDHPublicKey = getDHPublicKey;
  self.getDSAPublicKey = getDSAPublicKey;
  self.encodePublicKey = encodePublicKey;
  self.decodePublicKey = decodePublicKey;
  self.signPublicKeys = signPublicKeys;
  self.setServerKeys = setServerKeys;
  self.encrypt = encrypt;
  self.decrypt = decrypt;
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
