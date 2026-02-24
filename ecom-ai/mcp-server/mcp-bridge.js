const axios = require('axios');
const EventSource = require('eventsource');
const fs = require('fs');
const path = require('path');
const https = require('https');

// Force SSL bypass
process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

const LOG_FILE = path.join(__dirname, 'mcp_bridge.log');
const BASE_URL = 'https://mcp-server-production-b3d5.up.railway.app';
const SSE_URL = `${BASE_URL}/sse`;

function log(msg) {
    const ts = new Date().toISOString();
    fs.appendFileSync(LOG_FILE, `[${ts}] ${msg}\n`);
}

log(`--- MCP Bridge v12 (Zero-Latency) Starting ---`);

let messageEndpoint = null;
let requestBuffer = [];
let eventSource = null;

const agent = new https.Agent({
    rejectUnauthorized: false,
    keepAlive: true,
    maxSockets: 100
});

function connect() {
    log(`Connecting to: ${SSE_URL}`);
    if (eventSource) eventSource.close();

    eventSource = new EventSource(SSE_URL, {
        https: { rejectUnauthorized: false },
        headers: {
            'Cache-Control': 'no-cache',
            'X-Accel-Buffering': 'no'
        }
    });

    eventSource.onopen = () => log('Stream Active.');

    eventSource.onmessage = (event) => {
        if (!event.data || !event.data.trim()) return;
        try {
            const msg = JSON.parse(event.data);
            if (msg.jsonrpc) {
                // High-speed write
                process.stdout.write(JSON.stringify(msg) + '\n');
                log(`<<< RECV: ${msg.id}`);
            }
        } catch (e) {
            log(`!!! Parse Error: ${e.message}`);
        }
    };

    eventSource.addEventListener('endpoint', (event) => {
        messageEndpoint = `${BASE_URL}${event.data.trim()}`;
        log(`### Endpoint Linked: ${messageEndpoint}`);
        while (requestBuffer.length > 0) send(requestBuffer.shift());
    });

    eventSource.onerror = (err) => {
        log(`!!! SSE Error: ${err.message || 'Signal Lost'}`);
        messageEndpoint = null;
    };
}

async function send(req) {
    if (!messageEndpoint) {
        log(`Queue: ${req.method} (${req.id})`);
        requestBuffer.push(req);
        return;
    }
    try {
        log(`>>> SEND: ${req.method} (${req.id})`);
        await axios.post(messageEndpoint, req, {
            headers: { 'Content-Type': 'application/json' },
            httpsAgent: agent,
            timeout: 60000
        });
        log(`>>> SENT: OK`);
    } catch (err) {
        log(`!!! POST FAIL: ${err.message}`);
    }
}

// Low-level fast stdin reader
let buf = '';
process.stdin.on('data', (chunk) => {
    buf += chunk.toString();
    let idx;
    while ((idx = buf.indexOf('\n')) >= 0) {
        const line = buf.substring(0, idx).trim();
        buf = buf.substring(idx + 1);
        if (line) {
            try {
                send(JSON.parse(line));
            } catch (e) {
                log(`!!! Stdin JSON Error: ${e.message}`);
            }
        }
    }
});

connect();
process.stdin.resume();
log('Bridge Ready.');
