const fs = require('fs');
const path = require('path');

const configPath = path.join(process.env.APPDATA, 'Claude', 'claude_desktop_config.json');

const config = {
    "mcpServers": {
        "ecom-railway": {
            "command": "node",
            "args": [
                "d:/Malcolm/DSCE/Internship/SENSEI/ecommerce/ecom-ai/mcp-server/mcp-bridge.js"
            ]
        }
    }
};

try {
    fs.writeFileSync(configPath, JSON.stringify(config, null, 2), 'utf8');
    console.log('SUCCESS: Config written to ' + configPath);
} catch (err) {
    console.error('ERROR: ' + err.message);
    process.exit(1);
}
