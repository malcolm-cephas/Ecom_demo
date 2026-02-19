const axios = require('axios');
const EventSource = require('eventsource');
const readline = require('readline');
const fs = require('fs');
const path = require('path');

const LOG_FILE = path.join(__dirname, 'mcp_bridge.log');

// Helper to log errors
function log(msg) {
    const timestamp = new Date().toISOString();
    const formatted = `[${timestamp}] ${msg}\n`;
    fs.appendFileSync(LOG_FILE, formatted);
    console.error(formatted);
}

// Connection Configuration
const BASE_URL = 'http://localhost:9091';
const SSE_URL = `${BASE_URL}/sse`;

log(`Starting MCP Bridge`);
log(`SSE URL: ${SSE_URL}`);

// Store the session-specific endpoint URL (received after handshake)
let messageEndpoint = null;
// Buffer requests if they come before the handshake is complete
let requestBuffer = [];

// Connect to the Spring Boot App via SSE (Server-Sent Events)
log('Attempting to connect to SSE stream...');
const eventSource = new EventSource(SSE_URL);

eventSource.onopen = () => {
    log('Connected to SSE server');
};

// Handle incoming messages from the Spring Server
eventSource.onmessage = (event) => {
    // This handles the DEFAULT "message" event type from SSE
    try {
        const message = JSON.parse(event.data);
        // If the message is a JSON-RPC response/request intended for Claude
        if (message.jsonrpc) {
            // Forward it to Standard Output (stdout). 
            // This is how we "talk" to Claude Desktop.
            process.stdout.write(JSON.stringify(message) + '\n');
            log(`Sent to Claude: ${JSON.stringify(message).substring(0, 100)}...`);
        }
    } catch (e) {
        log(`Failed to parse SSE message: ${e.message}`);
    }
};

// Listen for the 'endpoint' event which contains the session-specific URL
// Listen for the 'endpoint' event. 
// The Spring MCP server sends this special event to tell us where to POST data.
eventSource.addEventListener('endpoint', (event) => {
    const endpointPath = event.data.trim();
    messageEndpoint = `${BASE_URL}${endpointPath}`;
    log(`Received session endpoint: ${messageEndpoint}`);

    // If we had queued requests waiting for this handshake, send them now
    if (requestBuffer.length > 0) {
        log(`Processing ${requestBuffer.length} buffered requests...`);
        while (requestBuffer.length > 0) {
            const req = requestBuffer.shift();
            sendRequest(req);
        }
    }
});

eventSource.onerror = (err) => {
    // Basic error logging
    if (err) {
        log(`SSE Error: ${JSON.stringify(err)}`);
    }
};

// Function to send POST request
async function sendRequest(request) {
    if (!messageEndpoint) {
        log(`Buffering request: ${request.method} (ID: ${request.id}) - Waiting for endpoint`);
        requestBuffer.push(request);
        return;
    }

    try {
        log(`Sending POST to ${messageEndpoint}`);
        await axios.post(messageEndpoint, request, {
            headers: { 'Content-Type': 'application/json' }
        });
        log(`Forwarded request to server successfully.`);
    } catch (httpError) {
        log(`HTTP Error posting to server: ${httpError.message}`);
        if (httpError.response) {
            log(`Response Data: ${JSON.stringify(httpError.response.data)}`);
        }
    }
}

// Handle Incoming Standard Input (stdin) from Claude
// Claude sends JSON-RPC requests via stdin.
const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
    terminal: false
});

rl.on('line', (line) => {
    if (!line.trim()) return;

    try {
        const request = JSON.parse(line);
        log(`Received from Claude: ${request.method} (ID: ${request.id})`);
        // Translate this stdin message into an HTTP POST to Spring
        sendRequest(request);
    } catch (e) {
        log(`Failed to parse stdin line: ${e.message}`);
    }
});

log('Bridge running and listening on Stdio...');
process.stdin.resume();
