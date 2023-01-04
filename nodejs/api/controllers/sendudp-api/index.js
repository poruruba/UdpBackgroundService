'use strict';

const HELPER_BASE = process.env.HELPER_BASE || "/opt/";
const Response = require(HELPER_BASE + 'response');

const dgram = require('dgram');
const socket = dgram.createSocket('udp4');

exports.handler = async (event, context, callback) => {
	var body = JSON.parse(event.body);
	console.log(body);

	if( event.path == '/udpsend-send' ){
    return new Promise((resolve, reject) =>{
      socket.send(JSON.stringify(body.params), body.port, body.ipaddress, (err, bytes) => {
          if (err)
            return reject(err);
          resolve(new Response({}));
      });
    });
	}else

	{
		throw "unknown endpoint";
	}
};
