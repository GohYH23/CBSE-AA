# run-vendor-tests.ps1 - Updated for correct path
Write-Host "????????????????????????????????????????????????????????" -ForegroundColor Cyan
Write-Host "?           VENDOR BUNDLE TEST RUNNER                 ?" -ForegroundColor Cyan
Write-Host "????????????????????????????????????????????????????????" -ForegroundColor Cyan
Write-Host ""

# Navigate to wif3006-cbse-osgi
Write-Host "Navigating to wif3006-cbse-osgi..." -ForegroundColor Yellow
cd wif3006-cbse-osgi

$currentDir = Get-Location
Write-Host "Current directory: $currentDir" -ForegroundColor Cyan

# Check for vendor-bundle
if (Test-Path "vendor-bundle") {
    Write-Host "? Found vendor-bundle directory" -ForegroundColor Green
    dir vendor-bundle -ErrorAction SilentlyContinue
} else {
    Write-Host "? vendor-bundle not found!" -ForegroundColor Red
    Write-Host "Available directories:" -ForegroundColor Yellow
    dir -Directory | ForEach-Object { Write-Host "  - $($_.Name)" }
    exit 1
}

# Check for pom.xml in vendor-bundle
if (Test-Path "vendor-bundle\pom.xml") {
    Write-Host "? Found vendor-bundle pom.xml" -ForegroundColor Green
} else {
    Write-Host "? vendor-bundle\pom.xml not found!" -ForegroundColor Red
}

# Check for test files
$testFile = "vendor-bundle\src\test\java\com\inventory\vendor\VendorServiceImplTest.java"
if (Test-Path $testFile) {
    Write-Host "? Found test file: VendorServiceImplTest.java" -ForegroundColor Green
} else {
    Write-Host "??  Test file not found: $testFile" -ForegroundColor Yellow
    Write-Host "Looking for other test files..." -ForegroundColor Cyan
    
    # Find any test files
    $testFiles = Get-ChildItem -Path "vendor-bundle" -Filter "*Test.java" -Recurse -ErrorAction SilentlyContinue
    if ($testFiles) {
        Write-Host "Found test files:" -ForegroundColor Green
        $testFiles | ForEach-Object { Write-Host "  - $($_.FullName)" }
    } else {
        Write-Host "No test files found!" -ForegroundColor Red
    }
}

# Check Maven
try {
    $mvnCheck = mvn -v 2>&1 | Out-Null
    Write-Host "? Maven is available" -ForegroundColor Green
} catch {
    Write-Host "??  Maven not found in PATH. Adding..." -ForegroundColor Yellow
    $env:Path += ";E:\Software\apache-maven-3.9.12-bin\bin"
    
    try {
        mvn -v 2>&1 | Out-Null
        Write-Host "? Maven added successfully" -ForegroundColor Green
    } catch {
        Write-Host "? Could not find Maven even after adding to PATH" -ForegroundColor Red
        Write-Host "Please install Maven from: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
        exit 1
    }
}

# Run tests
Write-Host "`nRunning vendor bundle tests..." -ForegroundColor Yellow
Write-Host "??????????????????????????????????????????????????????" -ForegroundColor DarkGray

# Change to vendor-bundle directory
cd vendor-bundle

# First, try to compile
Write-Host "Step 1: Compiling..." -ForegroundColor Cyan
mvn clean compile -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "? Compilation successful" -ForegroundColor Green
} else {
    Write-Host "? Compilation failed. Running with details:" -ForegroundColor Red
    mvn clean compile
    exit 1
}

# Run tests
Write-Host "`nStep 2: Running tests..." -ForegroundColor Cyan
$testOutput = mvn test 2>&1

# Display results
Write-Host "`nTest Results:" -ForegroundColor Yellow
Write-Host "??????????????????????????????????????????????????????" -ForegroundColor DarkGray

# Extract and display test summary
$testSummary = $testOutput | Select-String -Pattern "Tests run:|BUILD"

if ($testSummary) {
    foreach ($line in $testSummary) {
        if ($line -match "Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)") {
            $total = [int]$Matches[1]
            $failures = [int]$Matches[2]
            $errors = [int]$Matches[3]
            $skipped = [int]$Matches[4]
            $passed = $total - $failures - $errors
            
            Write-Host "Total Tests:   $total" -ForegroundColor White
            Write-Host "Tests Passed:  $passed" -ForegroundColor Green
            Write-Host "Tests Failed:  $failures" -ForegroundColor $(if ($failures -gt 0) { "Red" } else { "White" })
            Write-Host "Tests Errors:  $errors" -ForegroundColor $(if ($errors -gt 0) { "Red" } else { "White" })
            Write-Host "Tests Skipped: $skipped" -ForegroundColor Yellow
            
            if ($total -gt 0) {
                $successRate = ($passed * 100.0) / $total
                Write-Host "Success Rate:  $($successRate.ToString('0.00'))%" -ForegroundColor $(if ($successRate -ge 80) { "Green" } elseif ($successRate -ge 60) { "Yellow" } else { "Red" })
            }
        }
    }
}

# Check build status
if ($testOutput -match "BUILD SUCCESS") {
    Write-Host "`n? BUILD SUCCESSFUL" -ForegroundColor Green
} elseif ($testOutput -match "BUILD FAILURE") {
    Write-Host "`n? BUILD FAILED" -ForegroundColor Red
    
    # Show errors
    $errors = $testOutput | Select-String -Pattern "ERROR|FAILURE" -CaseSensitive:$false
    if ($errors) {
        Write-Host "`nErrors found:" -ForegroundColor Red
        $errors | Select-Object -First 5 | ForEach-Object {
            Write-Host "  $($_.ToString().Trim())" -ForegroundColor Red
        }
    }
}

Write-Host "`n? Test execution completed!" -ForegroundColor Green
