import axios from 'axios';
import router from '@/router';

const api = axios.create({
    baseURL: 'http://localhost:8081/api',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + btoa(localStorage.getItem('userEmail') + ':' + localStorage.getItem('userPassword'))
    }
});

api.interceptors.request.use(config => {
    const email = localStorage.getItem('userEmail');
    const password = localStorage.getItem('userPassword');

    if(email && password) {
        config.auth = {
            username: email,
            password: password
        };
    }
    return config;
});

//interceptor para manejar errores
api.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            localStorage.removeItem('userEmail');
            localStorage.removeItem('userPassword');
            localStorage.removeItem('userRole');
            router.push('/login');
        }
        return Promise.reject(error);
    }
);

export default api;