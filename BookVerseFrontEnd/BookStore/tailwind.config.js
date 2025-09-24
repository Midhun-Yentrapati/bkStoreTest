/** @type {import('tailwindcss').Config} */
module.exports = {

  content: [
    // These paths should point to all of your template files
    // where you use Tailwind CSS classes.
    "./src/**/*.{html,ts,css}",
  ],
  theme: {
    extend: {
      // Define custom color palette for light theme.
      colors: {
        light: {
          'bg': '#ffffff',
          'text': '#000000',
          'header-bg': '#1a1a1a',
          'header-text': '#ffffff',
          'nav-link': '#ffffff',
          'nav-link-hover': '#cccccc',
          'footer-bg': '#1a1a1a',
          'footer-text': '#ffffff',
          'footer-link': '#ffffff',
          'border': '#e0e0e0',
          'widget-bg': '#f8f8f8',
          'widget-text': '#333333',
        },
      },
      // Define the custom font family 'EB Garamond'.
      fontFamily: {
        garamond: ['"EB Garamond"', 'serif'],
      },
      // You can also extend other theme properties like spacing, borderRadius, etc.
      // For example, if you need a precise 8px border radius not covered by 'rounded-lg':
      // borderRadius: {
      //   '8px': '8px',
      // }
    },
  },
  plugins: [],
};
