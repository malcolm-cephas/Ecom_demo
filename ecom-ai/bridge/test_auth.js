const EventSourcePolyfill = require('eventsource').EventSource;
const axios = require('axios');
const fs = require('fs');

const tokenData = JSON.parse(fs.readFileSync('../../token_utf8.json', 'utf-8').replace(/^\uFEFF/, ''));
const token = tokenData.access_token;
const sseUrl = 'https://mcp-server-production-b3d5.up.railway.app/sse';

console.log(`Connecting to ${sseUrl} with Bearer token...`);

const es = new EventSourcePolyfill(sseUrl, {
    headers: {
        'Authorization': `Bearer ${token}`
    }
});

es.onmessage = (e) => {
    console.log('[SSE Message]', e.data);
};

es.addEventListener('endpoint', (e) => {
    console.log('[SSE Endpoint]', e.data);
});

es.onerror = (e) => {
    console.error('[SSE Error]', e);
};

es.onopen = () => {
    console.log('[SSE Open] Source connection established successfully!');
    setTimeout(() => {
        es.close();
        console.log('Test complete. Connection closed.');
    }, 5000);
};
