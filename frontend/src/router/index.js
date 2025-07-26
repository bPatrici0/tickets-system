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
        meta: { title: '> Login' }
    },
    {
        path: '/register',
        name: 'register',
        component: RegisterView,
        meta: { title: '> Registro' }
    },
    {
        path: '/tickets',
        name: 'tickets',
        component: TicketsView,
        meta: { title: '> Tickets', requiresAuth: true }
    }
];
const router = createRouter({
    history: createWebHistory(),
    routes
});

router.afterEach((to) => {
    document.title = to.meta.title || '> Sistema de Tickets';
});

/*router.beforeEach((to) => {
    if (to.meta.requiresAuth && !LocalStorage.getItem('token')){
        return '/login';
    }
})*/

export default router;