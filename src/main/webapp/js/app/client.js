'use strict';

function ClientSession(email, dh, dsa) {

  var self = this;

  var getEmail = function() {
    return email;
  }

  self.getEmail = getEmail;
}
