'use strict';

var client = new CrypticClient();

client.onOpen = function() {
  client.identify(getParameterByName('id') + '@example.com');
}

client.onDebug = function(data) {
    client.authenticate(data);
}

client.onAuthenticate = function() {
}

client.onClose = function(event) {
  console.log('Close: ' + event.code);
}

function test() {
  client.authorize('b@example.com');
}

function getParameterByName(name, url) {
  if (!url)
    url = window.location.href;
  name = name.replace(/[\[\]]/g, "\\$&");
  var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"), results = regex.exec(url);
  if (!results)
    return null;
  if (!results[2])
    return '';
  return decodeURIComponent(results[2].replace(/\+/g, " "));
}
