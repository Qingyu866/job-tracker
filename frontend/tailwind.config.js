/** @type {import('tailwindcss').Config} */
export default {
  darkMode: ['class'],
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      colors: {
        // 牛皮纸风格配色
        paper: {
          50: '#faf8f3',  // 最浅
          100: '#f5f0e6', // 浅色背景
          200: '#ebe4d6', // 边框
          300: '#ddd4c0', // 悬浮
          400: '#c9bd9f', // 次要文本
          500: '#8b7355', // 主要文本（深棕色）
          600: '#6b5344', // 深色文本
          700: '#4a3828', // 标题
          800: '#3d2e22', // 边框深色
          900: '#2a1f18', // 最深
        },
        // 强调色（保持温暖色调）
        accent: {
          amber: '#d4a574',
          green: '#8fbc8f',
          blue: '#7ba3b5',
          red: '#c17f7f',
          purple: '#9f7fb5',
        }
      },
      fontFamily: {
        // 阅读字体栈
        reading: [
          'Georgia',
          'Cambria',
          '"Times New Roman"',
          'Times',
          'serif'
        ],
        // UI 字体栈
        ui: [
          '-apple-system',
          'BlinkMacSystemFont',
          '"Segoe UI"',
          'Roboto',
          'sans-serif'
        ]
      },
      boxShadow: {
        // 纸张阴影
        'paper': '0 1px 3px rgba(74, 56, 40, 0.12), 0 1px 2px rgba(74, 56, 40, 0.08)',
        'paper-md': '0 4px 6px rgba(74, 56, 40, 0.1), 0 2px 4px rgba(74, 56, 40, 0.06)',
        'paper-lg': '0 10px 15px rgba(74, 56, 40, 0.1), 0 4px 6px rgba(74, 56, 40, 0.05)',
      }
    }
  }
}
