import React, { createContext, useState, useEffect } from 'react';
import database from '../database/database';

export const AuthContext = createContext();
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
    const checkLoggedInUser = async () => {
      try {
        setIsLoading(false);
      } catch (error) {
        console.error(error);
        setIsLoading(false);
      }
    };
    checkLoggedInUser();
  }, []);

  const login = async (email, password) => {
    try {
      const user = await database.getUserByEmail(email);
      if (!user || user.password !== password) {
        throw new Error('Invalid credentials');
      }
      setUser(user);
      return user;
    } catch (error) {
      throw error;
    }
  };

  const signup = async (userData) => {
    try {
      const newUser = {
        ...userData,
        id: Math.random().toString(36).substring(7),
      };
      await database.addUser(newUser);
      setUser(newUser);
      return newUser;
    } catch (error) {
      throw error;
    }
  };

  const logout = () => {
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        login,
        signup,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};