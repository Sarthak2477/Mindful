package com.android.mindful.model;

public class Task {

    private String taskText;
    private boolean completed;

    private long createdAt;

    public Task(String taskText, boolean completed, long createdAt) {
        this.taskText = taskText;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public String getTaskText() {
        return taskText;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
