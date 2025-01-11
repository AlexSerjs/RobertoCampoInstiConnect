self.addEventListener('install', function(event) {
  console.log('Service worker instalado.');
});

self.addEventListener('fetch', function(event) {
  // Puedes cachear recursos o controlar la respuesta a las solicitudes.
  event.respondWith(
    fetch(event.request).catch(() => caches.match(event.request))
  );
});
