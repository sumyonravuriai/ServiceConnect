/**
 * ServiceConnect - Central API Client
 * Seamlessly interfaces with Spring Boot REST Endpoints
 * Includes rich realistic fallback data for standalone frontend previews.
 */

// Live Production Backend URL on Render
const DEFAULT_BACKEND_API_URL = 'https://serviceconnect-backend-pg2o.onrender.com/api';

// Dynamically resolve REST API Base URL
const getApiBaseUrl = () => {
  if (window.__SERVICE_CONNECT_API_URL__) return window.__SERVICE_CONNECT_API_URL__;
  const configured = localStorage.getItem('sc_api_url');
  if (configured) return configured.replace(/\/+$/, '');

  // Local Spring Boot development override if explicitly tested on port 8080
  if (window.location.port === '8080') {
    return '/api';
  }

  // Default to live cloud backend on Render
  return DEFAULT_BACKEND_API_URL;
};

// Global helper to configure live backend API host from browser console or settings
window.setBackendUrl = (url) => {
  if (!url) {
    localStorage.removeItem('sc_api_url');
  } else {
    const formatted = url.trim().replace(/\/+$/, '');
    localStorage.setItem('sc_api_url', formatted.endsWith('/api') ? formatted : `${formatted}/api`);
  }
  window.location.reload();
};

const API_BASE_URL = getApiBaseUrl();

const Api = {
  // Flag to check if backend is reachable
  backendAvailable: null,

  async checkBackend() {
    if (this.backendAvailable === true) return true;
    try {
      // 25-second timeout to allow sleeping free-tier Render instances to complete cold-start
      const res = await fetch(`${API_BASE_URL}/health`, { method: 'GET', signal: AbortSignal.timeout(25000) });
      this.backendAvailable = res.ok;
    } catch (e) {
      this.backendAvailable = null;
      return false;
    }
    return this.backendAvailable;
  },

  getAuthHeaders() {
    const token = localStorage.getItem('sc_token');
    const headers = { 'Content-Type': 'application/json' };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
  },

  async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const headers = { ...this.getAuthHeaders(), ...(options.headers || {}) };

    try {
      const response = await fetch(url, { ...options, headers });
      const data = await response.json().catch(() => null);

      if (!response.ok) {
        const errMsg = (data && data.message) ? data.message : `Server Error: HTTP ${response.status}`;
        throw new Error(errMsg);
      }
      this.backendAvailable = true;
      return data;
    } catch (err) {
      // If server returned an application error message (e.g. 401, 400), throw directly
      if (err.message && !err.message.includes('Failed to fetch') && !err.message.includes('NetworkError') && !err.name.includes('AbortError')) {
        throw err;
      }

      // If connection to Render failed or timed out:
      if (endpoint.startsWith('/auth') || endpoint.startsWith('/admin') || (options.method && options.method !== 'GET')) {
        throw new Error(
          'Connecting to live backend (' + API_BASE_URL + ')... If the Render free server was asleep, it may take 20-40 seconds to wake up. Please wait a moment and try again.'
        );
      }

      // Harmless preview fallback for GET browsing if backend is cold-starting
      return this.mockHandler(endpoint, options);
    }
  },

  // -------------------------------------------------------------
  // Mock Data Store (Synchronized with MySQL sample_data.sql)
  // -------------------------------------------------------------
  mockData: {
    categories: [
      { id: 1, name: 'Electrician', slug: 'electrician', icon: '⚡', description: 'Wiring, fixtures, fuse fixes, inverter installation & electrical repairs' },
      { id: 2, name: 'Plumber', slug: 'plumber', icon: '🔧', description: 'Pipe leaks, taps, bathroom fittings, drainage & water heaters' },
      { id: 3, name: 'Mechanic', slug: 'mechanic', icon: '🚗', description: 'Car & bike maintenance, engine tune-up, battery jump & roadside fix' },
      { id: 4, name: 'Cleaner', slug: 'cleaner', icon: '🧹', description: 'Deep home cleaning, sofa, carpet sanitation & kitchen deep-clean' },
      { id: 5, name: 'Carpenter', slug: 'carpenter', icon: '🪚', description: 'Furniture repair, door locks, modular cabinets & custom woodwork' },
      { id: 6, name: 'AC Repair', slug: 'ac-repair', icon: '❄️', description: 'AC servicing, gas refilling, cooling inspection & compressor check' },
      { id: 7, name: 'Painter', slug: 'painter', icon: '🎨', description: 'Interior/exterior wall painting, waterproofing & texture finishes' },
      { id: 8, name: 'Cooking Services', slug: 'cooking', icon: '🍳', description: 'Daily home chef, meal preparation, party catering & regional dishes' },
      { id: 9, name: 'Tutoring', slug: 'tutoring', icon: '📚', description: 'K-12 academics, STEM subjects, music & spoken language tutoring' },
      { id: 10, name: 'Pest Control', slug: 'pest-control', icon: '🐜', description: 'Termite, cockroach, rodent eradication & safe organic fumigation' }
    ],

    providers: [
      {
        id: 1,
        userId: 2,
        name: 'Rajesh Sharma',
        businessName: 'Sharma Spark Electricals',
        category: 'Electrician',
        categoryId: 1,
        city: 'Downtown',
        serviceArea: 'Downtown, Westside, Metro Core',
        experience: '8 years',
        experienceYears: 8,
        rating: 4.9,
        reviewsCount: 38,
        startingPrice: 350,
        isVerified: true,
        isAvailable: true,
        avatar: 'https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=150',
        bio: 'Licensed master electrician with over 8 years experience in residential wiring, appliance setups, switchboard repairs and LED installations.',
        services: [
          { id: 101, name: 'Switchboard Repair & Wiring Check', price: 350, duration: '45 mins' },
          { id: 102, name: 'Ceiling Fan Installation / Servicing', price: 250, duration: '30 mins' },
          { id: 103, name: 'Complete Inverter & Battery Wiring', price: 950, duration: '90 mins' },
          { id: 104, name: 'MCB / Short Circuit Troubleshooting', price: 450, duration: '60 mins' }
        ],
        workingHours: 'Mon - Sat: 08:00 AM - 08:00 PM',
        availableSlots: ['09:00 AM', '11:00 AM', '02:00 PM', '04:30 PM', '06:00 PM'],
        reviews: [
          { id: 1, customerName: 'Anita Desai', rating: 5, date: '2026-11-10', comment: 'Rajesh was on time and fixed our short circuit within 40 minutes. Extremely polite and fair pricing!' },
          { id: 2, customerName: 'Vikram Singh', rating: 5, date: '2026-10-28', comment: 'Very professional electrical installation. Clean work, verified wiring safety before leaving.' }
        ]
      },
      {
        id: 2,
        userId: 3,
        name: 'David Miller',
        businessName: 'Miller Pro Plumbing Solutions',
        category: 'Plumber',
        categoryId: 2,
        city: 'Northside',
        serviceArea: 'Northside, Suburbia, Metro Core',
        experience: '6 years',
        experienceYears: 6,
        rating: 4.8,
        reviewsCount: 42,
        startingPrice: 400,
        isVerified: true,
        isAvailable: true,
        avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150',
        bio: 'Certified plumbing specialist addressing pipe leakages, bathroom fixture installations, geyser plumbing, and water tank blockages.',
        services: [
          { id: 201, name: 'Water Pipe Leakage Repair', price: 400, duration: '45 mins' },
          { id: 202, name: 'Tap / Shower Fitting Replacement', price: 300, duration: '30 mins' },
          { id: 203, name: 'Drainage Unclogging & Cleaning', price: 600, duration: '60 mins' },
          { id: 204, name: 'Water Geyser Connection & Testing', price: 500, duration: '60 mins' }
        ],
        workingHours: 'Daily: 07:30 AM - 07:30 PM',
        availableSlots: ['08:30 AM', '10:00 AM', '01:00 PM', '03:30 PM', '05:00 PM'],
        reviews: [
          { id: 3, customerName: 'Kavita Patel', rating: 5, date: '2026-11-04', comment: 'Saved our bathroom from a severe pipe burst. Came quickly on a Sunday!' }
        ]
      },
      {
        id: 3,
        userId: 4,
        name: 'Sunil Verma',
        businessName: 'FrostAir AC Technicians',
        category: 'AC Repair',
        categoryId: 6,
        city: 'Eastside',
        serviceArea: 'Eastside, Central, Downtown',
        experience: '10 years',
        experienceYears: 10,
        rating: 4.95,
        reviewsCount: 64,
        startingPrice: 500,
        isVerified: true,
        isAvailable: true,
        avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150',
        bio: 'Dedicated HVAC technician specializing in split, window and inverter AC servicing, gas charging, filter sanitization and PCB repairs.',
        services: [
          { id: 301, name: 'AC Deep Foam Jet Cleaning', price: 550, duration: '60 mins' },
          { id: 302, name: 'Gas Refilling & Leak Inspection', price: 1800, duration: '90 mins' },
          { id: 303, name: 'AC Installation / Uninstallation', price: 1200, duration: '120 mins' }
        ],
        workingHours: 'Mon - Sun: 09:00 AM - 09:00 PM',
        availableSlots: ['09:30 AM', '11:30 AM', '02:00 PM', '04:00 PM', '07:00 PM'],
        reviews: [
          { id: 4, customerName: 'Rohit Mehta', rating: 5, date: '2026-11-12', comment: 'Cooling was instantly restored. Sunil brought pressure washer tools and kept the room spotless.' }
        ]
      },
      {
        id: 4,
        userId: 5,
        name: 'Maria Santos',
        businessName: 'SparkleClean Home Care',
        category: 'Cleaner',
        categoryId: 4,
        city: 'Westside',
        serviceArea: 'Westside, Downtown, Bayview',
        experience: '5 years',
        experienceYears: 5,
        rating: 4.75,
        reviewsCount: 29,
        startingPrice: 450,
        isVerified: true,
        isAvailable: true,
        avatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150',
        bio: 'Residential deep cleaning specialist offering kitchen sanitization, sofa shampooing, bathroom scrubbing, and post-renovation cleanup.',
        services: [
          { id: 401, name: 'Full Bathroom Deep Scrubbing', price: 450, duration: '60 mins' },
          { id: 402, name: 'Kitchen Chimney & Counter Degreasing', price: 700, duration: '90 mins' },
          { id: 403, name: 'Sofa & Upholstery Shampooing', price: 900, duration: '120 mins' }
        ],
        workingHours: 'Mon - Sat: 08:30 AM - 06:30 PM',
        availableSlots: ['09:00 AM', '01:00 PM', '04:00 PM'],
        reviews: [
          { id: 5, customerName: 'Sanjay Kapoor', rating: 5, date: '2026-10-19', comment: 'Thorough cleaning. The kitchen looks completely brand new.' }
        ]
      },
      {
        id: 5,
        userId: 6,
        name: 'Arun Kumar',
        businessName: 'MasterCraft Woodworks',
        category: 'Carpenter',
        categoryId: 5,
        city: 'Downtown',
        serviceArea: 'Downtown, Northside, Eastside',
        experience: '12 years',
        experienceYears: 12,
        rating: 4.85,
        reviewsCount: 51,
        startingPrice: 400,
        isVerified: true,
        isAvailable: true,
        avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150',
        bio: 'Expert carpenter for furniture restoration, door hinge repairs, modular wardrobe assemblies, sliding door fixes and custom shelves.',
        services: [
          { id: 501, name: 'Door Lock & Latch Replacement', price: 400, duration: '40 mins' },
          { id: 502, name: 'Furniture Assembly & Hinge Realignment', price: 500, duration: '60 mins' },
          { id: 503, name: 'Custom Wooden Shelf / Cabinet Setup', price: 1100, duration: '120 mins' }
        ],
        workingHours: 'Mon - Sat: 09:00 AM - 07:00 PM',
        availableSlots: ['10:00 AM', '01:30 PM', '04:30 PM'],
        reviews: []
      },
      {
        id: 6,
        userId: 7,
        name: 'Kiran Rao',
        businessName: 'Precision Auto Diagnostics',
        category: 'Mechanic',
        categoryId: 3,
        city: 'Eastside',
        serviceArea: 'Eastside, Highway Belt, South Core',
        experience: '7 years',
        experienceYears: 7,
        rating: 4.7,
        reviewsCount: 22,
        startingPrice: 450,
        isVerified: true,
        isAvailable: true,
        avatar: 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150',
        bio: 'Doorstep automobile mechanic for car battery diagnostics, brake pad inspection, engine oil swap, and emergency roadside inspection.',
        services: [
          { id: 601, name: 'Doorstep Battery Jump & Health Check', price: 450, duration: '30 mins' },
          { id: 602, name: 'Brake Inspection & Fluid Replacement', price: 750, duration: '60 mins' },
          { id: 603, name: 'General Vehicle Safety Inspection', price: 600, duration: '60 mins' }
        ],
        workingHours: 'Mon - Sun: 08:00 AM - 08:00 PM',
        availableSlots: ['09:00 AM', '11:00 AM', '02:30 PM', '05:00 PM'],
        reviews: []
      }
    ],

    bookings: [
      {
        id: 1,
        reference: 'SC-89214',
        customerId: 1,
        customerName: 'Anita Desai',
        customerPhone: '+91 98765 43210',
        providerId: 1,
        providerName: 'Rajesh Sharma',
        providerBusiness: 'Sharma Spark Electricals',
        serviceName: 'Switchboard Repair & Wiring Check',
        bookingDate: '2026-11-20',
        timeSlot: '11:00 AM',
        status: 'ACCEPTED',
        address: 'Flat 402, Green Valley Apts, Downtown',
        problemDescription: 'Master bedroom main switch is sparking when turning on AC.',
        price: 350,
        createdAt: '2026-11-18 10:30'
      },
      {
        id: 2,
        reference: 'SC-67312',
        customerId: 1,
        customerName: 'Anita Desai',
        customerPhone: '+91 98765 43210',
        providerId: 2,
        providerName: 'David Miller',
        providerBusiness: 'Miller Pro Plumbing Solutions',
        serviceName: 'Water Pipe Leakage Repair',
        bookingDate: '2026-11-19',
        timeSlot: '03:30 PM',
        status: 'PENDING',
        address: 'Flat 402, Green Valley Apts, Downtown',
        problemDescription: 'Continuous dripping under the kitchen sink.',
        price: 400,
        createdAt: '2026-11-18 12:15'
      },
      {
        id: 3,
        reference: 'SC-44109',
        customerId: 1,
        customerName: 'Anita Desai',
        customerPhone: '+91 98765 43210',
        providerId: 3,
        providerName: 'Sunil Verma',
        providerBusiness: 'FrostAir AC Technicians',
        serviceName: 'AC Deep Foam Jet Cleaning',
        bookingDate: '2026-11-10',
        timeSlot: '02:00 PM',
        status: 'COMPLETED',
        address: 'Flat 402, Green Valley Apts, Downtown',
        problemDescription: 'Pre-summer deep cleaning for living room split AC.',
        price: 550,
        hasReviewed: true,
        createdAt: '2026-11-09 09:00'
      }
    ],

    addresses: [
      { id: 1, userId: 1, label: 'Home', addressLine1: 'Flat 402, Green Valley Apts', city: 'Downtown', postalCode: '500001', isDefault: true },
      { id: 2, userId: 1, label: 'Office', addressLine1: 'Suite 305, Tech Hub Tower', city: 'Metro Core', postalCode: '500081', isDefault: false }
    ],

    chatMessages: [
      { id: 1, bookingId: 1, senderId: 1, senderName: 'Anita Desai', message: 'Hello Rajesh, can you arrive around 11:15 AM?', createdAt: '2026-11-20 09:15' },
      { id: 2, bookingId: 1, senderId: 2, senderName: 'Rajesh Sharma', message: 'Sure Anita! I am on the way with replacement fuses and multimeter.', createdAt: '2026-11-20 09:20' }
    ],

    favourites: [1, 3]
  },

  // -------------------------------------------------------------
  // Real-Time Event Stream (SSE) Integration
  // -------------------------------------------------------------
  eventSource: null,
  subscribers: [],

  subscribeToLiveNotifications(userId, callback) {
    if (callback) this.subscribers.push(callback);
    if (this.eventSource) return;

    if (window.EventSource) {
      try {
        const sseUrl = `${API_BASE_URL}/notifications/subscribe/${userId || 1}`;
        this.eventSource = new EventSource(sseUrl);

        this.eventSource.addEventListener('CONNECTED', (e) => {
          console.log('⚡ SSE Connected:', e.data);
        });

        this.eventSource.addEventListener('BOOKING_UPDATE', (e) => {
          const payload = JSON.parse(e.data);
          this.notifySubscribers('BOOKING_UPDATE', payload);
        });

        this.eventSource.addEventListener('NEW_BOOKING_REQUEST', (e) => {
          const payload = JSON.parse(e.data);
          this.notifySubscribers('NEW_BOOKING_REQUEST', payload);
        });

        this.eventSource.addEventListener('CHAT_MESSAGE', (e) => {
          const payload = JSON.parse(e.data);
          this.notifySubscribers('CHAT_MESSAGE', payload);
        });

        this.eventSource.onerror = () => {
          if (this.eventSource) {
            this.eventSource.close();
            this.eventSource = null;
          }
        };
      } catch (e) {
        console.warn('EventSource initialization failed:', e);
      }
    }
  },

  notifySubscribers(eventType, data) {
    this.subscribers.forEach(cb => {
      try { cb(eventType, data); } catch (err) { console.error(err); }
    });
    window.dispatchEvent(new CustomEvent('serviceconnect:notification', { detail: { eventType, data } }));
  },

  async getChatMessages(bookingId) {
    return this.request(`/chat/booking/${bookingId}`);
  },

  async sendChatMessage(bookingId, senderId, senderName, message) {
    const res = await this.request(`/chat/booking/${bookingId}`, {
      method: 'POST',
      body: JSON.stringify({ senderId, senderName, message })
    });
    this.notifySubscribers('CHAT_MESSAGE', { bookingId, senderId, senderName, message, timestamp: Date.now() });
    return res;
  },

  async deleteBooking(id) {
    return this.request(`/bookings/${id}`, { method: 'DELETE' });
  },

  async clearProviderHistory(providerId) {
    return this.request(`/bookings/provider/${providerId}/history`, { method: 'DELETE' });
  },

  async clearCustomerHistory(customerId) {
    return this.request(`/bookings/customer/${customerId}/history`, { method: 'DELETE' });
  },

  async getBookingLocation(bookingId) {
    return this.request(`/bookings/${bookingId}/location`);
  },

  async updateBookingLocation(bookingId, locationData) {
    return this.request(`/bookings/${bookingId}/location`, {
      method: 'POST',
      body: JSON.stringify(locationData)
    });
  },

  // Fallback Mock API Handler
  async mockHandler(endpoint, options) {
    const method = (options.method || 'GET').toUpperCase();
    const [path, queryString] = endpoint.split('?');
    const params = new URLSearchParams(queryString || '');

    // Categories
    if (path === '/categories' && method === 'GET') {
      return { success: true, data: this.mockData.categories };
    }

    // Providers
    if (path === '/providers' && method === 'GET') {
      let result = [...this.mockData.providers];
      const category = params.get('category');
      const city = params.get('city');
      const maxPrice = params.get('maxPrice');
      const minRating = params.get('minRating');
      const sortBy = params.get('sortBy');

      if (category) {
        result = result.filter(p => p.category.toLowerCase().includes(category.toLowerCase()));
      }
      if (city) {
        result = result.filter(p => p.city.toLowerCase().includes(city.toLowerCase()) || p.serviceArea.toLowerCase().includes(city.toLowerCase()));
      }
      if (maxPrice) {
        result = result.filter(p => p.startingPrice <= Number(maxPrice));
      }
      if (minRating) {
        result = result.filter(p => p.rating >= Number(minRating));
      }
      if (sortBy === 'price_asc') {
        result.sort((a, b) => a.startingPrice - b.startingPrice);
      } else if (sortBy === 'price_desc') {
        result.sort((a, b) => b.startingPrice - a.startingPrice);
      } else if (sortBy === 'rating_desc') {
        result.sort((a, b) => b.rating - a.rating);
      }

      return { success: true, count: result.length, data: result };
    }

    // Single Provider
    if (path.startsWith('/providers/') && method === 'GET') {
      const id = Number(path.split('/')[2]);
      const provider = this.mockData.providers.find(p => p.id === id);
      if (provider) return { success: true, data: provider };
      return { success: false, message: 'Provider not found' };
    }

    // Bookings - List
    if (path === '/bookings' && method === 'GET') {
      return { success: true, data: this.mockData.bookings };
    }

    // Bookings - Create
    if (path === '/bookings' && method === 'POST') {
      const body = JSON.parse(options.body || '{}');
      const provider = this.mockData.providers.find(p => p.id === Number(body.providerId)) || this.mockData.providers[0];
      const newBooking = {
        id: this.mockData.bookings.length + 1,
        reference: 'SC-' + Math.floor(10000 + Math.random() * 90000),
        customerId: body.customerId || 1,
        customerName: body.customerName || 'Anita Desai',
        customerPhone: body.customerPhone || '+91 98765 43210',
        providerId: provider.id,
        providerName: provider.name,
        providerBusiness: provider.businessName,
        serviceName: body.serviceName || 'Standard Service',
        bookingDate: body.bookingDate || new Date().toISOString().split('T')[0],
        timeSlot: body.timeSlot || '10:00 AM',
        status: 'PENDING',
        address: body.address || 'Standard Address',
        problemDescription: body.problemDescription || '',
        price: body.price || provider.startingPrice,
        createdAt: new Date().toISOString().replace('T', ' ').slice(0, 16)
      };
      this.mockData.bookings.unshift(newBooking);
      return { success: true, message: 'Booking submitted successfully', data: newBooking };
    }

    // Bookings - Status update (Accept, Reject, Progress, Complete, Cancel)
    if (path.startsWith('/bookings/') && path.endsWith('/status') && method === 'PUT') {
      const parts = path.split('/');
      const id = Number(parts[2]);
      const body = JSON.parse(options.body || '{}');
      const booking = this.mockData.bookings.find(b => b.id === id);
      if (booking) {
        booking.status = body.status;
        return { success: true, message: `Booking status updated to ${body.status}`, data: booking };
      }
      return { success: false, message: 'Booking not found' };
    }

    // Bookings - Delete single booking
    if (path.startsWith('/bookings/') && method === 'DELETE' && !path.includes('/history')) {
      const id = Number(path.split('/')[2]);
      this.mockData.bookings = this.mockData.bookings.filter(b => b.id !== id);
      return { success: true, message: 'Booking record removed successfully' };
    }

    // Bookings - Clear Provider history
    if (path.startsWith('/bookings/provider/') && path.endsWith('/history') && method === 'DELETE') {
      const provId = Number(path.split('/')[3]);
      const before = this.mockData.bookings.length;
      this.mockData.bookings = this.mockData.bookings.filter(b => !(b.providerId === provId && ['COMPLETED', 'CANCELLED', 'REJECTED'].includes(b.status)));
      const count = before - this.mockData.bookings.length;
      return { success: true, message: `Cleared ${count} past records`, data: count };
    }

    // Bookings - Clear Customer history
    if (path.startsWith('/bookings/customer/') && path.endsWith('/history') && method === 'DELETE') {
      const custId = Number(path.split('/')[3]);
      const before = this.mockData.bookings.length;
      this.mockData.bookings = this.mockData.bookings.filter(b => !(b.customerId === custId && ['COMPLETED', 'CANCELLED', 'REJECTED'].includes(b.status)));
      const count = before - this.mockData.bookings.length;
      return { success: true, message: `Cleared ${count} past records`, data: count };
    }

    // Live GPS Location - GET
    if (path.startsWith('/bookings/') && path.endsWith('/location') && method === 'GET') {
      const id = Number(path.split('/')[2]);
      const booking = this.mockData.bookings.find(b => b.id === id) || { id, status: 'IN_PROGRESS' };
      const baseLat = 17.3850 + (id % 10) * 0.005;
      const baseLng = 78.4867 + (id % 10) * 0.005;
      const isCompleted = booking.status === 'COMPLETED';
      return {
        success: true,
        data: {
          bookingId: id,
          providerLat: isCompleted ? baseLat : baseLat + 0.0125,
          providerLng: isCompleted ? baseLng : baseLng - 0.0110,
          customerLat: baseLat,
          customerLng: baseLng,
          distanceKm: isCompleted ? 0.0 : 1.8,
          etaMinutes: isCompleted ? 0 : 8,
          statusText: isCompleted ? 'ARRIVED' : 'EN_ROUTE'
        }
      };
    }

    // Live GPS Location - POST update
    if (path.startsWith('/bookings/') && path.endsWith('/location') && method === 'POST') {
      const id = Number(path.split('/')[2]);
      const body = JSON.parse(options.body || '{}');
      this.notifySubscribers('LIVE_GPS_UPDATE', { bookingId: id, ...body });
      return { success: true, message: 'GPS coordinates updated', data: body };
    }

    // Reviews - Create
    if (path === '/reviews' && method === 'POST') {
      const body = JSON.parse(options.body || '{}');
      const booking = this.mockData.bookings.find(b => b.id === Number(body.bookingId));
      if (booking) {
        booking.hasReviewed = true;
      }
      const provider = this.mockData.providers.find(p => p.id === Number(body.providerId));
      if (provider) {
        provider.reviews.unshift({
          id: Date.now(),
          customerName: body.customerName || 'Anita Desai',
          rating: Number(body.rating) || 5,
          date: new Date().toISOString().split('T')[0],
          comment: body.comment || 'Great service!'
        });
        provider.reviewsCount += 1;
      }
      return { success: true, message: 'Review submitted successfully' };
    }

    // Real-Time Chat - Messages List
    if (path.startsWith('/chat/booking/') && method === 'GET') {
      const bId = Number(path.split('/')[3]);
      const list = this.mockData.chatMessages.filter(m => m.bookingId === bId);
      return { success: true, data: list };
    }

    // Real-Time Chat - Send Message
    if (path.startsWith('/chat/booking/') && method === 'POST') {
      const bId = Number(path.split('/')[3]);
      const body = JSON.parse(options.body || '{}');
      const newMsg = {
        id: Date.now(),
        bookingId: bId,
        senderId: body.senderId || 1,
        senderName: body.senderName || 'Anonymous',
        message: body.message || '',
        createdAt: new Date().toISOString().replace('T', ' ').slice(0, 16)
      };
      this.mockData.chatMessages.push(newMsg);
      return { success: true, data: newMsg };
    }

    // Customer Addresses
    if (path === '/customer/addresses' && method === 'GET') {
      return { success: true, data: this.mockData.addresses };
    }

    // Admin Metrics
    if (path === '/admin/stats' && method === 'GET') {
      return {
        success: true,
        data: {
          totalCustomers: 142,
          totalProviders: 38,
          totalServices: 64,
          totalBookings: 289,
          pendingApprovals: 4,
          recentRevenue: 42600
        }
      };
    }

    // Fallback default
    return { success: true, message: 'Operation simulated successfully' };
  }
};
