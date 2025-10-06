export const testConfig = {
  testUser: {
    username: process.env['TEST_USERNAME'] || 'testuser',
    email: process.env['TEST_EMAIL'] || 'test@example.com',
    password: process.env['TEST_PASSWORD'] || 'TestPassword123!'
  },
  testAdmin: {
    username: process.env['TEST_ADMIN_USERNAME'] || 'testadmin',
    email: process.env['TEST_ADMIN_EMAIL'] || 'admin@example.com',
    password: process.env['TEST_ADMIN_PASSWORD'] || 'AdminPassword123!'
  },
  apiUrls: {
    auth: process.env['TEST_AUTH_URL'] || 'http://localhost:8081/api/auth',
    cart: process.env['TEST_CART_URL'] || 'http://localhost:8082/api',
    books: process.env['TEST_BOOKS_URL'] || 'http://localhost:8080/api'
  }
};