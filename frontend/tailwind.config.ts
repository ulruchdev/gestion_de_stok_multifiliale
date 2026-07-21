import type { Config } from 'tailwindcss';

const config: Config = {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    container: {
      center: true,
      padding: '2rem',
      screens: {
        '2xl': '1400px',
      },
    },
    extend: {
      // === Couleurs shadcn/ui ===
      colors: {
        border: 'hsl(var(--border))',
        input: 'hsl(var(--input))',
        ring: 'hsl(var(--ring))',
        background: 'hsl(var(--background))',
        foreground: 'hsl(var(--foreground))',
        primary: {
          DEFAULT: 'hsl(var(--primary))',
          foreground: 'hsl(var(--primary-foreground))',
        },
        secondary: {
          DEFAULT: 'hsl(var(--secondary))',
          foreground: 'hsl(var(--secondary-foreground))',
        },
        destructive: {
          DEFAULT: 'hsl(var(--destructive))',
          foreground: 'hsl(var(--destructive-foreground))',
        },
        muted: {
          DEFAULT: 'hsl(var(--muted))',
          foreground: 'hsl(var(--muted-foreground))',
        },
        accent: {
          DEFAULT: 'hsl(var(--accent))',
          foreground: 'hsl(var(--accent-foreground))',
        },
        popover: {
          DEFAULT: 'hsl(var(--popover))',
          foreground: 'hsl(var(--popover-foreground))',
        },
        card: {
          DEFAULT: 'hsl(var(--card))',
          foreground: 'hsl(var(--card-foreground))',
        },
        // === Marque StockMaster ===
        brand: {
          accent: '#533afd',
          'accent-hover': '#7d63ff',
          'accent-active': '#3727d6',
          'accent-bg': '#f4f0ff',
          ink: '#0d253d',
          'ink-muted': '#64748b',
          canvas: '#ffffff',
          'canvas-soft': '#f6f9fc',
          hairline: '#e3e8ee',
          dark: '#1c1e54',
        },
        // === États stocks (contexte camerounais) ===
        stock: {
          warning: '#faad14',
          'warning-bg': '#fffbe6',
          danger: '#ff4d4f',
          'danger-bg': '#fff2f0',
          success: '#52c41a',
          'success-bg': '#f6ffed',
        },
      },
      // === Typographie ===
      fontFamily: {
        display: ['SF Pro Display', 'system-ui', '-apple-system', 'sans-serif'],
        body: ['system-ui', '-apple-system', 'sans-serif'],
        mono: ['ui-monospace', 'SF Mono', 'Cascadia Code', 'monospace'],
      },
      borderRadius: {
        lg: 'var(--radius)',
        md: 'calc(var(--radius) - 2px)',
        sm: 'calc(var(--radius) - 4px)',
      },
      // === Animations ===
      keyframes: {
        'accordion-down': {
          from: { height: '0' },
          to: { height: 'var(--radix-accordion-content-height)' },
        },
        'accordion-up': {
          from: { height: 'var(--radix-accordion-content-height)' },
          to: { height: '0' },
        },
        'fade-in': {
          from: { opacity: '0' },
          to: { opacity: '1' },
        },
        'fade-in-up': {
          from: { opacity: '0', transform: 'translateY(8px)' },
          to: { opacity: '1', transform: 'translateY(0)' },
        },
        'scale-in': {
          from: { opacity: '0', transform: 'scale(0.95)' },
          to: { opacity: '1', transform: 'scale(1)' },
        },
        'slide-in-right': {
          from: { transform: 'translateX(100%)' },
          to: { transform: 'translateX(0)' },
        },
        'slide-in-left': {
          from: { transform: 'translateX(-100%)' },
          to: { transform: 'translateX(0)' },
        },
        'slide-in-down': {
          from: { transform: 'translateY(-100%)', opacity: '0' },
          to: { transform: 'translateY(0)', opacity: '1' },
        },
      },
      animation: {
        'accordion-down': 'accordion-down 0.2s ease-out',
        'accordion-up': 'accordion-up 0.2s ease-out',
        'fade-in': 'fade-in var(--brand-duration-mid) var(--brand-ease-out)',
        'fade-in-up': 'fade-in-up var(--brand-duration-mid) var(--brand-ease-out)',
        'scale-in': 'scale-in var(--brand-duration-mid) var(--brand-ease-out)',
        'slide-in-right': 'slide-in-right var(--brand-duration-slow) var(--brand-ease-out)',
        'slide-in-left': 'slide-in-left var(--brand-duration-slow) var(--brand-ease-out)',
        'slide-in-down': 'slide-in-down var(--brand-duration-mid) var(--brand-ease-out)',
      },
      // Breakpoints mobile-first (US-D005)
      screens: {
        xs: '360px',
        sm: '640px',
        md: '768px',
        lg: '1024px',
        xl: '1280px',
        '2xl': '1536px',
      },
    },
  },
  plugins: [],
};

export default config;
