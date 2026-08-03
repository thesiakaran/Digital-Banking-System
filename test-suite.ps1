$baseUrl = "http://localhost:8080"
$ErrorActionPreference = "Continue"

function Write-Test {
    param([string]$name)
    Write-Host "`n================================================="
    Write-Host "TESTING: $name"
    Write-Host "================================================="
}

try {
    Write-Test "1. Health Check (API Gateway)"
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get -ErrorAction Stop
    Write-Host "Gateway Health: $($health.status)"

    Write-Test "2. Create Sender Account"
    $senderData = @{
        accountholdername = "Alice"
        email = "alice@example.com"
        phone = "123456"
        acctype = "SAVINGS"
        balance = 500000.0
    } | ConvertTo-Json
    
    $senderRes = Invoke-RestMethod -Uri "$baseUrl/api/accounts" -Method Post -Body $senderData -ContentType "application/json"
    $senderId = $senderRes.accountNumber
    Write-Host "Created Sender: $senderId with balance $($senderRes.balance)"

    Write-Test "3. Create Receiver Account"
    $receiverData = @{
        accountholdername = "Bob"
        email = "bob@example.com"
        phone = "654321"
        acctype = "SAVINGS"
        balance = 1000.0
    } | ConvertTo-Json
    
    $receiverRes = Invoke-RestMethod -Uri "$baseUrl/api/accounts" -Method Post -Body $receiverData -ContentType "application/json"
    $receiverId = $receiverRes.accountNumber
    Write-Host "Created Receiver: $receiverId"

    Write-Test "4. Valid Transaction (Normal Transfer)"
    $txnData = @{
        senderAccountNumber = $senderId
        receiverAccountNumber = $receiverId
        amount = 500.0
        type = "TRANSFER"
    } | ConvertTo-Json
    
    $txn1 = Invoke-RestMethod -Uri "$baseUrl/api/transactions" -Method Post -Body $txnData -ContentType "application/json"
    Write-Host "Transaction 1 Result: $($txn1.status)"

    Write-Test "5. Threshold Fraud Transaction (>$100,000)"
    $fraudTxnData = @{
        senderAccountNumber = $senderId
        receiverAccountNumber = $receiverId
        amount = 150000.0
        type = "TRANSFER"
    } | ConvertTo-Json
    
    try {
        $txn2 = Invoke-RestMethod -Uri "$baseUrl/api/transactions" -Method Post -Body $fraudTxnData -ContentType "application/json"
        Write-Host "Threshold Txn Result: $($txn2.status)"
    } catch {
        Write-Host "Threshold Txn Blocked! Error: $_"
    }

    Write-Test "6. Velocity Fraud & Rate Limiter Test (Rapid Fire)"
    for ($i = 1; $i -le 5; $i++) {
        try {
            $velRes = Invoke-RestMethod -Uri "$baseUrl/api/transactions" -Method Post -Body $txnData -ContentType "application/json"
            Write-Host "Rapid Txn $i Status: $($velRes.status)"
        } catch {
            Write-Host "Rapid Txn $i Failed/Blocked: $_"
        }
    }

    Write-Test "7. Fetch Fraud Alerts"
    Start-Sleep -Seconds 2 # wait for Kafka events to process
    $alerts = Invoke-RestMethod -Uri "$baseUrl/api/fraud/alerts" -Method Get
    Write-Host "Total Alerts Found: $($alerts.content.Count)"
    foreach ($a in $alerts.content) {
        Write-Host "Alert: $($a.reason) (Score: $($a.riskScore)) - Status: $($a.status)"
    }

} catch {
    Write-Host "TEST SUITE FAILED: $_"
}
