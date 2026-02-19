const EventSource = require('eventsource').EventSource || require('eventsource');
const axios = require('axios');

const SSE_URL = 'http://127.0.0.1:9091/sse';
const MESSAGE_URL = 'http://127.0.0.1:9091/mcp/message';

// Auth credentials
const authHash = Buffer.from('client-01:ecom-secret-key-123').toString('base64');
const headers = { 'Authorization': `Basic ${authHash}` };

console.error(`Connecting to SSE stream at ${SSE_URL}...`);
console.error(`Using Authorization: Basic ${authHash}`);

const eventSource = new EventSource(SSE_URL, { headers: headers });

eventSource.onopen = () => {
    console.error('Connected to SSE stream.');
};

eventSource.onmessage = (event) => {
    try {
        // SSE data is typically the JSON-RPC message
        const data = JSON.parse(event.data);
        // Write to stdout (for Claude)
        console.log(JSON.stringify(data));
    } catch (error) {
        console.error('Failed to parse SSE message:', error);
    }
};

eventSource.onerror = (error) => {
    console.error('SSE connection error:', error);
    // Optional: Implement reconnect logic if critical
};

// Handle Standard Input (from Claude)
const readline = require('readline');
const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
    terminal: false
});

rl.on('line', async (line) => {
    if (!line.trim()) return;

    try {
        const message = JSON.parse(line);

        // Forward message to MCP Server via POST
        try {
            await axios.post(MESSAGE_URL, message, {
                headers: {
                    'Content-Type': 'application/json',
                    ...headers
                }
            });
        } catch (postError) {
            console.error('Failed to send message to MCP server:', postError.message);
        }
    } catch (parseError) {
        console.error('Invalid JSON received from stdin:', parseError);
    }
});

// Handle process termination
process.on('SIGINT', () => {
    eventSource.close();
    process.exit(0);
});
