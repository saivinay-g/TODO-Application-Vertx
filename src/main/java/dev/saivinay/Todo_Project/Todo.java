package dev.saivinay.Todo_Project;

import java.util.Objects;

public class Todo {
  private String id;
  private String title;
  private boolean completed;

  public Todo(String id, String title, boolean completed) {
    this.id = id;
    this.title = title;
    this.completed = completed;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Todo todo = (Todo) o;
    return completed == todo.completed && Objects.equals(id, todo.id) && Objects.equals(title, todo.title);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, completed);
  }

  @Override
  public String toString() {
    return "Todo{" +
      "id='" + id + '\'' +
      ", title='" + title + '\'' +
      ", completed=" + completed +
      '}';
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }
}

