package com.bosch.btmpt.SSEDemo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class UserTasks {

	private Map<UUID, SimpleTask> taskMap = new HashMap<>();

	private SseEmitter emitter = new SseEmitter(-1L);		

	public Map<UUID, SimpleTask> getTaskMap() {
		return taskMap;
	}

	public void setTaskMap(Map<UUID, SimpleTask> taskMap) {
		this.taskMap = taskMap;
	}

	public SseEmitter getEmitter() {
		return emitter;
	}	
	
}
