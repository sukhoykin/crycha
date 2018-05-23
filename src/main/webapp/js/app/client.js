function CrypticClient(url) {

  var CLIENT_ERROR = 3500;
  var INVALID_COMMAND = 3501;
  var INVALID_SIGNATURE = 3502;

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
    if (self.onOpen) {
      self.onOpen();
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

      case 'envelope':

        message = decryptMessage(message);

        switch (message.command) {

        case 'authorize':
          authorizeCommand(message);
          break;

        default:
          console.error('Invalid command: ' + message.command);
          socket.close(INVALID_COMMAND);
          return;
        }

        break;

      default:
        console.error('Invalid command: ' + message.command);
        socket.close(INVALID_COMMAND);
        return;
      }

    } catch (e) {
      console.error(e);
      socket.close(CLIENT_ERROR);
    }
  }

  var onError = function(error) {
    console.error('Connection error');
    console.error(error);
    socket.close(CLIENT_ERROR);
  }

  var onClose = function(event) {

    console.log('Disconnected');

    if (self.onClose) {
      self.onClose(event);
    }
  }

  var sendMessage = function(message) {

    try {

      if (cipher.isTLSEnabled()) {
        message = encryptMessage(message);
      }

      socket.send(JSON.stringify(message));

      console.log('SEND');
      console.log(message);

    } catch (e) {
      console.error(e);
      socket.close(CLIENT_ERROR);
    }
  }

  var encryptMessage = function(message) {

    message = JSON.stringify(message);
    message = cipher.encrypt(message);

    message = {
      command : 'envelope',
      payload : message,
      signature : cipher.sign(message)
    };

    return message;
  }

  var decryptMessage = function(message) {

    if (!cipher.verify(message.payload, message.signature)) {
      socket.close(INVALID_SIGNATURE);
      return;
    }

    message = cipher.decrypt(message.payload);
    message = JSON.parse(message);

    return message;
  }

  var authenticateCommand = function(message) {

    var dh = cipher.decodeKey(message.dh);
    var dsa = cipher.decodeKey(message.dsa);

    var signature = cipher.signPublicKeys(dh.getPublic(), dsa.getPublic());

    if (signature !== message.signature) {
      socket.close(INVALID_SIGNATURE);
      return;
    }

    cipher.setUpTLS(dh, dsa);

    if (self.onAuthenticate) {
      self.onAuthenticate();
    }
  }

  var authorizeCommand = function(message) {

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

  var authorize = function(email) {

    sendMessage({
      command : 'authorize',
      email : email
    });
  }

  self.onOpen = null;
  self.onDebug = null;
  self.onAuthenticate = null;
  self.onClose = null;

  self.identify = identify;
  self.authenticate = authenticate;
  self.authorize = authorize;
}
