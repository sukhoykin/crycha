'use strict';

var service = new CrypticService();

service.onOpen = function() {
  console.log('Connected to %s', service.getUrl());
  service.identify(getParameterByName('id') + '@example.com');
}

service.onAuthenticate = function() {
  console.log('onAuthenticate');
}

service.onAuthorize = function(client) {
  console.log('onAuthorize');
}

service.onClose = function(eventOrClient) {

  if (eventOrClient instanceof ClientSession) {
    console.log('onClose %s', eventOrClient.getEmail());
  } else {
    console.log('Disconnected %s %s', event.code, event.reason);
  }
}

function authorize(id) {
  service.authorize(id + '@example.com');
  return 0;
}

function prohibit(id) {
  service.prohibit(id + '@example.com');
  return 0;
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
