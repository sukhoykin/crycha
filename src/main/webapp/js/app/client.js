function CrypticClient(url) {

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

      var command = JSON.parse(event.data);

      console.log('RECEIVE');
      console.log(command);

      switch (command.command) {

      case 'debug-totp':
        if (self.onDebug) {
          self.onDebug(command.data);
        }
        break;

      case 'authenticate':
        authenticateCommand(command);
        break;

      case 'data':
        console.log(cipher.decrypt(command.payload));
        break;

      case 'authorize':
        authorizeCommand(command);
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

  var sendCommand = function(command) {

    if (cipher.isTLSEnabled()) {

      command = JSON.stringify(command);
      command = cipher.encrypt(command);

      command = {
        command : 'envelope',
        payload : command,
        signature : cipher.sign(command)
      };
    }

    socket.send(JSON.stringify(command));

    console.log('SEND');
    console.log(command);
  }

  var identify = function(email) {

    sendCommand({
      command : 'identify',
      email : email
    });
  }

  var authenticate = function(totp) {

    cipher.setTOTP(totp);

    var dhPub = cipher.getDHPublicKey();
    var dsaPub = cipher.getDSAPublicKey();

    sendCommand({
      command : 'authenticate',
      dh : cipher.encodePublicKey(dhPub),
      dsa : cipher.encodePublicKey(dsaPub),
      signature : cipher.signPublicKeys(dhPub, dsaPub)
    });
  }

  var authenticateCommand = function(command) {

    var dhPub = cipher.decodePublicKey(command.dh);
    var dsaPub = cipher.decodePublicKey(command.dsa);

    var signature = cipher.signPublicKeys(dhPub, dsaPub);

    if (signature !== command.signature) {
      socket.close(401);
      return;
    }

    cipher.setServerKeys(dhPub, dsaPub);

    if (self.onAuthenticate) {
      self.onAuthenticate();
    }
  }

  var authorize = function(clientId) {

    sendCommand({
      command : 'authorize',
      email : clientId
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
