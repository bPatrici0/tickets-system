import { createVuetify } from 'vuetify'
import * as component from 'vuetify/components'
import * as directives from 'vuetify/directives'

export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'matrixTheme',
    themes: {
      matrixTheme: {
        dark: true,
        colors: {
          background: '#000000',
          surface: '#001100',
          primary: '#00ff88',
          'primary-darken-1': '#00cc66',
          secondary: '#003300',
          error: '#ff0044',
          info: '#0099ff',
          success: '#00ff88',
          warning: '#ffaa00',
        }
      }
    }
  }
})
