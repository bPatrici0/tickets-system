import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import TicketsView from '../views/TicketsView.vue';

const routes = [
    {
        path:'/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'login',
        component: LoginView,
        meta: {
            title: '> Login',
            public: true
        }
    },
    {
        path: '/register',
        name: 'register',
        component: RegisterView,
        meta: {
            title: '> Registro',
            public: true
        }
    },
    {
        path: '/tickets',
        name: 'tickets',
        component: TicketsView,
        meta: {
            title: '> Tickets',
            requiresAuth: true
        }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach ((to, from, next) => {
    const isAuthenticated = localStorage.getItem('authToken');

    if (to.meta.requiresAuth && !isAuthenticated) {
        next('/login');
    } else if (isAuthenticated && (to.name === 'login' || to.name === 'register')) {
        next('/tickets'); //redirige al dashboard
    }
    else {
        next();
    }
});

router.afterEach((to) => {
    document.title = to.meta.title || '> Sistema de Tickets';
});

export default router;