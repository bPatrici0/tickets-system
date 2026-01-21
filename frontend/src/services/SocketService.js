import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import Swal from 'sweetalert2';

class SocketService {
    constructor() {
        this.client = null;
        this.connected = false;
        this.subscriptions = [];
    }

    connect(userEmail, userRole) {
        if (this.connected) return;

        // URL del backend 
        const socketUrl = 'http://localhost:8080/ws';

        this.client = new Client({
            webSocketFactory: () => new SockJS(socketUrl),
            debug: (str) => {
                console.log('WS Debug:', str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        this.client.onConnect = (frame) => {
            console.log('WS Conectado:', frame);
            this.connected = true;

            // 1. Suscribirse a notificaciones globales para el rol (Admins)
            if (userRole === 'ROLE_ADMIN') {
                this.client.subscribe('/topic/admins', (message) => {
                    this.showNotification('ADMIN ALERT', message.body);
                });
            }

            // 2. Suscribirse a notificaciones personales (User queue)
            // Spring usa /user/queue/notifications que se traduce internamente por usuario
            this.client.subscribe('/user/queue/notifications', (message) => {
                this.showNotification('SISTEMA', message.body);
            });

            // 3. Suscribirse a actualizaciones generales de tickets
            this.client.subscribe('/topic/tickets', (message) => {
                console.log('Ticket Broadcast:', message.body);
            });
        };

        this.client.onStompError = (frame) => {
            console.error('WS Error:', frame.headers['message']);
            console.error('Detalles:', frame.body);
        };

        this.client.activate();
    }

    showNotification(title, message) {
        Swal.fire({
            title: `> ${title}`,
            text: message,
            icon: 'info',
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 5000,
            timerProgressBar: true,
            background: '#000',
            color: '#00ff41',
            customClass: {
                popup: 'border border-green-500 rounded-none shadow-[0_0_15px_rgba(0,255,65,0.3)]',
                title: 'font-mono text-xs',
                htmlContainer: 'font-mono text-xs'
            }
        });
    }

    disconnect() {
        if (this.client) {
            this.client.deactivate();
            this.connected = false;
        }
    }
}

export default new SocketService();
