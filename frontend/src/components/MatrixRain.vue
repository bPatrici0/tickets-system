<template>
  <canvas 
    ref="matrixCanvas" 
    class="matrix-rain"
    :class="{ 'fade-out': !active }"
  ></canvas>
</template>

<script>
export default {
  name: 'MatrixRain',
  props: {
    active: {
      type: Boolean,
      default: true
    },
    timeout: {
      type: Number,
      default: 2000
    }
  },
  data() {
    return {
      canvas: null,
      ctx: null,
      columns: 0,
      drops: [],
      fontSize: 16,
      chars: 'アカサタナハマヤラワABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789$+-*/=%\"\'#&_(),.;:?!\\|{}<>[]',
      animationId: null
    };
  },
  mounted() {
    this.initCanvas();
    window.addEventListener('resize', this.initCanvas);
    this.animate();
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.initCanvas);
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
    }
  },
  methods: {
    initCanvas() {
      this.canvas = this.$refs.matrixCanvas;
      if (!this.canvas) return;
      
      this.ctx = this.canvas.getContext('2d');
      this.canvas.width = window.innerWidth;
      this.canvas.height = window.innerHeight;

      this.columns = Math.floor(this.canvas.width / this.fontSize);
      this.drops = [];
      for (let i = 0; i < this.columns; i++) {
        this.drops[i] = Math.random() * -100; // Start off-screen
      }
    },
    draw() {
      // Semi-transparent black rectangle to create trail effect
      this.ctx.fillStyle = 'rgba(0, 0, 0, 0.05)';
      this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

      this.ctx.fillStyle = '#00FF41'; // Matrix Green
      this.ctx.font = this.fontSize + 'px monospace';

      for (let i = 0; i < this.drops.length; i++) {
        const text = this.chars[Math.floor(Math.random() * this.chars.length)];
        this.ctx.fillText(text, i * this.fontSize, this.drops[i] * this.fontSize);

        if (this.drops[i] * this.fontSize > this.canvas.height && Math.random() > 0.975) {
          this.drops[i] = 0;
        }
        this.drops[i]++;
      }
    },
    animate() {
      this.draw();
      this.animationId = requestAnimationFrame(this.animate);
    }
  }
};
</script>

<style scoped>
.matrix-rain {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 999998;
  pointer-events: none;
  background: black;
  transition: opacity 0.8s ease-in-out;
}

.fade-out {
  opacity: 0;
}
</style>
