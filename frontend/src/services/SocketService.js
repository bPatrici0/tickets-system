import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import Swal from 'sweetalert2';
import SoundService from './SoundService';

class SocketService {
    constructor() {
        this.client = null;
        this.connected = false;
        this.connecting = false;
        this.listeners = new Map();
    }

    on(event, callback) {
        if (!this.listeners.has(event)) {
            this.listeners.set(event, []);
        }
        this.listeners.get(event).push(callback);
    }

    off(event, callback) {
        if (!this.listeners.has(event)) return;
        const filtered = this.listeners.get(event).filter(cb => cb !== callback);
        this.listeners.set(event, filtered);
    }

    emit(event, data) {
        if (!this.listeners.has(event)) return;
        this.listeners.get(event).forEach(cb => cb(data));
    }

    connect(userEmail, userRole) {
        if (this.connected || this.connecting) return;
        this.connecting = true;

        // URL del backend dinámica (Coincidir con la IP del host)
        const host = window.location.hostname;
        const socketUrl = `http://${host}:8081/ws`;

        this.client = new Client({
            webSocketFactory: () => new SockJS(socketUrl),
            // debug: (str) => {
            //     console.log('WS Debug:', str);
            // },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        this.client.onConnect = (frame) => {
            console.log('WS Conectado:', frame);
            this.connected = true;
            this.connecting = false;

            // 1. Suscribirse a notificaciones globales para el rol (Admins)
            if (userRole === 'ROLE_ADMIN') {
                this.client.subscribe('/topic/admins', (message) => {
                    this.showNotification('ADMIN ALERT', message.body);
                    this.emit('TICKET_UPDATE', message.body);
                });
            }

            // 2. Suscribirse a canal personal basado en email
            this.client.subscribe(`/topic/user.${userEmail}`, (message) => {
                this.showNotification('SISTEMA', message.body);
                this.emit('TICKET_UPDATE', message.body);
            });

            // 3. Suscribirse a actualizaciones generales de tickets (Broadcast Global)
            this.client.subscribe('/topic/tickets', (message) => {
                console.log('>>> Ticket Broadcast recibido:', message.body);
                // Emitimos el evento para que las listas y detalles se refresquen
                this.emit('TICKET_UPDATE', message.body);
            });
        };

        this.client.onStompError = (frame) => {
            console.error('WS Error:', frame.headers['message']);
            console.error('Detalles:', frame.body);
            this.connecting = false;
        };

        this.client.onWebSocketClose = () => {
            this.connected = false;
            this.connecting = false;
        };

        this.client.activate();
    }

    showNotification(title, message) {
        const isCritical = message.includes('CRITICA') || message.includes('CRÍTICA');
        if (isCritical) {
            SoundService.playAlarm();
        } else {
            SoundService.playNotification();
        }
        Swal.fire({
            title: `> ${title}`,
            text: message,
            icon: isCritical ? 'error' : 'info',
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: isCritical ? 10000 : 5000,
            timerProgressBar: true,
            background: isCritical ? '#440000' : '#000',
            color: isCritical ? '#ff0000' : '#00ff41',
            customClass: {
                popup: `border ${isCritical ? 'border-red-600' : 'border-green-500'} rounded-none shadow-[0_0_15px_rgba(255,0,0,0.3)]`,
                title: 'font-mono text-xs font-bold',
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
