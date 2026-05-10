---
description: >
  Sincroniza los endpoints de la API con Postman descargando el spec OpenAPI
  y guardándolo en postman/market-api.json. Úsalo cuando el usuario diga cosas como:
  "actualiza Postman", "sincroniza los endpoints", "exporta la API a Postman",
  "genera la colección de Postman", "mi colección está desactualizada", etc.
  Acepta argumento opcional "import" para subir directamente a Postman via API.
allowed-tools: PowerShell
---

## Instrucciones

1. Ejecuta el script con PowerShell:
   ```
   .\scripts\sync-postman.ps1 $ARGUMENTS
   ```

2. Si la app no está corriendo (error de conexión), indica al usuario que debe arrancarla primero:
   - Con Docker: `docker-compose up -d`
   - La app corre en `http://localhost:8080`

3. Muestra el resultado:
   - Modo `save` (por defecto): confirma que se guardó en `postman/market-api.json` y recuerda que pueden arrastrarlo a Postman para importarlo
   - Modo `import`: confirma que se creó la colección en Postman con el nombre y uid devueltos
   - Si falta `POSTMAN_API_KEY` para modo `import`, indica cómo definirla:
     ```powershell
     $env:POSTMAN_API_KEY = "tu-api-key"
     ```
     (La key se obtiene en Postman → Account Settings → API Keys)
