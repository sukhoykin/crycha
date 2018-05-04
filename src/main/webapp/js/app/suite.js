function CipherSuite(curve, hashjs) {

  var self = this;

  var dh = curve.genKeyPair();
  var dsa = curve.genKeyPair();

  var signPublicKeys = function(key) {

    var mac = hashjs.hmac(hashjs.sha256, key, 'hex');

    console.log(dh.getPublic().encodeCompressed());
    console.log(dsa.getPublic().encodeCompressed());

    mac.update(dh.getPublic().encodeCompressed());
    mac.update(dsa.getPublic().encodeCompressed());

    return mac.digest('hex');
  }

  self.publicKeys = {
    dh : dh.getPublic().encodeCompressed('hex'),
    dsa : dsa.getPublic().encodeCompressed('hex')
  };

  self.signPublicKeys = signPublicKeys;
}
