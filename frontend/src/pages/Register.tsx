
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import api from "../api/axios";
import type { RegisterRequest, UserResponse } from "../types";

export default function Register() {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (
    event: React.FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setError("");
    setLoading(true);

    const request: RegisterRequest = {
      name,
      email,
      password,
    };

    try {
      await api.post<UserResponse>(
        "/users/register",
        request
      );

      navigate("/login");
    } catch {
      console.error("Registration error:", error);

        const message =
          error.response?.data?.message ||
          error.response?.data ||
          error.message ||
          "Registration failed.";

        setError(
          typeof message === "string"
            ? message
            : JSON.stringify(message)
        );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>TaskFlow</h1>
      <h2>Create account</h2>

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="name">Name</label>

          <input
            id="name"
            type="text"
            value={name}
            onChange={(event) => setName(event.target.value)}
            required
          />
        </div>

        <div>
          <label htmlFor="email">Email</label>

          <input
            id="email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </div>

        <div>
          <label htmlFor="password">Password</label>

          <input
            id="password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            minLength={6}
            required
          />
        </div>

        {error && <p>{error}</p>}

        <button type="submit" disabled={loading}>
          {loading ? "Creating account..." : "Register"}
        </button>
      </form>

      <p>
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </div>
  );
}
