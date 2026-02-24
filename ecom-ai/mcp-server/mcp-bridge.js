const axios = require('axios');
const EventSource = require('eventsource');
const fs = require('fs');
const path = require('path');
const https = require('https');

process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';
const LOG_FILE = path.join(__dirname, 'mcp_bridge.log');
const BASE_URL = 'https://mcp-server-production-b3d5.up.railway.app';
const SSE_URL = `${BASE_URL}/sse`;

function log(msg) {
    const ts = new Date().toISOString();
    fs.appendFileSync(LOG_FILE, `[${ts}] ${msg}\n`);
}

log(`--- MCP Bridge v13 (Large Payload Fix) Starting ---`);

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
            const dataStr = event.data.trim();
            // Verify if it's a complete JSON-RPC message
            if (dataStr.startsWith('{') && dataStr.endsWith('}')) {
                const message = JSON.parse(dataStr);
                if (message.jsonrpc) {
                    // Force synchronous write to stdout to avoid buffering
                    const output = JSON.stringify(message) + '\n';
                    fs.writeSync(1, output);
                    log(`<<< [RECV] ID: ${message.id} (Size: ${output.length} bytes)`);
                }
            } else {
                log(`!!! Incomplete data: ${dataStr.substring(0, 50)}...`);
            }
        } catch (e) {
            log(`!!! Parse Fail: ${e.message} | Data snippet: ${event.data.substring(0, 50)}`);
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
        log(`>>> [SEND] ${req.method} (${req.id})`);
        await axios.post(messageEndpoint, req, {
            headers: { 'Content-Type': 'application/json' },
            httpsAgent: agent,
            timeout: 60000
        });
        log(`>>> [SENT] OK`);
    } catch (err) {
        log(`!!! POST FAIL: ${err.message}`);
    }
}

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
            } catch (e) { }
        }
    }
});

connect();
process.stdin.resume();
log('Bridge v13 ready.');
