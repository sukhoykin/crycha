'use strict';

var hash = require('hash.js');
var EC = require('elliptic').ec;

var ec = new EC('curve25519');

var key1 = ec.genKeyPair();
var key2 = ec.genKeyPair();

var shared1 = key1.derive(key2.getPublic());
var shared2 = key2.derive(key1.getPublic());

console.log('Both shared secrets are BN instances');
console.log(shared1.toString(16));
console.log(shared2.toString(16));

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
