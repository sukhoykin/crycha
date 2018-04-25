'use strict';

var socket = new WebSocket('wss://' + location.host + location.pathname + 'crycha');

socket.onopen = function() {
	console.log('onopen');
	
	var message = {command: 'IDENTIFY', email: 'vadim@example.com'};
	socket.send(JSON.stringify(message));
}

socket.onclose = function(event) {
	console.log('onclose');
	console.log(event);
};

socket.onmessage = function(event) {
	console.log('onmessage');
	console.log(event);
};

socket.onerror = function(error) {
	console.log('onerror');
	console.log(error);
};
