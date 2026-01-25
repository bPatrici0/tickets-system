/**
 * SoundService.js
 * Gestiona efectos de sonido similares a terminales mediante Web Audio API
 */

class SoundService {
    constructor() {
        this.audioCtx = null;
        this.initialized = false;
    }

    init() {
        if (this.initialized) return;
        try {
            this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();
            this.initialized = true;
            console.log('>>> SoundService Initialized');
        } catch (e) {
            console.warn('Web Audio API not supported in this browser');
        }
    }

    // Reproduce un pitido sintético
    beep(frequency, duration, volume = 0.1, type = 'sine') {
        if (!this.initialized) this.init();
        if (!this.audioCtx) return;

        const oscillator = this.audioCtx.createOscillator();
        const gainNode = this.audioCtx.createGain();

        oscillator.type = type;
        oscillator.frequency.setValueAtTime(frequency, this.audioCtx.currentTime);

        gainNode.gain.setValueAtTime(volume, this.audioCtx.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.00001, this.audioCtx.currentTime + duration);

        oscillator.connect(gainNode);
        gainNode.connect(this.audioCtx.destination);

        oscillator.start();
        oscillator.stop(this.audioCtx.currentTime + duration);
    }

    playSuccess() {
        // Doble pitido agudo
        this.beep(880, 0.1, 0.05, 'square');
        setTimeout(() => this.beep(1100, 0.1, 0.05, 'square'), 100);
    }

    playError() {
        // Pitido grave y ominoso
        this.beep(150, 0.3, 0.1, 'sawtooth');
    }

    playNotification() {
        // Sonido de alerta
        this.beep(660, 0.2, 0.05, 'sine');
    }

    playAlarm() {
        // Sonido de alarma crítica (repetitivo)
        this.beep(440, 0.2, 0.1, 'sawtooth');
        setTimeout(() => this.beep(440, 0.2, 0.1, 'sawtooth'), 250);
        setTimeout(() => this.beep(440, 0.4, 0.1, 'sawtooth'), 500);
    }

    playClick() {
        // Click mecánico sutil
        this.beep(Math.random() * 50 + 400, 0.01, 0.02, 'sine');
    }
}

export default new SoundService();
