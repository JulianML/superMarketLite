param([string]$Mode = "save")

$ApiUrl  = "http://localhost:8080/v3/api-docs"
$OutDir  = "postman"
$OutFile = "$OutDir\market-api.json"

# 1. Obtener spec
try {
    $spec = Invoke-RestMethod -Uri $ApiUrl -Method Get -ErrorAction Stop
} catch {
    Write-Error "No se pudo conectar a $ApiUrl. Asegúrate de que la app esté corriendo."
    exit 1
}

# 2. Guardar a archivo
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$spec | ConvertTo-Json -Depth 20 | Set-Content $OutFile -Encoding utf8
Write-Host "OK  Spec guardada en $OutFile"

# 3. Importar a Postman si corresponde
if ($Mode -ne "import") { exit 0 }

$apiKey = $env:POSTMAN_API_KEY
if (-not $apiKey) {
    Write-Error "Para importar define la variable de entorno POSTMAN_API_KEY."
    exit 1
}

$body = @{ input = $spec; type = "json" } | ConvertTo-Json -Depth 20

try {
    $res = Invoke-RestMethod `
        -Uri     "https://api.getpostman.com/import/openapi" `
        -Method  Post `
        -Headers @{ "X-Api-Key" = $apiKey; "Content-Type" = "application/json" } `
        -Body    $body `
        -ErrorAction Stop

    $name = $res.collections[0].name
    $uid  = $res.collections[0].uid
    Write-Host "OK  Colección '$name' creada en Postman (uid: $uid)"
} catch {
    Write-Error "Error al importar a Postman: $_"
    exit 1
}
