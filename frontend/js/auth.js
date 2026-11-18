/**
 * ServiceConnect - Authentication & Role Manager
 */

const Auth = {
  // Get current logged-in user
  getUser() {
    try {
      const data = localStorage.getItem('sc_user');
      return data ? JSON.parse(data) : null;
    } catch (e) {
      return null;
    }
  },

  // Set user session
  setUser(user, token = 'sample_jwt_token_serviceconnect') {
    localStorage.setItem('sc_user', JSON.stringify(user));
    localStorage.setItem('sc_token', token);
  },

  // Clear session
  logout() {
    localStorage.removeItem('sc_user');
    localStorage.removeItem('sc_token');
    window.location.href = this.isSubpage() ? '../index.html' : 'index.html';
  },

  // Check if current page is in /pages/ subfolder
  isSubpage() {
    return window.location.pathname.includes('/pages/');
  },

  // Require specific role or redirect
  requireRole(allowedRoles = []) {
    const user = this.getUser();
    if (!user) {
      const target = this.isSubpage() ? 'login.html' : 'pages/login.html';
      window.location.href = target;
      return null;
    }
    if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
      alert(`Access restricted. Requires ${allowedRoles.join(' or ')} privileges.`);
      window.location.href = this.isSubpage() ? '../index.html' : 'index.html';
      return null;
    }
    return user;
  },

  // Synchronize top navigation bar with user state
  renderNav() {
    const navActions = document.querySelector('.nav-actions');
    if (!navActions) return;

    const user = this.getUser();
    const isSub = this.isSubpage();
    const prefix = isSub ? '' : 'pages/';
    const homePrefix = isSub ? '../' : '';

    if (user) {
      let dashboardUrl = `${prefix}customer-dashboard.html`;
      if (user.role === 'PROVIDER') dashboardUrl = `${prefix}provider-dashboard.html`;
      if (user.role === 'ADMIN') dashboardUrl = `${prefix}admin-dashboard.html`;

      navActions.innerHTML = `
        <a href="${dashboardUrl}" class="btn btn-outline btn-sm">
          <span>👤 ${user.fullName || user.email}</span>
          <span class="badge ${user.role === 'ADMIN' ? 'badge-progress' : (user.role === 'PROVIDER' ? 'badge-accepted' : 'badge-completed')}">${user.role}</span>
        </a>
        <button class="btn btn-text btn-sm" onclick="Auth.logout()" title="Sign Out">Sign Out</button>
      `;
    } else {
      navActions.innerHTML = `
        <a href="${prefix}login.html" class="btn btn-text btn-sm">Log In</a>
        <a href="${prefix}register.html" class="btn btn-primary btn-sm">Register</a>
      `;
    }
  }
};

document.addEventListener('DOMContentLoaded', () => {
  Auth.renderNav();
});
