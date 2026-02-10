const http = require('http');
const https = require('https');
const { URL } = require('url');
const EventSource = require('eventsource');

// Capture the SSE URL from the command line arguments
const targetUrl = process.argv[2];
if (!targetUrl) {
    console.error('Usage: node mcp-bridge.js <sse-url>');
    process.exit(1);
}

const url = new URL(targetUrl);
// Initialize the persistent SSE connection to the Spring Boot server
const es = new EventSource(targetUrl);

// This variable will store the unique HTTP endpoint provided by the server 
// for sending messages back (POST requests)
let endpointUrl = null;

es.onopen = () => {
    // Log to stderr because stdout is reserved for MCP JSON-RPC messages
    console.error(`Connected to SSE at ${targetUrl}`);
};

es.onerror = (err) => {
    console.error('SSE Error:', err);
};

// The Spring AI MCP server sends an 'endpoint' event upon connection.
// This event contains the URL where we should send our outgoing messages.
es.addEventListener('endpoint', (event) => {
    endpointUrl = new URL(event.data, url.origin).toString();
    console.error(`Received POST endpoint: ${endpointUrl}`);
});

// Any message received from the server via SSE is forwarded directly to Claude (stdout)
es.onmessage = (event) => {
    process.stdout.write(event.data + '\n');
};

// Any message received from Claude (stdin) is forwarded to the server via an HTTP POST request
process.stdin.on('data', (data) => {
    if (!endpointUrl) {
        // If we haven't received the callback endpoint yet, we can't send messages
        return;
    }

    const body = data.toString();
    const postUrl = new URL(endpointUrl);

    // Prepare the HTTP POST request configuration
    const options = {
        hostname: postUrl.hostname,
        port: postUrl.port,
        path: postUrl.pathname + postUrl.search,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(body)
        }
    };

    // Execute the POST request
    const req = (postUrl.protocol === 'https:' ? https : http).request(options, (res) => {
        // We don't need to do anything with the response for basic bridging
    });
    req.on('error', (e) => console.error(`POST error: ${e.message}`));
    req.write(body);
    req.end();
});

// Ensure the connection is closed gracefully on exit
process.on('SIGINT', () => {
    es.close();
    process.exit(0);
});
