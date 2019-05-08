var app = require('express')();
var server = require('http').createServer(app);
var io = require('socket.io')(server);
var fs = require('fs');
var ms = require('mediaserver');
var UPDATE_VERSION=201809;
server.listen(3000, {secure: true});


app.get('/', function (req, res) {
	res.sendFile(__dirname + '/index.html');
	console.log("variable one = "+req.params.di +" variable two : "+req.params.id);
});

app.get('/music/:ID', function(req,res){
	
	ms.pipe(req, res, __dirname + '/music/'+req.params.ID+'.mp3');
	console.log("piping music id "+req.params.ID);
	
	
});

io.on('connection', function (socket) {
	console.log("user connected");
	socket.emit('',UPDATE_VERSION)
	socket.on('UPDATE_REQUEST',function(){
		
		socket.emit('DATA', "bunch of data");
	

	});

	
    
});