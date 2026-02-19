const EventSource = require('eventsource');
const url = 'http://localhost:9091/sse';
console.log(`Connecting to ${url}...`);
const es = new EventSource(url);
es.onmessage = (e) => {
    console.log('Message:', e.data);
};
es.onerror = (e) => {
    console.error('Error:', e);
};
es.onopen = () => {
    console.log('Open');
};
