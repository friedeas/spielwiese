package com.bosch.btmpt.SSEDemo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;
import org.springframework.web.util.WebUtils;

@Controller
public class SseTaskController {

	private ExecutorService nonBlockingService = Executors.newCachedThreadPool();

	private Map<UUID, UserTasks> userTasksMap = new HashMap<>();

	private static final String SSE_COOKIE_NAME = "SSE_ID";

	final static Logger LOG = LoggerFactory.getLogger(SseTaskController.class);

	@PostMapping("/api/tasks")
	public ResponseEntity<TaskEvent> createTask(@RequestParam(required = true) String sseid,
			final HttpServletRequest request) {
		LOG.info("Create new task for SSId {}", sseid);
		final UserTasks userTasks = this.userTasksMap.get(UUID.fromString(sseid));
		if (userTasks != null) {
			final SimpleTask task = new SimpleTask(userTasks.getEmitter());
			nonBlockingService.submit(task);
			userTasks.getTaskMap().put(task.getTaskId(), task);
			LOG.trace("Task created : " + task.getTaskId());
			return ResponseEntity.status(HttpStatus.CREATED).body(task.createEvent());
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@GetMapping(path = "/api/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getTasks(@RequestParam(required = true) String sseid,
			final HttpServletRequest request) {
		LOG.info("Get tasks for SSId {}", sseid);
		final UserTasks userTasks = this.userTasksMap.get(UUID.fromString(sseid));
		if (userTasks != null) {
			return ResponseEntity.status(HttpStatus.OK).body(userTasks.getTaskMap().values());
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User task id " + sseid + " not found");
		}
	}

	@PutMapping("/api/tasks/{taskid}")
	public ResponseEntity<TaskEvent> completeTask(@PathVariable(name = "taskid") String id,
			@RequestParam(required = true) String sseid, final HttpServletRequest request) {
		LOG.info("Complete task {} for {}", id, sseid);
		final UserTasks userTasks = this.userTasksMap.get(UUID.fromString(sseid));
		if (userTasks != null) {
			final UUID taskID = UUID.fromString(id);
			final Map<UUID, SimpleTask> taskMap = userTasks.getTaskMap();
			if (taskMap.containsKey(taskID)) {
				final SimpleTask task = taskMap.get(taskID); 
				task.setCompleted();
				return ResponseEntity.status(HttpStatus.OK).body(task.createEvent());
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@GetMapping(path = "/api/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter listen(final HttpServletRequest request)
			throws IOException {
		Cookie sseCookie = WebUtils.getCookie(request, SSE_COOKIE_NAME);
		if (sseCookie != null) {			
			final String sseid = sseCookie.getValue();
		
			LOG.info("Listen for {}", sseid);
			final UserTasks userTasks = this.userTasksMap.get(UUID.fromString(sseid));
			if (userTasks != null) {
				SseEventBuilder event = SseEmitter.event().data("Connected").id(UUID.randomUUID().toString())
						.name("ConnectionEvent");
				final SseEmitter emitter = userTasks.getEmitter();
				emitter.send(event);
				return emitter;
			} else {
				return createErrorEmitter("No Emitter found for SSEId " + sseid);
			}
		} else {
			LOG.error("No SSEId cookie set or no Emitter foind for the SSEId!");
			return createErrorEmitter("No SSEId cookie set!");
		}
	}
	
	private SseEmitter createErrorEmitter(final String errorMsg) throws IOException {
		LOG.error(errorMsg);
		SseEmitter emitter = new SseEmitter();
		emitter.send(SseEmitter.event().data("401: " + errorMsg)
				.id(UUID.randomUUID().toString()).name("TaskEvent"));
		emitter.complete();
		return emitter;
	}

	@GetMapping("/")
	public String showIndexPage(Model model, final HttpServletRequest request, final HttpServletResponse response) {
		LOG.info("Show index Page");
		Cookie sseCookie = WebUtils.getCookie(request, SSE_COOKIE_NAME);
		UUID userTasksIdentifier;
		
		if (sseCookie == null) {			
			sseCookie = this.newCookieAndUserTaskObject();
			userTasksIdentifier = UUID.fromString(sseCookie.getValue());
		} else {
			userTasksIdentifier = UUID.fromString(sseCookie.getValue());
			UserTasks userTasksObject = this.userTasksMap.get(userTasksIdentifier);
			if(userTasksObject == null) {
				sseCookie = this.newCookieAndUserTaskObject();
				userTasksIdentifier = UUID.fromString(sseCookie.getValue());
			}
		}
		response.addCookie(sseCookie);		
		model.addAttribute("tasklist", this.userTasksMap.get(userTasksIdentifier).getTaskMap().values());
		return "home";
	}

	private Cookie newCookieAndUserTaskObject() {
		UUID userTasksIdentifier = UUID.randomUUID();
		UserTasks userTasksObject = new UserTasks();
		this.userTasksMap.put(userTasksIdentifier, userTasksObject);
		Cookie sseCookie = new Cookie(SSE_COOKIE_NAME, userTasksIdentifier.toString());
		sseCookie.setHttpOnly(false);
		return sseCookie;
	}
}
