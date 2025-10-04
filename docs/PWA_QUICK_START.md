# 📱 PWA Quick Start Guide - Mobile-Ready Janitorr

## 🎯 Overview

This guide shows how to convert the existing Janitorr Management UI into a **Progressive Web App (PWA)** that can be installed on mobile devices (iOS/Android) with minimal cost and effort.

## ✨ Benefits of PWA Approach

### Advantages
- ✅ **Quick Implementation**: 1-2 weeks instead of 3-4 months
- ✅ **Low Cost**: $5k-$10k instead of $50k-$70k
- ✅ **Single Codebase**: Reuse existing Management UI
- ✅ **No App Stores**: Direct installation from browser
- ✅ **Instant Updates**: No app store approval needed
- ✅ **Offline Support**: Works without internet connection
- ✅ **Mobile Responsive**: Already built-in to current UI

### Limitations
- ❌ **No Native Widgets**: Can't add home screen widgets
- ❌ **Limited iOS Push**: Push notifications limited on iOS Safari
- ❌ **No Deep OS Integration**: No biometric auth, no Siri shortcuts
- ❌ **Reduced Discovery**: Not in App Store search

## 📋 Implementation Checklist

### Phase 1: Basic PWA Setup (Week 1)
- [ ] Create Web App Manifest
- [ ] Create Service Worker
- [ ] Add PWA meta tags
- [ ] Add app icons
- [ ] Test installation on Android
- [ ] Test installation on iOS

### Phase 2: Advanced Features (Week 2)
- [ ] Implement offline caching
- [ ] Add push notification support (Android/Desktop)
- [ ] Create install prompt
- [ ] Add splash screens
- [ ] Optimize for mobile performance
- [ ] Add to home screen instructions

## 🔧 Implementation Steps

### Step 1: Create Web App Manifest

**File**: `src/main/resources/static/manifest.json`

```json
{
  "name": "Janitorr - Media Library Cleanup",
  "short_name": "Janitorr",
  "description": "Manage and clean up your media library",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#1a1a1a",
  "theme_color": "#4a90e2",
  "orientation": "portrait-primary",
  "scope": "/",
  "icons": [
    {
      "src": "/icons/icon-72.png",
      "sizes": "72x72",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-96.png",
      "sizes": "96x96",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-128.png",
      "sizes": "128x128",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-144.png",
      "sizes": "144x144",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-152.png",
      "sizes": "152x152",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-192.png",
      "sizes": "192x192",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-384.png",
      "sizes": "384x384",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-512.png",
      "sizes": "512x512",
      "type": "image/png",
      "purpose": "any maskable"
    }
  ],
  "screenshots": [
    {
      "src": "/screenshots/mobile-dashboard.png",
      "sizes": "390x844",
      "type": "image/png",
      "form_factor": "narrow"
    },
    {
      "src": "/screenshots/desktop-dashboard.png",
      "sizes": "1920x1080",
      "type": "image/png",
      "form_factor": "wide"
    }
  ],
  "shortcuts": [
    {
      "name": "Trigger Media Cleanup",
      "short_name": "Media Cleanup",
      "description": "Run media cleanup immediately",
      "url": "/?action=cleanup-media",
      "icons": [
        {
          "src": "/icons/cleanup-media.png",
          "sizes": "96x96"
        }
      ]
    },
    {
      "name": "View Status",
      "short_name": "Status",
      "description": "Check system status",
      "url": "/?action=view-status",
      "icons": [
        {
          "src": "/icons/status.png",
          "sizes": "96x96"
        }
      ]
    }
  ]
}
```

### Step 2: Create Service Worker

**File**: `src/main/resources/static/service-worker.js`

```javascript
const CACHE_NAME = 'janitorr-v1';
const urlsToCache = [
  '/',
  '/index.html',
  '/styles.css',
  '/app.js',
  '/icons/icon-192.png',
  '/icons/icon-512.png'
];

// Install service worker and cache static assets
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        console.log('Opened cache');
        return cache.addAll(urlsToCache);
      })
  );
  // Force activation
  self.skipWaiting();
});

// Activate service worker and remove old caches
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          if (cacheName !== CACHE_NAME) {
            console.log('Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
  // Take control immediately
  return self.clients.claim();
});

// Fetch strategy: Network first, fallback to cache
self.addEventListener('fetch', event => {
  // Skip non-GET requests
  if (event.request.method !== 'GET') {
    return;
  }

  // Skip API requests (always go to network)
  if (event.request.url.includes('/api/')) {
    event.respondWith(fetch(event.request));
    return;
  }

  // For other requests: Network first, fallback to cache
  event.respondWith(
    fetch(event.request)
      .then(response => {
        // Clone response before caching
        const responseToCache = response.clone();
        caches.open(CACHE_NAME).then(cache => {
          cache.put(event.request, responseToCache);
        });
        return response;
      })
      .catch(() => {
        // Network failed, try cache
        return caches.match(event.request)
          .then(response => {
            if (response) {
              return response;
            }
            // Return offline page if nothing in cache
            return caches.match('/');
          });
      })
  );
});

// Push notification support
self.addEventListener('push', event => {
  const data = event.data ? event.data.json() : {};
  const title = data.title || 'Janitorr';
  const options = {
    body: data.body || 'Notification from Janitorr',
    icon: '/icons/icon-192.png',
    badge: '/icons/badge-72.png',
    tag: data.tag || 'janitorr-notification',
    data: data,
    actions: data.actions || []
  };

  event.waitUntil(
    self.registration.showNotification(title, options)
  );
});

// Notification click handler
self.addEventListener('notificationclick', event => {
  event.notification.close();

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true })
      .then(clientList => {
        // If app is already open, focus it
        for (const client of clientList) {
          if (client.url === '/' && 'focus' in client) {
            return client.focus();
          }
        }
        // Otherwise open new window
        if (clients.openWindow) {
          return clients.openWindow('/');
        }
      })
  );
});
```

### Step 3: Update index.html

**Modifications to**: `src/main/resources/static/index.html`

Add to `<head>` section:

```html
<!-- PWA Meta Tags -->
<meta name="mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
<meta name="apple-mobile-web-app-title" content="Janitorr">
<meta name="theme-color" content="#4a90e2">
<meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">

<!-- Manifest -->
<link rel="manifest" href="/manifest.json">

<!-- iOS Icons -->
<link rel="apple-touch-icon" sizes="180x180" href="/icons/apple-touch-icon.png">
<link rel="icon" type="image/png" sizes="32x32" href="/icons/favicon-32x32.png">
<link rel="icon" type="image/png" sizes="16x16" href="/icons/favicon-16x16.png">

<!-- iOS Splash Screens -->
<link rel="apple-touch-startup-image" href="/splash/iphone5_splash.png" media="(device-width: 320px) and (device-height: 568px) and (-webkit-device-pixel-ratio: 2)">
<link rel="apple-touch-startup-image" href="/splash/iphone6_splash.png" media="(device-width: 375px) and (device-height: 667px) and (-webkit-device-pixel-ratio: 2)">
<link rel="apple-touch-startup-image" href="/splash/iphoneplus_splash.png" media="(device-width: 621px) and (device-height: 1104px) and (-webkit-device-pixel-ratio: 3)">
<link rel="apple-touch-startup-image" href="/splash/iphonex_splash.png" media="(device-width: 375px) and (device-height: 812px) and (-webkit-device-pixel-ratio: 3)">
<link rel="apple-touch-startup-image" href="/splash/iphonexr_splash.png" media="(device-width: 414px) and (device-height: 896px) and (-webkit-device-pixel-ratio: 2)">
<link rel="apple-touch-startup-image" href="/splash/iphonexsmax_splash.png" media="(device-width: 414px) and (device-height: 896px) and (-webkit-device-pixel-ratio: 3)">
<link rel="apple-touch-startup-image" href="/splash/ipad_splash.png" media="(device-width: 768px) and (device-height: 1024px) and (-webkit-device-pixel-ratio: 2)">
<link rel="apple-touch-startup-image" href="/splash/ipadpro1_splash.png" media="(device-width: 834px) and (device-height: 1112px) and (-webkit-device-pixel-ratio: 2)">
<link rel="apple-touch-startup-image" href="/splash/ipadpro3_splash.png" media="(device-width: 834px) and (device-height: 1194px) and (-webkit-device-pixel-ratio: 2)">
<link rel="apple-touch-startup-image" href="/splash/ipadpro2_splash.png" media="(device-width: 1024px) and (device-height: 1366px) and (-webkit-device-pixel-ratio: 2)">
```

Add before closing `</body>` tag:

```html
<!-- Register Service Worker -->
<script>
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/service-worker.js')
      .then(registration => {
        console.log('Service Worker registered:', registration);
        
        // Check for updates periodically
        setInterval(() => {
          registration.update();
        }, 60000); // Check every minute
      })
      .catch(error => {
        console.error('Service Worker registration failed:', error);
      });
  });
}

// Install prompt
let deferredPrompt;
window.addEventListener('beforeinstallprompt', (e) => {
  // Prevent the mini-infobar from appearing on mobile
  e.preventDefault();
  // Stash the event so it can be triggered later
  deferredPrompt = e;
  // Show install button
  showInstallPromotion();
});

function showInstallPromotion() {
  // Create install button if not exists
  if (!document.getElementById('install-button')) {
    const installBtn = document.createElement('button');
    installBtn.id = 'install-button';
    installBtn.textContent = '📱 Install App';
    installBtn.className = 'install-button';
    installBtn.onclick = async () => {
      if (deferredPrompt) {
        deferredPrompt.prompt();
        const { outcome } = await deferredPrompt.userChoice;
        console.log(`User response to install prompt: ${outcome}`);
        deferredPrompt = null;
        installBtn.style.display = 'none';
      }
    };
    document.body.appendChild(installBtn);
  }
}

// Track installation
window.addEventListener('appinstalled', (evt) => {
  console.log('App installed successfully');
  // Hide install button
  const installBtn = document.getElementById('install-button');
  if (installBtn) {
    installBtn.style.display = 'none';
  }
});
</script>
```

### Step 4: Add PWA Styles

**Additions to**: `src/main/resources/static/styles.css`

```css
/* PWA Install Button */
.install-button {
    position: fixed;
    bottom: 20px;
    right: 20px;
    padding: 12px 24px;
    background: linear-gradient(135deg, #4a90e2 0%, #357abd 100%);
    color: white;
    border: none;
    border-radius: 25px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(74, 144, 226, 0.4);
    z-index: 1000;
    transition: all 0.3s ease;
}

.install-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(74, 144, 226, 0.5);
}

.install-button:active {
    transform: translateY(0);
}

/* iOS Safe Area Support */
@supports (padding: max(0px)) {
    body {
        padding-top: max(env(safe-area-inset-top), 0);
        padding-bottom: max(env(safe-area-inset-bottom), 0);
        padding-left: max(env(safe-area-inset-left), 0);
        padding-right: max(env(safe-area-inset-right), 0);
    }
}

/* Standalone App Adjustments */
@media all and (display-mode: standalone) {
    /* Hide browser UI elements when in standalone mode */
    .browser-only {
        display: none !important;
    }
    
    /* Add top padding for status bar */
    body {
        padding-top: env(safe-area-inset-top);
    }
}

/* Mobile-First Responsive Adjustments */
@media (max-width: 768px) {
    .install-button {
        bottom: 10px;
        right: 10px;
        padding: 10px 20px;
        font-size: 12px;
    }
    
    /* Make cards full-width on mobile */
    .card {
        margin: 10px 0;
    }
    
    /* Larger touch targets */
    button {
        min-height: 44px;
        min-width: 44px;
    }
}

/* Landscape Orientation */
@media (orientation: landscape) and (max-height: 500px) {
    .card-container {
        flex-direction: row;
        overflow-x: auto;
    }
}

/* Pull-to-Refresh Indicator */
.ptr-indicator {
    position: fixed;
    top: 0;
    left: 50%;
    transform: translateX(-50%);
    padding: 10px 20px;
    background: rgba(74, 144, 226, 0.9);
    color: white;
    border-radius: 0 0 10px 10px;
    font-size: 14px;
    z-index: 2000;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.ptr-indicator.active {
    opacity: 1;
}

/* Loading States */
.loading-skeleton {
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: loading 1.5s ease-in-out infinite;
}

@keyframes loading {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
}

/* Offline Indicator */
.offline-indicator {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    background: #ff6b6b;
    color: white;
    padding: 10px;
    text-align: center;
    font-weight: 600;
    z-index: 3000;
    transform: translateY(-100%);
    transition: transform 0.3s ease;
}

.offline-indicator.show {
    transform: translateY(0);
}
```

### Step 5: Add Offline Detection

**Additions to**: `src/main/resources/static/app.js`

```javascript
// Offline/Online Detection
let isOffline = !navigator.onLine;

function updateOnlineStatus() {
    const wasOffline = isOffline;
    isOffline = !navigator.onLine;
    
    if (wasOffline && !isOffline) {
        // Just came back online
        showOfflineIndicator(false);
        // Refresh data
        location.reload();
    } else if (!wasOffline && isOffline) {
        // Just went offline
        showOfflineIndicator(true);
    }
}

function showOfflineIndicator(show) {
    let indicator = document.getElementById('offline-indicator');
    if (!indicator) {
        indicator = document.createElement('div');
        indicator.id = 'offline-indicator';
        indicator.className = 'offline-indicator';
        indicator.textContent = '⚠️ You are offline. Some features may be limited.';
        document.body.appendChild(indicator);
    }
    
    if (show) {
        indicator.classList.add('show');
    } else {
        indicator.classList.remove('show');
    }
}

window.addEventListener('online', updateOnlineStatus);
window.addEventListener('offline', updateOnlineStatus);

// Initial check
updateOnlineStatus();

// Pull-to-Refresh Implementation
let startY = 0;
let currentY = 0;
let pulling = false;

document.addEventListener('touchstart', (e) => {
    if (window.scrollY === 0) {
        startY = e.touches[0].pageY;
        pulling = false;
    }
});

document.addEventListener('touchmove', (e) => {
    if (window.scrollY === 0) {
        currentY = e.touches[0].pageY;
        const pullDistance = currentY - startY;
        
        if (pullDistance > 80 && !pulling) {
            pulling = true;
            showPullToRefreshIndicator(true);
        }
    }
});

document.addEventListener('touchend', () => {
    if (pulling) {
        pulling = false;
        showPullToRefreshIndicator(false);
        // Reload page or refresh data
        location.reload();
    }
});

function showPullToRefreshIndicator(show) {
    let indicator = document.getElementById('ptr-indicator');
    if (!indicator) {
        indicator = document.createElement('div');
        indicator.id = 'ptr-indicator';
        indicator.className = 'ptr-indicator';
        indicator.textContent = '🔄 Release to refresh';
        document.body.appendChild(indicator);
    }
    
    if (show) {
        indicator.classList.add('active');
    } else {
        indicator.classList.remove('active');
    }
}

// Request Push Notification Permission (for Android/Desktop)
async function requestNotificationPermission() {
    if ('Notification' in window && 'serviceWorker' in navigator) {
        const permission = await Notification.requestPermission();
        if (permission === 'granted') {
            console.log('Notification permission granted');
            // Subscribe to push notifications
            subscribeUserToPush();
        }
    }
}

async function subscribeUserToPush() {
    try {
        const registration = await navigator.serviceWorker.ready;
        const subscription = await registration.pushManager.subscribe({
            userVisibleOnly: true,
            // Replace 'YOUR_PUBLIC_VAPID_KEY_HERE' with your actual public VAPID key.
            // To generate VAPID keys, use the 'web-push' npm package:
            //   npx web-push generate-vapid-keys
            // The public key goes here, and the private key is used on your server.
            // See: https://web-push-book.gauntface.com/chapter-04/01-generating-vapid-keys/
            applicationServerKey: urlBase64ToUint8Array('YOUR_PUBLIC_VAPID_KEY_HERE')
        });
        
        // Send subscription to server
        await fetch('/api/mobile/notifications/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(subscription)
        });
        
        console.log('Push subscription successful');
    } catch (error) {
        console.error('Push subscription failed:', error);
    }
}

function urlBase64ToUint8Array(base64String) {
    const padding = '='.repeat((4 - base64String.length % 4) % 4);
    const base64 = (base64String + padding)
        .replace(/\-/g, '+')
        .replace(/_/g, '/');
    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);
    for (let i = 0; i < rawData.length; ++i) {
        outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
}
```

## 📱 Icon Generation

### Required Icons
Generate icons in the following sizes from the existing Janitorr logo:

```
icons/
├── favicon-16x16.png
├── favicon-32x32.png
├── apple-touch-icon.png (180x180)
├── icon-72.png
├── icon-96.png
├── icon-128.png
├── icon-144.png
├── icon-152.png
├── icon-192.png
├── icon-384.png
└── icon-512.png
```

### Using ImageMagick (CLI)
```bash
# Convert your logo to different sizes
# Replace 'path/to/your/logo.png' with the actual path to your logo file (e.g., ../images/logos/janitorr_icon.png)
convert path/to/your/logo.png -resize 16x16 favicon-16x16.png
convert path/to/your/logo.png -resize 32x32 favicon-32x32.png
convert path/to/your/logo.png -resize 180x180 apple-touch-icon.png
convert path/to/your/logo.png -resize 72x72 icon-72.png
convert path/to/your/logo.png -resize 96x96 icon-96.png
convert path/to/your/logo.png -resize 128x128 icon-128.png
convert path/to/your/logo.png -resize 144x144 icon-144.png
convert path/to/your/logo.png -resize 152x152 icon-152.png
convert path/to/your/logo.png -resize 192x192 icon-192.png
convert path/to/your/logo.png -resize 384x384 icon-384.png
convert path/to/your/logo.png -resize 512x512 icon-512.png
```

## 🧪 Testing

### Android (Chrome)
1. Navigate to your Janitorr instance (http://localhost:8978)
2. Chrome will show "Add to Home Screen" banner
3. Tap "Add" to install
4. App appears on home screen
5. Launches in standalone mode (no browser UI)

### iOS (Safari)
1. Navigate to your Janitorr instance
2. Tap Share button
3. Tap "Add to Home Screen"
4. Enter name and tap "Add"
5. App appears on home screen
6. Launches in standalone mode

### Desktop (Chrome/Edge)
1. Navigate to your Janitorr instance
2. Click install icon in address bar
3. Click "Install"
4. App opens in standalone window

## 📊 Success Metrics

Track PWA adoption:
- Installation rate (% of users who install)
- Standalone usage (% of sessions in standalone mode)
- Offline usage (% of sessions that go offline)
- Return visits (how often users return)

## 🚀 Deployment

### No Backend Changes Required!
Since this is all static frontend files:
1. Add files to `src/main/resources/static/`
2. Build Janitorr normally: `./gradlew build`
3. Deploy as usual

The JVM image will automatically serve all static files.

## 📝 User Instructions

Add to documentation:

### Installing on Mobile

**Android:**
1. Open Chrome browser
2. Navigate to http://your-janitorr-server:8978
3. Tap the "Add Janitorr to Home screen" prompt
4. Tap "Install"
5. Janitorr now on your home screen!

**iOS:**
1. Open Safari browser
2. Navigate to http://your-janitorr-server:8978
3. Tap the Share button (square with arrow)
4. Scroll down and tap "Add to Home Screen"
5. Tap "Add"
6. Janitorr now on your home screen!

## ✅ Completion Checklist

Before considering PWA complete:
- [ ] manifest.json created and valid
- [ ] Service worker registered and caching
- [ ] All icons generated and optimized
- [ ] iOS meta tags added
- [ ] Install prompt working
- [ ] Offline mode functional
- [ ] Tested on Android Chrome
- [ ] Tested on iOS Safari
- [ ] Tested on Desktop Chrome
- [ ] Documentation updated
- [ ] User guide created

## 🎯 Next Steps After PWA

Once PWA is deployed and validated:
1. **Collect Metrics**: Usage, adoption rate, user feedback
2. **Iterate**: Fix issues, improve UX
3. **Decide**: Continue with PWA or invest in native app

If PWA is successful (>30% adoption), consider native app for:
- Home screen widgets
- Better push notifications on iOS
- Biometric authentication
- Deep OS integration
- App store discovery

---

**Estimated Implementation Time**: 1-2 weeks  
**Estimated Cost**: $5,000 - $10,000  
**ROI**: High (low cost, quick validation)  
**Risk**: Low (reversible, no breaking changes)
