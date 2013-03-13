; installerInnoSetup.iss
;
; Copyright (c) 2009 John Stoner. All Rights Reserved.
;
; boogiepants-0.2.0-windows.exe setup program creator
; This script requires Inno setup available at http://www.jrsoftware.org/isinfo.php
; and a tmp directory stored in current directory containing :
;   a boogiepants.exe file built with launch4j
; + a jre... subdirectory containing a dump of Windows JRE without the files mentioned 
;   in the JRE README.TXT file (JRE bin/javaw.exe command excepted)     
; + a lib subdirectory containing boogiepants.jar and Windows Java 3D DLLs and JARs for Java 3D
; + file COPYING.TXT

[Setup]
AppName=boogiepants
AppVerName=boogiepants v0.2.0--proving the concept
AppPublisher=John Stoner
AppPublisherURL=http://boogiepants.typepad.com
AppSupportURL=http://boogiepants.sourceforge.net
AppUpdatesURL=http://boogiepants.sourceforge.net
DefaultDirName={pf}\boogiepants
DefaultGroupName=boogiepants
OutputDir=.
OutputBaseFilename=..\boogiepants-0.2.0-windows
Compression=lzma
SolidCompression=yes
ChangesAssociations=yes
VersionInfoVersion=0.2.0.0
VersionInfoTextVersion=0.2.0
VersionInfoDescription=boogiepants Setup
VersionInfoCopyright=Copyright (c) 2008-2009 John Stoner
VersionInfoCompany=John Stoner

[Dirs]
Name: "C:\users\{%USERNAME}\boogiepants"
Name: "C:\users\{%USERNAME}\boogiepants\pd"
Name: "C:\users\{%USERNAME}\boogiepants\resources"

[Files]
Source: "pd\*";  Destdir: "C:\users\{%USERNAME}\boogiepants\pd"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "resources\*";  Destdir: "C:\users\{%USERNAME}\boogiepants\resources"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "*"; DestDir: "{app}"; Excludes: "pd\*,resources\*"; Flags: ignoreversion recursesubdirs createallsubdirs

[Tasks]
Name: desktopicon; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Icons]
Name: "{group}\boogiepants"; Filename: "{app}\boogiepants.exe"; Comment: "{cm:boogiepantsComment}"
Name: "{group}\{cm:UninstallProgram,boogiepants}"; Filename: "{uninstallexe}"
Name: "{userdesktop}\boogiepants"; Filename: "{app}\boogiepants.exe"; Tasks: desktopicon; Comment: "{cm:boogiepantsComment}"

[UninstallDelete]
Type: filesandordirs; Name: "{app}\jre1.6.0_06\launch4j-tmp"

[CustomMessages]
boogiepantsComment=boogiepants do not dance to music, music dances to boogiepants!
OtherTasks=Other tasks:

[Registry]
Root: HKCR; Subkey: ".pants"; ValueType: string; ValueName: ""; ValueData: "boogiepants"; Flags: uninsdeletevalue
Root: HKCR; Subkey: " boogiepants"; ValueType: string; ValueName: ""; ValueData: "boogiepants"; Flags: uninsdeletekey
Root: HKCR; Subkey: " boogiepants\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\boogiepants.exe,0"
Root: HKCR; Subkey: " boogiepants\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\boogiepants.exe"" -open ""%1"""

