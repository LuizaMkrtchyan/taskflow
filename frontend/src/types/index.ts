
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
