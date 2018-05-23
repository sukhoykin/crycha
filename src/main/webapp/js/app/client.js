function CrypticClient(url) {

  var INVALID_COMMAND = 3500;
  var INVALID_SIGNATURE = 3501;

  var self = this;
  var cipher = new CipherSuite();

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

    try {

      var message = JSON.parse(event.data);

      console.log('RECEIVE');
      console.log(message);

      switch (message.command) {

      case 'debug-totp':
        if (self.onDebug) {
          self.onDebug(message.data);
        }
        break;

      case 'authenticate':
        authenticateCommand(message);
        break;

      case 'data':
        console.log(cipher.decrypt(message.payload));
        break;

      case 'authorize':
        authorizeCommand(message);
        break;

      default:
        console.error('Invalid command: ' + message.command);
        socket.close(INVALID_COMMAND);
        return;
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

    if (cipher.isTLSEnabled()) {

      message = JSON.stringify(message);
      message = cipher.encrypt(message);

      message = {
        command : 'envelope',
        payload : message,
        signature : cipher.sign(message)
      };
    }

    socket.send(JSON.stringify(message));

    console.log('SEND');
    console.log(message);
  }

  var identify = function(email) {

    sendMessage({
      command : 'identify',
      email : email
    });
  }

  var authenticate = function(totp) {

    cipher.setTOTP(totp);

    var dh = cipher.getDHKey();
    var dsa = cipher.getDSAKey();

    sendMessage({
      command : 'authenticate',
      dh : cipher.encodePublicKey(dh),
      dsa : cipher.encodePublicKey(dsa),
      signature : cipher.signPublicKeys(dh, dsa)
    });
  }

  var authenticateCommand = function(message) {

    var dh = cipher.decodePublicKey(message.dh);
    var dsa = cipher.decodePublicKey(message.dsa);

    var signature = cipher.signPublicKeys(dh, dsa);

    if (signature !== message.signature) {
      socket.close(INVALID_SIGNATURE);
      return;
    }

    cipher.setUpTLS(dh, dsa);

    if (self.onAuthenticate) {
      self.onAuthenticate();
    }
  }

  var authorize = function(email) {

    sendMessage({
      command : 'authorize',
      email : email
    });
  }

  var authorizeCommand = function(command) {

  }

  self.onReady = null;
  self.onDebug = null;
  self.onAuthenticate = null;

  self.identify = identify;
  self.authenticate = authenticate;
  self.authorize = authorize;
}
