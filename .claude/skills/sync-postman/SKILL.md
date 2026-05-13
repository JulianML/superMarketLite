---
description: >
  Syncs API endpoints with Postman by downloading the OpenAPI spec
  and saving it to postman/market-api.json. Use it when the user says things like:
  "update Postman", "sync the endpoints", "export the API to Postman",
  "generate the Postman collection", "my collection is out of date", etc.
  Accepts optional "import" argument to upload directly to Postman via API.
allowed-tools: PowerShell
---

## Instructions

1. Run the script with PowerShell:
   ```
   .\scripts\sync-postman.ps1 $ARGUMENTS
   ```

2. If the app is not running (connection error), tell the user they need to start it first:
   - With Docker: `docker-compose up -d`
   - The app runs at `http://localhost:8080`

3. Show the result:
   - `save` mode (default): confirm it was saved to `postman/market-api.json` and remind them they can drag it into Postman to import it
   - `import` mode: confirm the collection was created in Postman with the returned name and uid
   - If `POSTMAN_API_KEY` is missing for `import` mode, explain how to set it:
     ```powershell
     $env:POSTMAN_API_KEY = "your-api-key"
     ```
     (The key is obtained in Postman → Account Settings → API Keys)
