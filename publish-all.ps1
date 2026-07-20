param(
    [string[]] $Branches = @(
    "26.1",
    "26.2"
),

    [string] $ReleaseBranch = "26.2",

    [switch] $DryRun
)

$ErrorActionPreference = "Stop"

function Get-GradleProperty
{
    param(
        [Parameter(Mandatory = $true)]
        [string] $Name
    )

    $gradlePropertiesPath = Join-Path $repositoryDirectory "gradle.properties"

    $line = Get-Content $gradlePropertiesPath |
            Where-Object {
                $_ -match "^\s*$([regex]::Escape($Name) )\s*="
            } |
            Select-Object -First 1

    if ($null -eq $line)
    {
        throw "Property '$Name' not found in gradle.properties."
    }

    return ($line -split "=", 2)[1].Trim()
}

function Invoke-Gradle
{
    param(
        [Parameter(Mandatory = $true)]
        [string[]] $Arguments
    )

    if ($DryRun)
    {
        Write-Host "DRY RUN: gradlew $( $Arguments -join ' ' )"
        return
    }

    & "$repositoryDirectory\gradlew.bat" @Arguments

    if ($LASTEXITCODE -ne 0)
    {
        throw "Gradle failed: gradlew $( $Arguments -join ' ' )"
    }
}

function Invoke-Git
{
    param(
        [Parameter(Mandatory = $true)]
        [string[]] $Arguments
    )

    if ($DryRun)
    {
        Write-Host "DRY RUN: git $( $Arguments -join ' ' )"
        return
    }

    & git -C $repositoryDirectory @Arguments

    if ($LASTEXITCODE -ne 0)
    {
        throw "Git failed: git $( $Arguments -join ' ' )"
    }
}

function Test-LocalGitTagExists
{
    param(
        [Parameter(Mandatory = $true)]
        [string] $TagName
    )

    if ($DryRun)
    {
        Write-Host "DRY RUN: git tag --list $TagName"
        return $false
    }

    $existingTag = git -C $repositoryDirectory tag --list $TagName

    if ($LASTEXITCODE -ne 0)
    {
        throw "Unable to check whether local tag '$TagName' exists."
    }

    return -not [string]::IsNullOrWhiteSpace($existingTag)
}

function Test-RemoteGitTagExists
{
    param(
        [Parameter(Mandatory = $true)]
        [string] $TagName
    )

    if ($DryRun)
    {
        Write-Host "DRY RUN: git ls-remote --exit-code --tags origin refs/tags/$TagName"
        return $false
    }

    git -C $repositoryDirectory ls-remote `
        --exit-code `
        --tags `
        origin `
        "refs/tags/$TagName" *> $null

    $exitCode = $LASTEXITCODE
    $global:LASTEXITCODE = 0

    switch ($exitCode)
    {
        0 {
            return $true
        }

        2 {
            return $false
        }

        default {
            throw "Unable to check whether remote tag '$TagName' exists. Git exited with code $exitCode."
        }
    }
}

$repositoryDirectory = $PSScriptRoot
$releaseDirectory = Join-Path $repositoryDirectory "release-jars"

$initialBranch = (
git -C $repositoryDirectory branch --show-current
).Trim()

if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($initialBranch))
{
    throw "Unable to determine the current Git branch."
}

function Invoke-Gradle
{
    param(
        [Parameter(Mandatory = $true)]
        [string[]] $Arguments
    )

    & "$repositoryDirectory\gradlew.bat" @Arguments

    if ($LASTEXITCODE -ne 0)
    {
        throw "Gradle failed: gradlew $( $Arguments -join ' ' )"
    }
}

try
{
    if (Test-Path $releaseDirectory)
    {
        Remove-Item $releaseDirectory -Recurse -Force
    }

    New-Item -ItemType Directory -Path $releaseDirectory | Out-Null

    foreach ($branch in $Branches)
    {
        Write-Host ""
        Write-Host "========================================"
        Write-Host "Publishing Minecraft branch: $branch"
        Write-Host "========================================"

        Invoke-Git @(
            "switch",
            $branch
        )

        if ($LASTEXITCODE -ne 0)
        {
            throw "Unable to switch to branch '$branch'."
        }

        Invoke-Gradle @(
            "clean",
            ":fabric:jar",
            ":neoforge:jar",
            "publishModrinthFabric",
            "publishCurseforgeFabric",
            "publishModrinthNeoForge",
            "publishCurseforgeNeoForge"
        )

        $fabricJars = Get-ChildItem `
            -Path "$repositoryDirectory\fabric\build\libs" `
            -Filter "*.jar" |
                Where-Object {
                    $_.Name -notmatch "-sources\.jar$" -and
                            $_.Name -notmatch "-dev\.jar$"
                }

        $neoForgeJars = Get-ChildItem `
            -Path "$repositoryDirectory\neoforge\build\libs" `
            -Filter "*.jar" |
                Where-Object {
                    $_.Name -notmatch "-sources\.jar$" -and
                            $_.Name -notmatch "-dev\.jar$"
                }

        if ($fabricJars.Count -ne 1)
        {
            throw "Expected exactly one Fabric JAR for branch '$branch', found $( $fabricJars.Count )."
        }

        if ($neoForgeJars.Count -ne 1)
        {
            throw "Expected exactly one NeoForge JAR for branch '$branch', found $( $neoForgeJars.Count )."
        }

        Copy-Item $fabricJars[0].FullName $releaseDirectory
        Copy-Item $neoForgeJars[0].FullName $releaseDirectory
    }

    Write-Host ""
    Write-Host "Preparing the final release..."

    Invoke-Git @(
        "switch",
        $ReleaseBranch
    )

    if ($LASTEXITCODE -ne 0)
    {
        throw "Unable to switch to release branch '$ReleaseBranch'."
    }

    $modVersion = (Get-GradleProperty "mod_version").Split("-", 2)[0]
    $tagName = "v$modVersion"

    Write-Host "Creating Git tag '$tagName' on branch '$ReleaseBranch'..."

    Invoke-Git @(
        "tag",
        "--list",
        $tagName
    )

    if (Test-LocalGitTagExists -TagName $tagName)
    {
        throw "The local tag '$tagName' already exists."
    }

    if (Test-RemoteGitTagExists -TagName $tagName)
    {
        throw "The remote tag '$tagName' already exists."
    }

    # ls-remote renvoie 2 quand le tag n'existe pas, ce qui est attendu.
    $global:LASTEXITCODE = 0

    Invoke-Git @(
        "tag",
        $tagName
    )

    if ($LASTEXITCODE -ne 0)
    {
        throw "Unable to create tag '$tagName'."
    }

    Invoke-Git @(
        "push",
        "origin",
        $tagName
    )

    if ($LASTEXITCODE -ne 0)
    {
        throw "Unable to push tag '$tagName'."
    }

    Write-Host ""
    Write-Host "Creating the single GitHub release..."

    Invoke-Gradle @(
        "publishGithub",
        "-Prelease_jars_dir=$releaseDirectory",
        "-Pgithub_tag=$tagName",
        "-Pgithub_commitish=$ReleaseBranch"
    )

    Write-Host ""
    Write-Host "Sending the Discord announcement..."

    Invoke-Gradle @(
        "announceDiscord"
    )

    if (Test-Path $releaseDirectory)
    {
        Remove-Item $releaseDirectory -Recurse -Force
    }

    Write-Host ""
    Write-Host "Release successfully published."
}
finally
{
    Write-Host ""
    Write-Host "Returning to branch '$initialBranch'..."

    git -C $repositoryDirectory switch $initialBranch
}