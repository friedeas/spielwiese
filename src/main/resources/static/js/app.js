console.log('Creating SharedWorker');
const taskMessageWorker  = new SharedWorker('js/worker.js', 'TaskMessageWorker');
console.log('SharedWorker created');

taskMessageWorker.port.onmessage = function(event){
  var message = event.data;
  console.group('EventSource message received');
  console.log( message );
  console.groupEnd();
  
  updateTaskUI(message);

};

taskMessageWorker.port.start();
//  This will trigger a message event in the worker
taskMessageWorker.port.postMessage('Go');


function updateTaskUI(taskEvent){
	updateTableUI(taskEvent);
	updateModalDialogUI(taskEvent);
};

function updateTableUI(taskEvent){
	taskRow = document.getElementById(taskEvent.taskId);
	if(taskRow){
		updateTaskRow(taskRow, taskEvent);
	} else {
		var tbodyRef = document.querySelector('#taskTable').getElementsByTagName('tbody')[0];
		renderTask(taskEvent, tbodyRef);		
	}
};

function updateTaskRow(taskRow, taskEvent){
	td = taskRow.querySelectorAll("td");
  	td[0].textContent = '' + taskEvent.taskId;
  	td[1].textContent = '' + taskEvent.taskStatus;
  	if(taskEvent.payload){
  		td[2].querySelectorAll('span')[0].textContent = '' + taskEvent.payload;
  	} 
  	if(taskEvent.taskStatus == 'ACTIVE'){
		var activDivIndicator = document.querySelector('#activeTaskTemplate');
		var clone = document.importNode(activDivIndicator.content, true);
		td[2].appendChild(clone);
	} else if(taskEvent.taskStatus == 'COMPLETED'){
		let activeIndicator = td[2].querySelector('div.spinner-border');
		if(activeIndicator){
			activeIndicator.remove();
		}
	}
	
};

function renderTask(taskEvent, targetNode, idPrefix){	
	var tr = document.querySelector('#tableRowTemplate');
	// Populate the src at runtime.
	if(idPrefix){
		tr.content.querySelector('tr').id = idPrefix + '-' + taskEvent.taskId;
	} else {
		tr.content.querySelector('tr').id = taskEvent.taskId;
	}
	
	td = tr.content.querySelectorAll("td");
  	td[0].textContent = '' + taskEvent.taskId;
  	td[1].textContent = '' + taskEvent.taskStatus;
  	if(taskEvent.payload){
  		td[2].querySelectorAll('span')[0].textContent = '' + taskEvent.payload;
  	} else {
		td[2].querySelectorAll('span')[0].textContent = '';
	}
	var clone = document.importNode(tr.content, true);	
	targetNode.insertBefore(clone, targetNode.firstChild);		
};

function getCookie(name) {
    var v = document.cookie.match('(^|;) ?' + name + '=([^;]*)(;|$)');
    return v ? v[2] : null;
};

function createNewTask(){
	showTaskDialog(true);
	newTaskApiCall();
};

function showTaskDialog(flag){
	if(flag){
		document.myTaskModal.show();
	} else {
		document.myTaskModal.hide();
	}
};

function newTaskApiCall(){
	let sseid = getCookie('SSE_ID');
	fetch('/api/tasks?sseid=' + sseid, {
  		method: 'POST'
	}).then(response => response.json())
  	  .then(data => updateModalDialogUI(data));
};

function completeTaskApiCall(taskId){
	if(taskId){
		let sseid = getCookie('SSE_ID');
		fetch('/api/tasks/' + taskId + '?sseid=' + sseid, {
	  		method: 'PUT'
		});
	}
};

function updateModalDialogUI(taskEvent){
	var tbodyRef = document.querySelector('#taskModalDialogBody');
	while (tbodyRef.firstChild) {
        tbodyRef.removeChild(tbodyRef.firstChild);
    }
    
    var table = document.createElement('table');
    tbodyRef.appendChild(table);
	renderTask(taskEvent, table, 'md');	
	if(taskEvent.taskStatus != 'COMPLETED'){
		setTaskActiveControlls(true);	
	} else {
		setTaskActiveControlls(false);
	}
};

function setTaskActiveControlls(active){	
	var activeElements = document.myTaskModal._element.querySelectorAll('.active-task-element'), i;
	var completedElements = document.myTaskModal._element.querySelectorAll('.completed-task-element');
	if(active){		
		for (i = 0; i < activeElements.length; ++i) {
			activeElements[i].classList.remove('d-none');
		}		
		for (i = 0; i < completedElements.length; ++i) {
			completedElements[i].classList.add('d-none');
		}
	} else {		
		for (i = 0; i < activeElements.length; ++i) {
			activeElements[i].classList.add('d-none');
		}		
		for (i = 0; i < completedElements.length; ++i) {
			completedElements[i].classList.remove('d-none');
		}
	}		
	
}

document.addEventListener("DOMContentLoaded", function() {
	
	let modalDomElement = document.getElementById("taskModalDialog");
	
  	document.myTaskModal = new bootstrap.Modal(modalDomElement, {
		backdrop: 'static',
		keyboard: false
	});	
  	
  	document.getElementById("newTaskButton").addEventListener("click", function() {
 		createNewTask();
	});		
	
	document.getElementById("completeTaskTextInput").addEventListener('keyup', function (event) {
    	if (event.keyCode === 13) {
			let keyId = event.target.value;
			event.target.value = '';
           	completeTaskApiCall(keyId);
        }
    });
    
	document.getElementById("completeTaskButton").addEventListener("click", function() {
		let inputElement = document.getElementById("completeTaskTextInput");
 		let keyId = inputElement.value;
 		inputElement.value = '';
 		completeTaskApiCall(keyId);
	});		
		
});

