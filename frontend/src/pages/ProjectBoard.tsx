
import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import api from "../api/axios";
import type {
  CommentResponse,
  CreateCommentRequest,
  CreateTaskRequest,
  TaskPriority,
  TaskResponse,
  TaskStatus,
  UpdateTaskRequest,
  UserResponse,
} from "../types";

const columns: { status: TaskStatus; title: string }[] = [
  { status: "TODO", title: "To do" },
  { status: "IN_PROGRESS", title: "In progress" },
  { status: "DONE", title: "Done" },
];

interface TaskFormData {
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  assigneeId: string;
  dueDate: string;
}

function getInitials(name: string) {
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
}

function formatDate(date: string) {
  return new Date(`${date}T00:00:00`).toLocaleDateString(undefined, {
    day: "numeric",
    month: "short",
    year: "numeric",
  });
}

function formatDateTime(date: string) {
  const parsed = new Date(date);
  return Number.isNaN(parsed.getTime())
    ? date
    : parsed.toLocaleString(undefined, {
        dateStyle: "medium",
        timeStyle: "short",
      });
}

export default function ProjectBoard() {
  const { projectId } = useParams<{ projectId: string }>();
  const id = Number(projectId);
  const { user } = useAuth();

  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [users, setUsers] = useState<UserResponse[]>([]);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<TaskPriority>("MEDIUM");
  const [assigneeId, setAssigneeId] = useState("");
  const [dueDate, setDueDate] = useState("");

  const [loading, setLoading] = useState(true);
  const [usersLoading, setUsersLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [deletingTaskId, setDeletingTaskId] = useState<number | null>(null);
  const [editingTaskId, setEditingTaskId] = useState<number | null>(null);
  const [savingTask, setSavingTask] = useState(false);
  const [editForm, setEditForm] = useState<TaskFormData | null>(null);
  const [error, setError] = useState("");

  const [openCommentsTaskId, setOpenCommentsTaskId] = useState<number | null>(
    null
  );
  const [commentsByTask, setCommentsByTask] = useState<
    Record<number, CommentResponse[]>
  >({});
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [commentDraft, setCommentDraft] = useState("");
  const [postingComment, setPostingComment] = useState(false);

  const loadTasks = useCallback(async () => {
    if (!Number.isInteger(id) || id <= 0) {
      setError("Invalid project ID.");
      setLoading(false);
      return;
    }

    try {
      const response = await api.get<TaskResponse[]>(`/projects/${id}/tasks`);
      setTasks(response.data);
      setError("");
    } catch {
      setError("Could not load tasks for this project.");
    } finally {
      setLoading(false);
    }
  }, [id]);

  const loadUsers = useCallback(async () => {
    try {
      const response = await api.get<UserResponse[]>("/users");
      setUsers(response.data);
    } catch (error) {
      console.error("Could not load users:", error);
      setError("Could not load users for task assignment.");
    } finally {
      setUsersLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadTasks();
    void loadUsers();
  }, [loadTasks, loadUsers]);

  const getUserName = (userId: number | null) => {
    if (userId === null) return "Unassigned";
    return users.find((item) => item.id === userId)?.name ?? `User #${userId}`;
  };

  const handleCreateTask = async (
    event: React.FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (!user) {
      setError("Please log in before creating a task.");
      return;
    }

    if (!assigneeId) {
      setError("Please select an assignee.");
      return;
    }

    const parsedAssigneeId = Number(assigneeId);
    if (!Number.isInteger(parsedAssigneeId) || parsedAssigneeId <= 0) {
      setError("Please select a valid assignee.");
      return;
    }

    setError("");
    setCreating(true);

    const request: CreateTaskRequest = {
      title: title.trim(),
      description: description.trim(),
      priority,
      assigneeId: parsedAssigneeId,
      dueDate: dueDate || null,
    };

    try {
      const response = await api.post<TaskResponse>(
        `/projects/${id}/tasks`,
        request
      );

      setTasks((current) => [...current, response.data]);
      setTitle("");
      setDescription("");
      setPriority("MEDIUM");
      setAssigneeId("");
      setDueDate("");
    } catch (error) {
      console.error("Task creation failed:", error);
      setError("Could not create the task. Check the task details and try again.");
    } finally {
      setCreating(false);
    }
  };

  const handleStatusChange = async (
    task: TaskResponse,
    newStatus: TaskStatus
  ) => {
    if (task.status === newStatus) return;

    if (task.assigneeId === null) {
      setError(`Please assign "${task.title}" before changing its status.`);
      return;
    }

    setError("");

    const request: UpdateTaskRequest = {
      title: task.title,
      description: task.description ?? "",
      status: newStatus,
      priority: task.priority,
      assigneeId: task.assigneeId,
      dueDate: task.dueDate,
    };

    try {
      const response = await api.put<TaskResponse>(
        `/tasks/${task.id}`,
        request
      );

      setTasks((current) =>
        current.map((item) => (item.id === task.id ? response.data : item))
      );
    } catch (error) {
      console.error("Task status update failed:", error);
      setError(`Could not update "${task.title}".`);
    }
  };

  const handleEditClick = (task: TaskResponse) => {
    setError("");
    setEditingTaskId(task.id);
    setEditForm({
      title: task.title,
      description: task.description ?? "",
      status: task.status,
      priority: task.priority,
      assigneeId: task.assigneeId == null ? "" : String(task.assigneeId),
      dueDate: task.dueDate ?? "",
    });
  };

  const handleEditCancel = () => {
    setEditingTaskId(null);
    setEditForm(null);
  };

  const updateEditField = <K extends keyof TaskFormData>(
    field: K,
    value: TaskFormData[K]
  ) => {
    setEditForm((current) =>
      current ? { ...current, [field]: value } : current
    );
  };

  const handleEditSave = async (
    event: React.FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (editingTaskId === null || !editForm) return;

    const task = tasks.find((item) => item.id === editingTaskId);
    if (!task) return;

    const parsedAssigneeId = Number(editForm.assigneeId);

    if (!editForm.assigneeId || !Number.isInteger(parsedAssigneeId) || parsedAssigneeId <= 0) {
      setError("Please select a valid assignee.");
      return;
    }

    const request: UpdateTaskRequest = {
      title: editForm.title.trim(),
      description: editForm.description.trim(),
      status: editForm.status,
      priority: editForm.priority,
      assigneeId: parsedAssigneeId,
      dueDate: editForm.dueDate || null,
    };

    setError("");
    setSavingTask(true);

    try {
      const response = await api.put<TaskResponse>(
        `/tasks/${editingTaskId}`,
        request
      );

      setTasks((current) =>
        current.map((item) =>
          item.id === editingTaskId ? response.data : item
        )
      );

      setEditingTaskId(null);
      setEditForm(null);
    } catch (error) {
      console.error("Task update failed:", error);
      setError(`Could not update "${task.title}". Check the task details.`);
    } finally {
      setSavingTask(false);
    }
  };

  const handleDeleteTask = async (task: TaskResponse) => {
    const confirmed = window.confirm(
      `Are you sure you want to delete "${task.title}"? This action cannot be undone.`
    );

    if (!confirmed) return;

    setError("");
    setDeletingTaskId(task.id);

    try {
      await api.delete(`/tasks/${task.id}`);

      setTasks((current) =>
        current.filter((item) => item.id !== task.id)
      );

      if (editingTaskId === task.id) {
        setEditingTaskId(null);
        setEditForm(null);
      }

      if (openCommentsTaskId === task.id) {
        setOpenCommentsTaskId(null);
      }
    } catch (error) {
      console.error("Task deletion failed:", error);
      setError(`Could not delete "${task.title}".`);
    } finally {
      setDeletingTaskId(null);
    }
  };

  const handleToggleComments = async (taskId: number) => {
    if (openCommentsTaskId === taskId) {
      setOpenCommentsTaskId(null);
      setCommentDraft("");
      return;
    }

    setOpenCommentsTaskId(taskId);
    setCommentDraft("");
    setCommentsLoading(true);
    setError("");

    try {
      const response = await api.get<CommentResponse[]>(
        `/tasks/${taskId}/comments`
      );

      setCommentsByTask((current) => ({
        ...current,
        [taskId]: response.data,
      }));
    } catch (error) {
      console.error("Could not load comments:", error);
      setError("Could not load comments for this task.");
    } finally {
      setCommentsLoading(false);
    }
  };

  const handleAddComment = async (
    event: React.FormEvent<HTMLFormElement>,
    taskId: number
  ) => {
    event.preventDefault();

    const content = commentDraft.trim();
    if (!content) return;

    const request: CreateCommentRequest = { content };

    setPostingComment(true);
    setError("");

    try {
      const response = await api.post<CommentResponse>(
        `/tasks/${taskId}/comments`,
        request
      );

      setCommentsByTask((current) => ({
        ...current,
        [taskId]: [...(current[taskId] ?? []), response.data],
      }));

      setCommentDraft("");
    } catch (error) {
      console.error("Could not post comment:", error);
      setError("Could not add your comment. Please try again.");
    } finally {
      setPostingComment(false);
    }
  };

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const todayString = [
    today.getFullYear(),
    String(today.getMonth() + 1).padStart(2, "0"),
    String(today.getDate()).padStart(2, "0"),
  ].join("-");

  return (
    <main className="board-page">
      <header className="board-header">
        <Link className="back-link" to="/projects">
          ← Back to projects
        </Link>
        <h1>Project Kanban Board</h1>
        <p>Plan work, track progress, and keep your team in sync.</p>
      </header>

      <section className="task-form-section">
        <h2>Create a task</h2>

        <form className="create-task-form" onSubmit={handleCreateTask}>
          <div className="form-field">
            <label htmlFor="task-title">Title</label>
            <input
              id="task-title"
              value={title}
              onChange={(event) => setTitle(event.target.value)}
              placeholder="e.g. Implement login"
              required
              maxLength={200}
            />
          </div>

          <div className="form-field">
            <label htmlFor="task-description">Description</label>
            <textarea
              id="task-description"
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              placeholder="What needs to be done?"
            />
          </div>

          <div className="form-field">
            <label htmlFor="task-priority">Priority</label>
            <select
              id="task-priority"
              value={priority}
              onChange={(event) =>
                setPriority(event.target.value as TaskPriority)
              }
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>
          </div>

          <div className="form-field">
            <label htmlFor="task-assignee">Assignee</label>
            <select
              id="task-assignee"
              value={assigneeId}
              onChange={(event) => setAssigneeId(event.target.value)}
              required
              disabled={usersLoading}
            >
              <option value="">
                {usersLoading ? "Loading users..." : "Select a user"}
              </option>
              {users.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-field">
            <label htmlFor="task-due-date">Due date (optional)</label>
            <input
              id="task-due-date"
              type="date"
              value={dueDate}
              onChange={(event) => setDueDate(event.target.value)}
            />
          </div>

          <button
            className="primary-button"
            type="submit"
            disabled={creating || usersLoading || users.length === 0}
          >
            {creating ? "Creating..." : "＋ Add task"}
          </button>
        </form>
      </section>

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      {loading ? (
        <p className="board-loading">Loading tasks...</p>
      ) : (
        <section className="kanban-board">
          {columns.map((column) => {
            const columnTasks = tasks.filter(
              (task) => task.status === column.status
            );

            return (
              <section className="kanban-column" key={column.status}>
                <header className="kanban-column-header">
                  <h2>{column.title}</h2>
                  <span className="task-count">{columnTasks.length}</span>
                </header>

                {columnTasks.length === 0 ? (
                  <p className="empty-column">No tasks yet</p>
                ) : (
                  <div className="task-list">
                    {columnTasks.map((task) => {
                      const isOverdue =
                        task.dueDate !== null &&
                        task.dueDate < todayString &&
                        task.status !== "DONE";

                      return (
                        <article className="task-card" key={task.id}>
                          {editingTaskId === task.id && editForm ? (
                            <form
                              className="edit-task-form"
                              onSubmit={handleEditSave}
                            >
                              <h3>Edit task</h3>

                              <label htmlFor={`edit-title-${task.id}`}>
                                Title
                              </label>
                              <input
                                id={`edit-title-${task.id}`}
                                value={editForm.title}
                                onChange={(event) =>
                                  updateEditField("title", event.target.value)
                                }
                                required
                                maxLength={200}
                              />

                              <label htmlFor={`edit-description-${task.id}`}>
                                Description
                              </label>
                              <textarea
                                id={`edit-description-${task.id}`}
                                value={editForm.description}
                                onChange={(event) =>
                                  updateEditField(
                                    "description",
                                    event.target.value
                                  )
                                }
                              />

                              <label htmlFor={`edit-status-${task.id}`}>
                                Status
                              </label>
                              <select
                                id={`edit-status-${task.id}`}
                                value={editForm.status}
                                onChange={(event) =>
                                  updateEditField(
                                    "status",
                                    event.target.value as TaskStatus
                                  )
                                }
                              >
                                <option value="TODO">To do</option>
                                <option value="IN_PROGRESS">In progress</option>
                                <option value="DONE">Done</option>
                              </select>

                              <label htmlFor={`edit-priority-${task.id}`}>
                                Priority
                              </label>
                              <select
                                id={`edit-priority-${task.id}`}
                                value={editForm.priority}
                                onChange={(event) =>
                                  updateEditField(
                                    "priority",
                                    event.target.value as TaskPriority
                                  )
                                }
                              >
                                <option value="LOW">Low</option>
                                <option value="MEDIUM">Medium</option>
                                <option value="HIGH">High</option>
                              </select>

                              <label htmlFor={`edit-assignee-${task.id}`}>
                                Assignee
                              </label>
                              <select
                                id={`edit-assignee-${task.id}`}
                                value={editForm.assigneeId}
                                onChange={(event) =>
                                  updateEditField(
                                    "assigneeId",
                                    event.target.value
                                  )
                                }
                                required
                                disabled={usersLoading}
                              >
                                <option value="">Select a user</option>
                                {users.map((item) => (
                                  <option key={item.id} value={item.id}>
                                    {item.name}
                                  </option>
                                ))}
                              </select>

                              <label htmlFor={`edit-due-date-${task.id}`}>
                                Due date
                              </label>
                              <input
                                id={`edit-due-date-${task.id}`}
                                type="date"
                                value={editForm.dueDate}
                                onChange={(event) =>
                                  updateEditField(
                                    "dueDate",
                                    event.target.value
                                  )
                                }
                              />

                              <div className="task-action-buttons">
                                <button
                                  type="submit"
                                  className="edit-task-button"
                                  disabled={savingTask}
                                >
                                  {savingTask ? "Saving..." : "Save changes"}
                                </button>
                                <button
                                  type="button"
                                  className="cancel-edit-button"
                                  onClick={handleEditCancel}
                                  disabled={savingTask}
                                >
                                  Cancel
                                </button>
                              </div>
                            </form>
                          ) : (
                            <>
                              <div className="task-card-top">
                                <span
                                  className={`priority priority-${task.priority.toLowerCase()}`}
                                >
                                  {task.priority}
                                </span>
                                <span className="task-number">#{task.id}</span>
                              </div>

                              <h3>{task.title}</h3>
                              <p className="task-description">
                                {task.description || "No description provided."}
                              </p>

                              <div className="task-metadata">
                                <div className="assignee-info">
                                  <span className="assignee-avatar">
                                    {getInitials(getUserName(task.assigneeId))}
                                  </span>
                                  <span>{getUserName(task.assigneeId)}</span>
                                </div>

                                {task.dueDate && (
                                  <span
                                    className={`due-date ${
                                      isOverdue ? "due-date-overdue" : ""
                                    }`}
                                  >
                                    <span aria-hidden="true">▦</span>{" "}
                                    {isOverdue ? "Overdue · " : ""}
                                    {formatDate(task.dueDate)}
                                  </span>
                                )}
                              </div>

                              <div className="status-field">
                                <label htmlFor={`status-${task.id}`}>
                                  Status
                                </label>
                                <select
                                  id={`status-${task.id}`}
                                  value={task.status}
                                  onChange={(event) =>
                                    void handleStatusChange(
                                      task,
                                      event.target.value as TaskStatus
                                    )
                                  }
                                >
                                  <option value="TODO">To do</option>
                                  <option value="IN_PROGRESS">
                                    In progress
                                  </option>
                                  <option value="DONE">Done</option>
                                </select>
                              </div>

                              <div className="task-action-buttons">
                                <button
                                  type="button"
                                  className="edit-task-button"
                                  onClick={() => handleEditClick(task)}
                                  disabled={deletingTaskId === task.id}
                                >
                                  Edit
                                </button>
                                <button
                                  type="button"
                                  className="delete-task-button"
                                  disabled={deletingTaskId === task.id}
                                  onClick={() => void handleDeleteTask(task)}
                                >
                                  {deletingTaskId === task.id
                                    ? "Deleting..."
                                    : "Delete"}
                                </button>
                                <button
                                  type="button"
                                  className="comments-toggle-button"
                                  onClick={() =>
                                    void handleToggleComments(task.id)
                                  }
                                >
                                  {openCommentsTaskId === task.id
                                    ? "Hide comments"
                                    : `Comments (${commentsByTask[task.id]?.length ?? "…" })`}
                                </button>
                              </div>

                              {openCommentsTaskId === task.id && (
                                <section className="task-comments">
                                  <h4>Comments</h4>

                                  {commentsLoading ? (
                                    <p>Loading comments...</p>
                                  ) : (commentsByTask[task.id] ?? []).length ===
                                    0 ? (
                                    <p className="no-comments">
                                      No comments yet. Start the conversation.
                                    </p>
                                  ) : (
                                    <div className="comment-list">
                                      {(commentsByTask[task.id] ?? []).map(
                                        (comment) => (
                                          <article
                                            className="comment-item"
                                            key={comment.id}
                                          >
                                            <div className="comment-header">
                                              <strong>
                                                {getUserName(comment.authorId)}
                                              </strong>
                                              <time>
                                                {formatDateTime(
                                                  comment.createdAt
                                                )}
                                              </time>
                                            </div>
                                            <p>{comment.content}</p>
                                          </article>
                                        )
                                      )}
                                    </div>
                                  )}

                                  <form
                                    className="comment-form"
                                    onSubmit={(event) =>
                                      void handleAddComment(event, task.id)
                                    }
                                  >
                                    <label htmlFor={`comment-${task.id}`}>
                                      Add a comment
                                    </label>
                                    <textarea
                                      id={`comment-${task.id}`}
                                      value={commentDraft}
                                      onChange={(event) =>
                                        setCommentDraft(event.target.value)
                                      }
                                      placeholder="Write a comment..."
                                      maxLength={2000}
                                      required
                                    />
                                    <button
                                      type="submit"
                                      className="primary-button"
                                      disabled={
                                        postingComment ||
                                        !commentDraft.trim()
                                      }
                                    >
                                      {postingComment
                                        ? "Posting..."
                                        : "Post comment"}
                                    </button>
                                  </form>
                                </section>
                              )}
                            </>
                          )}
                        </article>
                      );
                    })}
                  </div>
                )}
              </section>
            );
          })}
        </section>
      )}
    </main>
  );
}
