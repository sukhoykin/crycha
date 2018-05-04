'use strict';

var client = new CrypticClient();

client.onReady = function() {

  try {
    client.identify('a@example.com');
  } catch (e) {
    console.log(e);
  }
}

client.onDebug = function(data) {

  try {
    client.authenticate(data);
  } catch (e) {
    console.log(e);
  }
}
