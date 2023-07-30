const ports = [];

console.group('Init Worker');
console.log('Creating EventSource');
const eventSrc = new EventSource('/api/events', { withCredentials: true });
console.log('EventSource created');
	
eventSrc.onopen = function () {
   console.log('connection is established');
};

eventSrc.onerror = function (event) {
    console.log('connection state: ' + eventSrc.readyState + ', error: ' + event);
};

console.log('Add EventSource TaskEvent listener');		
eventSrc.addEventListener('TaskEvent', function(event) {

	const message = JSON.parse(event.data);		
	for (var i = 0; i < ports.length; i++) {			
		ports[i].postMessage(message);
	}

}, false);
	
console.groupEnd();

self.onconnect = function(event){		
	
	var port = event.ports[0];
	
	if(!ports.includes(port)){
		console.log('Register new port');
		console.log(port);
		ports.push(port);
	}				
	
	port.onmessage = function(msg){
		console.group('Worker message');		
		console.log(msg);
		console.groupEnd();	
	};
	
};

	