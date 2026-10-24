
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import type {
  CreateProjectRequest,
  ProjectResponse,
} from "../types";

export default function Projects() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);

  const loadProjects = async () => {
    try {
      const response = await api.get<ProjectResponse[]>(
        "/projects"
      );

      setProjects(response.data);
      setError("");
    } catch {
      setError("Could not load projects.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProjects();
  }, []);

  const handleCreateProject = async (
    event: React.FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setError("");
    setCreating(true);

    const request: CreateProjectRequest = {
      name,
      description,
    };

    try {
      const response = await api.post<ProjectResponse>(
        "/projects",
        request
      );

      setProjects((currentProjects) => [
        ...currentProjects,
        response.data,
      ]);

      setName("");
      setDescription("");
    } catch {
      setError("Could not create project.");
    } finally {
      setCreating(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div>
      <header>
        <h1>TaskFlow</h1>

        <div>
          <span>
            Welcome, {user?.name ?? "User"}
          </span>

          <button onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <h2>Projects</h2>

      <form onSubmit={handleCreateProject}>
        <h3>Create project</h3>

        <div>
          <label htmlFor="project-name">Project name</label>

          <input
            id="project-name"
            value={name}
            onChange={(event) => setName(event.target.value)}
            required
          />
        </div>

        <div>
          <label htmlFor="project-description">
            Description
          </label>

          <textarea
            id="project-description"
            value={description}
            onChange={(event) =>
              setDescription(event.target.value)
            }
          />
        </div>

        <button type="submit" disabled={creating}>
          {creating ? "Creating..." : "Create project"}
        </button>
      </form>

      {error && <p>{error}</p>}

      <section>
        <h3>Your projects</h3>

        {loading ? (
          <p>Loading projects...</p>
        ) : projects.length === 0 ? (
          <p>No projects yet. Create your first project!</p>
        ) : (
          projects.map((project) => (
            <article key={project.id}>
              <h4>{project.name}</h4>
              <p>{project.description}</p>
            </article>
          ))
        )}
      </section>
    </div>
  );
}
