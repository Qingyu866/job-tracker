/** @type {import('tailwindcss').Config} */
export default {
  darkMode: ['class'],
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      animation: {
        'fade-in': 'fadeIn 0.2s ease-out',
        'scale-in': 'scaleIn 0.2s ease-out',
        'slide-up': 'slideUp 0.3s ease-out',
        'slide-down': 'slideDown 0.3s ease-out',
        'slide-in-right': 'slideInRight 0.3s ease-out',
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        scaleIn: {
          '0%': { transform: 'scale(0.95)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        slideDown: {
          '0%': { transform: 'translateY(-10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        slideInRight: {
          '0%': { transform: 'translateX(100%)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' },
        },
      },
      colors: {
        paper: {
          50: '#faf8f3',
          100: '#f5f0e6',
          200: '#ebe4d6',
          300: '#ddd4c0',
          400: '#c9bd9f',
          500: '#8b7355',
          600: '#6b5344',
          700: '#4a3828',
          800: '#3d2e22',
          900: '#2a1f18',
        },
        accent: {
          amber: '#d4a574',
          green: '#8fbc8f',
          blue: '#7ba3b5',
          red: '#e53d3d',
          purple: '#9f7fb5',
        },
        status: {
          pending: '#3b82f6',
          success: '#22c55e',
          warning: '#f59e0b',
          error: '#ef4444',
          info: '#06b6d4',
        },
      },
      fontFamily: {
        reading: [
          'Georgia',
          'Cambria',
          '"Times New Roman"',
          'Times',
          'serif'
        ],
        ui: [
          '-apple-system',
          'BlinkMacSystemFont',
          '"Segoe UI"',
          'Roboto',
          'sans-serif'
        ],
        mono: [
          '"SF Mono"',
          'Monaco',
          'Inconsolata',
          '"Fira Code"',
          'monospace'
        ],
      },
      boxShadow: {
        'paper': '0 1px 3px rgba(74, 56, 40, 0.12), 0 1px 2px rgba(74, 56, 40, 0.08)',
        'paper-md': '0 4px 6px rgba(74, 56, 40, 0.1), 0 2px 4px rgba(74, 56, 40, 0.06)',
        'paper-lg': '0 10px 15px rgba(74, 56, 40, 0.1), 0 4px 6px rgba(74, 56, 40, 0.05)',
        'paper-xl': '0 20px 25px rgba(74, 56, 40, 0.1), 0 10px 10px rgba(74, 56, 40, 0.04)',
      },
      backgroundImage: {
        'report-gradient': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'paper-texture': 'linear-gradient(to right, rgba(0,0,0,0.02) 1px, transparent 1px), linear-gradient(to bottom, rgba(0,0,0,0.02) 1px, transparent 1px)',
      },
      spacing: {
        '18': '4.5rem',
        '88': '22rem',
        '112': '28rem',
        '128': '32rem',
      },
      borderRadius: {
        '4xl': '2rem',
      },
      zIndex: {
        '60': '60',
        '70': '70',
        '80': '80',
        '90': '90',
        '100': '100',
      },
    }
  }
}
