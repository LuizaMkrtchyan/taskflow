
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  id: number;
  name: string;
  role: string;
  email: string;
  token: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface UserResponse {
  id: number;
  name: string;
  email: string;
  role: string;
}

export interface CreateProjectRequest {
  name: string;
  description: string;
}

export interface ProjectResponse {
  id: number;
  name: string;
  description: string;
  createdBy: number;
}
export type TaskStatus =
  | "TODO"
  | "IN_PROGRESS"
  | "DONE";

export type TaskPriority =
  | "LOW"
  | "MEDIUM"
  | "HIGH";

export interface TaskResponse {
  id: number;
  projectId: number;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  assigneeId: number | null;
  dueDate: string | null;
}

export interface CreateTaskRequest {
  title: string;
  description: string;
  priority: TaskPriority;
  assigneeId: number | null;
  dueDate: string | null;
}

export interface UpdateTaskRequest {
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  assigneeId: number | null;
  dueDate: string | null;
}

export interface CommentResponse {
  id: number;
  taskId: number;
  content: string;
  authorId: number;
  createdAt: string;
}

export interface CreateCommentRequest {
  content: string;
}
