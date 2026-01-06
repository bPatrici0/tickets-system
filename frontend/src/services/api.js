import axios from 'axios';
import router from '@/router';

const api = axios.create({
    baseURL: 'http://localhost:8081/api',
    //withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
        //'Authorization': 'Basic ' + btoa(localStorage.getItem('userEmail') + ':' + localStorage.getItem('userPassword'))
    }
});

api.interceptors.request.use(config => {
    const token = localStorage.getItem('token');

    console.log('Token encontrado en localStorage:', token ? 'SÍ' : 'NO');
    console.log('Peticion a:', config.url);
    //const email = localStorage.getItem('userEmail');
    //const password = localStorage.getItem('userPassword');

    if(token) {
        config.headers.Autthorization = `Basic ${token}`;
        console.log('Autorization header agregado:', config.headers.Authorization.substring(0, 30) + '...');
    } else {
        console.log('no hay token en localStorage');
    }

    return config;
}, error => {
    console.error('Error en interceptor de request:', error);
    return Promise.reject(error);
});

//interceptor para manejar errores
api.interceptors.response.use(
    response => {
        console.log('Response recibido:', response.config.url, response.status);
        return response;
    },
    error => {
        console.error('Error en response:', error.config?.url, error.response?.status);

        if (error.response?.status === 401 || error.response?.status === 403) {
            console.log('Error de autentificación - Limpiando localStorage');
            localStorage.removeItem('token');
            localStorage.removeItem('userRole');
            localStorage.removeItem('userEmail');
            localStorage.removeItem('userName');

            router.push('/login');
        }
        return Promise.reject(error);
    }
);

export default api;