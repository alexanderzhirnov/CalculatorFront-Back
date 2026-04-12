$ErrorActionPreference = "Stop"
$base = "http://127.0.0.1:3000/api"

function Assert-True {
  param(
    [bool]$Condition,
    [string]$Message
  )

  if (-not $Condition) {
    throw $Message
  }
}

function Invoke-JsonRequest {
  param(
    [string]$Method,
    [string]$Path,
    [object]$Body = $null,
    [hashtable]$Headers = @{}
  )

  $params = @{
    Method = $Method
    Uri = "$base$Path"
    Headers = $Headers
  }

  if ($null -ne $Body) {
    $params["ContentType"] = "application/json"
    $params["Body"] = ($Body | ConvertTo-Json -Depth 10 -Compress)
  }

  Invoke-RestMethod @params
}

$suffix = Get-Date -Format "yyyyMMddHHmmss"
$login = "smoke_$suffix"
$password = "SmokePass123!"

$register = Invoke-JsonRequest -Method POST -Path "/auth/register" -Body @{
  login = $login
  password = $password
  firstName = "Smoke"
  lastName = "Test"
}

Assert-True ($register.login -eq $login) "Register returned unexpected login."
Assert-True (-not [string]::IsNullOrWhiteSpace($register.accessToken)) "Register did not return an access token."

$authHeaders = @{
  Authorization = "Bearer $($register.accessToken)"
}

$client = Invoke-JsonRequest -Method POST -Path "/clients" -Headers $authHeaders -Body @{
  lastName = "Check"
  firstName = "Smoke"
  phone = "+79990000000"
  email = "smoke+$suffix@example.test"
  address = "Smoke address 1"
}

Assert-True ($client.id -gt 0) "Client was not created."

$calculation = Invoke-JsonRequest -Method POST -Path "/calculations/client/$($client.id)" -Headers $authHeaders -Body @{
  constructionAddress = "Smoke object 1"
}

Assert-True ($calculation.id -gt 0) "Calculation was not created."

$frame = Invoke-JsonRequest -Method POST -Path "/calculations/$($calculation.id)/frame" -Headers $authHeaders -Body @{
  floors = 1
  floorHeight = 2.8
  perimeter = 48
  foundationArea = 120
  innerWallLength = 36
  extWallThickness = "MM_150"
  intWallThickness = "MM_100"
  ceilingThickness = "MM_200"
}

Assert-True ($frame.elementType -eq "FRAME") "Frame calculation failed."
Assert-True (($frame.resultItems | Measure-Object).Count -gt 0) "Frame calculation returned no materials."

$foundation = Invoke-JsonRequest -Method POST -Path "/calculations/$($calculation.id)/foundation" -Headers $authHeaders -Body @{
  externalPerimeter = 48
  innerWallLength = 36
}

Assert-True ($foundation.elementType -eq "FOUNDATION") "Foundation calculation failed."
Assert-True (($foundation.resultItems | Measure-Object).Count -gt 0) "Foundation calculation returned no materials."

$calculations = @(Invoke-JsonRequest -Method GET -Path "/calculations/client/$($client.id)" -Headers $authHeaders)
$targetCalculation = $calculations | Where-Object { $_.id -eq $calculation.id } | Select-Object -First 1

Assert-True ($null -ne $targetCalculation) "Created calculation was not found in history."
Assert-True (($targetCalculation.elements | Measure-Object).Count -ge 2) "Calculation history is missing computed elements."

$elementTypes = @($targetCalculation.elements | ForEach-Object { $_.elementType })
Assert-True ($elementTypes -contains "FRAME") "History is missing FRAME."
Assert-True ($elementTypes -contains "FOUNDATION") "History is missing FOUNDATION."

$support = Invoke-JsonRequest -Method POST -Path "/support/requests" -Body @{
  name = "Smoke Test"
  contact = "@smoke_test"
  message = "Support form smoke test"
  source = "smoke-script"
  page = "docker"
}

Assert-True ($support.accepted -eq $true) "Support request was not accepted."
Assert-True (@("log", "telegram") -contains $support.transport) "Support transport is unexpected."

[pscustomobject]@{
  login = $register.login
  clientId = $client.id
  calculationId = $calculation.id
  frameItems = (@($frame.resultItems)).Count
  foundationItems = (@($foundation.resultItems)).Count
  supportTransport = $support.transport
  totalElements = ($targetCalculation.elements | Measure-Object).Count
} | ConvertTo-Json -Depth 4
