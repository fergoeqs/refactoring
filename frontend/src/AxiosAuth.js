import { useMemo } from 'react';
import { jwtDecode } from 'jwt-decode';
import { useAuth } from './AuthProvider';
import axios from "axios";

const useAxiosWithAuth = () => {
    const { token, logout } = useAuth();

    return useMemo(() => {
        const instance = axios.create({
            baseURL: process.env.REACT_APP_API_URL || "/api",
        });

        instance.interceptors.request.use(
            (config) => {
                const authToken = token || localStorage.getItem("token");

                if (!authToken) {
                    console.warn("No token found, redirecting to login...");
                    logout();
                    window.location.href = '/login';
                    return Promise.reject(new Error("Token отсутствует"));
                }

                try {
                    const decoded = jwtDecode(authToken);
                    const currentTime = Date.now() / 1000;

                    if (decoded.exp < currentTime) {
                        console.warn("Token expired, logging out...");
                        logout();
                        window.location.href = '/login';
                        return Promise.reject(new Error("Token expired"));
                    }

                    config.headers['Authorization'] = `Bearer ${authToken}`;

                } catch (error) {
                    console.error("Error decoding token:", error);
                    logout();
                    window.location.href = '/login';
                    return Promise.reject(error);
                }

                return config;
            },
            (error) => Promise.reject(error)
        );

        return instance;
    }, [token, logout]);
};

export default useAxiosWithAuth;
