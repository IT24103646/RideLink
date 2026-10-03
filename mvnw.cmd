<# : batch portion
@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_PSMODULEP_SAVE=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir='%~dp0'; $script='%__MVNW_ARG0_NAME__%'; icm -ScriptBlock ([Scriptblock]::Create((Get-Content -Raw '%~f0'))) -NoNewScope}"`) DO @(
  IF "%%A"=="MVN_CMD" (set __MVNW_CMD__=%%B) ELSE IF "%%B"=="" (echo %%A) ELSE (echo %%A=%%B)
)
@SET PSModulePath=%__MVNW_PSMODULEP_SAVE%
@IF NOT "%__MVNW_CMD__%"=="" ("%__MVNW_CMD__%" %*)
@echo Cannot start maven from wrapper >&2 && exit /b 1
@GOTO :EOF
: end batch / begin powershell #>

$ErrorActionPreference = "Stop"
$distributionUrl = (Get-Content -Raw "$scriptDir/.mvn/wrapper/maven-wrapper.properties" | ConvertFrom-StringData).distributionUrl
$distributionUrlName = $distributionUrl -replace '^.*/',''
$distributionUrlNameMain = $distributionUrlName -replace '\.[^.]*$','' -replace '-bin$',''
$MAVEN_M2_PATH = if ($env:MAVEN_USER_HOME) { $env:MAVEN_USER_HOME } else { "$HOME/.m2" }
$MAVEN_HOME_PARENT = "$MAVEN_M2_PATH/wrapper/dists/$distributionUrlNameMain"
$MAVEN_HOME_NAME = ([System.Security.Cryptography.SHA256]::Create().ComputeHash([byte[]][char[]]$distributionUrl) | ForEach-Object {$_.ToString("x2")}) -join ''
$MAVEN_HOME = "$MAVEN_HOME_PARENT/$MAVEN_HOME_NAME"
if (Test-Path -Path "$MAVEN_HOME" -PathType Container) {
  Write-Output "MVN_CMD=$MAVEN_HOME/bin/mvn.cmd"
  exit $?
}
New-Item -Path $MAVEN_HOME_PARENT -ItemType Directory -Force | Out-Null
$TMP_DOWNLOAD_DIR = New-Item -Itemtype Directory -Path ([System.IO.Path]::GetTempPath() + [System.Guid]::NewGuid())
try {
  $archive = Join-Path $TMP_DOWNLOAD_DIR $distributionUrlName
  (New-Object System.Net.WebClient).DownloadFile($distributionUrl, $archive)
  Expand-Archive $archive -DestinationPath $TMP_DOWNLOAD_DIR | Out-Null
  Move-Item "$TMP_DOWNLOAD_DIR/$distributionUrlNameMain" $MAVEN_HOME
} finally {
  Remove-Item $TMP_DOWNLOAD_DIR -Recurse -Force -ErrorAction SilentlyContinue
}
Write-Output "MVN_CMD=$MAVEN_HOME/bin/mvn.cmd"
