export const environment = {
  production: false,
  apiUrl: process.env['API_URL'] || 'https://localhost:8080/api',
  authUrl: process.env['AUTH_URL'] || 'https://localhost:8081/api/auth',
  cartUrl: process.env['CART_URL'] || 'https://localhost:8082/api',
  adminUrl: process.env['ADMIN_URL'] || 'https://localhost:8083/api',
  jwtSecret: process.env['JWT_SECRET'] || 'default-secret-key',
  enableLogging: true,
  logLevel: 'info'
};

export const environmentProd = {
  production: true,
  apiUrl: process.env['API_URL'] || 'https://api.bookverse.com',
  authUrl: process.env['AUTH_URL'] || 'https://auth.bookverse.com/api',
  cartUrl: process.env['CART_URL'] || 'https://cart.bookverse.com/api',
  adminUrl: process.env['ADMIN_URL'] || 'https://admin.bookverse.com/api',
  jwtSecret: process.env['JWT_SECRET'] || '',
  enableLogging: false,
  logLevel: 'error'
};