import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import AdminPanel from '../views/AdminPanel.vue';
import UserTickets from '../views/UserTickets.vue';
import TicketView from '../views/TicketView.vue';
import AdminPanelUser from '../views/AdminPanelUser.vue';
import AdminTicketEstatus from '../views/AdminTicketEstatus.vue';
import AdminTicketsView from '../views/AdminTicketsView.vue';

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
        path: '/admin',
        name: 'admin',
        component: AdminPanel,
        meta: {
            title: '>Admin Panel',
            requiresAuth: true,
            requiredAdmin: true
        }
    },
    {
        path: '/tickets',
        name: 'tickets',
        component: UserTickets,
        meta: {
            title: '> Tickets',
            requiresAuth: true
        }
    },
    {
        path: '/tickets/:id',
        name: 'TicketView',
        component: TicketView,
        meta: {
            title: '> Detalle ticket',
            requiresAuth: true
        }
    },
    {
        path: '/admin/AdminPanelUser',
        name: 'AdminUser',
        component: AdminPanelUser,
        meta: {
            title: '> Admin - Usuarios',
            requiresAuth: true,
            requiresAdmin: true
        }
    },
    {
        path: '/admin/tickets',
        name: 'AdminTickets',
        component: AdminTicketsView,
        meta: {
            title: '> Admin - Tickets',
            requiresAuth: true,
            requiresAdmin: true
        }
    },
    {
        path: '/admin/tickets/:id',
        name: 'AdminTicketEstatus',
        component: AdminTicketEstatus,
        meta: {
            title: '> Admin - Detalle Ticket',
            requiresAuth: true,
            requiresAdmin: true
        }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach ((to, from, next) => {
    const isAuthenticated = localStorage.getItem('userEmail');
    const userRole = localStorage.getItem('userRole');

    if(to.meta.public) {
        return next();
    }

    if (to.meta.requiresAuth && !isAuthenticated) {
        next('/login');
    } else if (isAuthenticated && (to.name === 'login' || to.name === 'register')) {
        next(userRole === 'ROLE_ADMIN' ? '/admin' : '/tickets');
    } else if (to.meta.requiredRole && userRole !== to.meta.requiredRole) {
        next(userRole === 'ROLE_ADMIN' ? 'admin' : '/tickets');
    } else {
        next();
    }
});

router.afterEach((to) => {
    document.title = to.meta.title || '> Sistema de Tickets';
});

export default router;