import React from "react";

interface AuthFormProps {
  title: string;
  children: React.ReactNode;
  onSubmit: (e: React.FormEvent) => void;
  footer?: React.ReactNode;
}

const AuthForm = ({ title, children, onSubmit, footer }: AuthFormProps) => {
  return (
    <div className="auth-container">
      <h2>{title}</h2>
      <form onSubmit={onSubmit} className="auth-form">
        {children}
      </form>
      {footer && <div className="auth-footer">{footer}</div>}
    </div>
  );
};

export default AuthForm;