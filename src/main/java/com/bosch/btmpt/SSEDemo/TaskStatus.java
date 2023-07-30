package com.bosch.btmpt.SSEDemo;

public enum TaskStatus {
	
	CREATED(0),
	ACTIVE(1), 
	COMPLETED(99);
	
	private int value;
	
	private TaskStatus(int i) {
		this.value = i;
	}
	
	public int getValue() {
        return value;
    }
	
}


