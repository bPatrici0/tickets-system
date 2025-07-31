import axios from 'axios';
import router from '@/router';

const api = axios.create({
    baseURL: 'http://localhost:8081/api',
});

api.interceptors.request.use(config => {
    const email = localStorage.getItem('userEmail');
    const password = '';
    if(email) {
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
        if (error.response.status === 401) {
            localStorage.removeItem('authToken');
            router.push('/login');
        }
        return Promise.reject(error);
    }
);

export default api;