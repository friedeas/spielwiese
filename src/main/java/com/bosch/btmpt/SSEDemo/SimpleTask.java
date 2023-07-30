package com.bosch.btmpt.SSEDemo;

import java.io.IOException;
import java.time.LocalTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

public class SimpleTask implements Runnable {

	final static Logger LOG = LoggerFactory.getLogger(SimpleTask.class);

	private final SseEmitter emitter;

	private final UUID taskId;	

	private boolean completed = false;
	
	private TaskStatus taskStatus;
	
	private Object payload;

	public SimpleTask(final SseEmitter sseEmitter) {
		this.emitter = sseEmitter;
		this.taskId = UUID.randomUUID();
		this.taskStatus = TaskStatus.CREATED;
		sendEvent();
	}

	@Override
	public void run() {
		this.taskStatus = TaskStatus.ACTIVE;
		sendEvent();
		while (!completed) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				emitter.completeWithError(e);
			}
		}
		this.setPayload(LocalTime.now().toString());
		sendEvent();
	}

	private void sendEvent() {
		try {
			SseEventBuilder event = SseEmitter.event().data(createEvent()).id(taskId.toString())
					.name("TaskEvent");
			emitter.send(event); 
		} catch (IOException e) {
			LOG.error("Failed to send event");
			emitter.completeWithError(e);
		}
	}

	public TaskEvent createEvent() {
		TaskEvent taskEvent = new TaskEvent();
		taskEvent.setTaskStatus(this.taskStatus);
		taskEvent.setTaskId(this.getTaskId().toString());
		if(TaskStatus.COMPLETED.equals(this.taskStatus)) {
			taskEvent.setPayload(this.getPayload());
		}
		return taskEvent;
	}

	public UUID getTaskId() {
		return taskId;
	}

	public boolean isCompleted() {
		return completed;
	}

	public synchronized void setCompleted() {		
		this.completed = true;
		this.taskStatus = TaskStatus.COMPLETED;
	}

	public TaskStatus getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(TaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}
	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	};
}
