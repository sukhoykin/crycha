'use strict';

var client = new CrypticClient();

client.onReady = function() {

  try {
    client.identify(getParameterByName('id') + '@example.com');
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

client.onAuthenticate = function() {

  try {
   // client.authorize('b@example.com');
  } catch (e) {
    console.log(e);
  }
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
