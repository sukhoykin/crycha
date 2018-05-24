'use strict';

var service = new CrypticService();

service.onOpen = function() {
  console.log('Connected to %s', service.getUrl());
  service.identify(getParameterByName('id') + '@example.com');
}

service.onDebug = function(data) {
  service.authenticate(data);
}

service.onAuthenticate = function() {
  console.log('onAuthenticate');
}

service.onAuthorize = function(service) {
  service.authorize(service);
}

service.onClose = function(event) {
  console.log('Disconnected %s %s', event.code, event.reason);
}

function test() {
  service.authorize('b@example.com');
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
