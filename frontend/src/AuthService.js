import axios from 'axios';

export const loginUser = async (username, password, setToken) => {
    try {
        const apiUrl = process.env.REACT_APP_API_URL || "/api";
        const response = await axios.post(`${apiUrl}/users/login`, { username, password }, { withCredentials: true });
        const token = response.data.token;
        setToken(token);
        console.log("Token received from server:", token);
        return token;
    } catch (error) {
        console.error("Login error:", error);
        throw error;
    }
};