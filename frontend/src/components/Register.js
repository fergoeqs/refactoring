import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthProvider';
import '../auth.css';

const Register = () => {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        phoneNumber: '',
        name: '',
        surname: '',
    });
    const [error, setError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [step, setStep] = useState(1);
    const [errors, setErrors] = useState({});
    const navigate = useNavigate();
    const { setToken } = useAuth();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
        setErrors({ ...errors, [e.target.name]: '' });
    };

    const validateFields = () => {
        const newErrors = {};

        if (step === 1) {
            if (!formData.username) newErrors.username = 'Username is required';
            if (!formData.email) {
                newErrors.email = 'Email is required';
            } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
                newErrors.email = 'Invalid email format';
            }
            if (!formData.password) newErrors.password = 'Password is required';
        } else if (step === 2) {
            if (!formData.phoneNumber) {
                newErrors.phoneNumber = 'Phone Number is required';
            } else if (!/^\+?[0-9]{10,15}$/.test(formData.phoneNumber)) {
                newErrors.phoneNumber = 'Invalid phone number';
            }
            if (!formData.name) newErrors.name = 'Name is required';
            if (!formData.surname) newErrors.surname = 'Surname is required';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleNext = () => {
        if (validateFields()) {
            setStep(2);
        }
    };

    const handleBack = () => {
        setStep(1);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (validateFields()) {
            setError('');
            setSuccessMessage('');
            try {
                const apiUrl = process.env.REACT_APP_API_URL || "/api";
                const response = await axios.post(`${apiUrl}/users/register`, formData);
                setSuccessMessage('Registration successful! You can now log in.');
            } catch (error) {
                if (error.response) {
                    setError(error.response.data.message || 'Registration error. Username or email is already taken. Please try again.');
                } else {
                    setError('Network error. Please try again later.');
                }
            }
        }
    };

    return (
        <div className="auth-wrapper">
            <div className="auth-container">
                <h1>Register</h1>
                {error && <p style={{ color: 'red' }}>{error}</p>}
                {successMessage && (
                    <div>
                        <p style={{ color: 'green' }}>{successMessage}</p>
                    </div>
                )}
                <form onSubmit={handleSubmit}>
                    {step === 1 && (
                        <>
                            <div style={{ marginBottom: '3px' }} className="form-group">
                                <label>Username:</label>
                                <input
                                    type="text"
                                    name="username"
                                    value={formData.username}
                                    onChange={handleChange}
                                    required
                                    className={errors.username ? 'error-input' : ''}
                                />
                                {errors.username && <span className="error">{errors.username}</span>}
                            </div>
                            <div style={{ marginBottom: '3px' }} className="form-group">
                                <label>Email:</label>
                                <input
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                    required
                                    className={errors.email ? 'error-input' : ''}
                                />
                                {errors.email && <span className="error">{errors.email}</span>}
                            </div>
                            <div style={{ marginBottom: '3px' }} className="form-group">
                                <label>Password:</label>
                                <input
                                    type="password"
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    required
                                    className={errors.password ? 'error-input' : ''}
                                />
                                {errors.password && <span className="error">{errors.password}</span>}
                            </div>
                            <button type="button" onClick={handleNext}>
                                Next
                            </button>
                        </>
                    )}

                    {step === 2 && (
                        <>
                            <div style={{ marginBottom: '3px' }} className="form-group">
                                <label>Phone Number:</label>
                                <input
                                    type="text"
                                    name="phoneNumber"
                                    value={formData.phoneNumber}
                                    onChange={handleChange}
                                    required
                                    className={errors.phoneNumber ? 'error-input' : ''}
                                />
                                {errors.phoneNumber && <span className="error">{errors.phoneNumber}</span>}
                            </div>
                            <div style={{ marginBottom: '3px' }} className="form-group">
                                <label>Name:</label>
                                <input
                                    type="text"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleChange}
                                    required
                                    className={errors.name ? 'error-input' : ''}
                                />
                                {errors.name && <span className="error">{errors.name}</span>}
                            </div>
                            <div style={{ marginBottom: '3px' }} className="form-group">
                                <label>Surname:</label>
                                <input
                                    type="text"
                                    name="surname"
                                    value={formData.surname}
                                    onChange={handleChange}
                                    required
                                    className={errors.surname ? 'error-input' : ''}
                                />
                                {errors.surname && <span className="error">{errors.surname}</span>}
                            </div>
                            {successMessage ? (
                                <button type="button" onClick={() => navigate('/login')}>
                                    Login
                                </button>
                            ) : (
                                <>
                                    <button type="button" onClick={handleBack}>
                                        Back
                                    </button>
                                    <button type="submit">Register</button>
                                </>
                            )}
                        </>
                    )}
                </form>
            </div>
        </div>
    );
};

export default Register;