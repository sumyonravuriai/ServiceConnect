/**
 * ServiceConnect - Main Utility & UI Functions
 */

// Toast Notifications System
function showToast(message, type = 'info') {
  let container = document.querySelector('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  
  let icon = 'ℹ️';
  if (type === 'success') icon = '✅';
  if (type === 'error') icon = '⚠️';

  toast.innerHTML = `
    <span>${icon}</span>
    <span>${message}</span>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// Format Currency
function formatCurrency(amount) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0
  }).format(amount);
}

// Render Star Rating HTML
function renderRatingStars(rating) {
  const full = Math.floor(rating);
  const hasHalf = rating - full >= 0.5;
  let stars = '';
  for (let i = 0; i < full; i++) stars += '★';
  if (hasHalf) stars += '★';
  while (stars.length < 5) stars += '☆';
  return `<span class="star-icon">${stars}</span> <span>${rating.toFixed(1)}</span>`;
}

// Initialize Global UI Components
document.addEventListener('DOMContentLoaded', () => {
  // Mobile Nav Toggle
  const mobileToggle = document.querySelector('.mobile-menu-btn');
  const navMenu = document.querySelector('.nav-menu');
  if (mobileToggle && navMenu) {
    mobileToggle.addEventListener('click', () => {
      navMenu.classList.toggle('active');
    });
  }

  // Quick Search Form on Hero
  const heroSearchForm = document.getElementById('heroSearchForm');
  if (heroSearchForm) {
    heroSearchForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const service = document.getElementById('searchService')?.value.trim() || '';
      const location = document.getElementById('searchLocation')?.value.trim() || '';
      const isSub = window.location.pathname.includes('/pages/');
      const target = isSub ? 'providers.html' : 'pages/providers.html';
      const params = new URLSearchParams();
      if (service) params.set('category', service);
      if (location) params.set('city', location);
      window.location.href = `${target}?${params.toString()}`;
    });
  }
});
