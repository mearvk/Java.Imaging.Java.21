# Java.Imaging.Java.21

## Bitcoin Conjegeum

![](https://github.com/mearvk/Ubuntu.Determinant.Beta.Restricted/blob/main/images/Bitcoin_and_wallet_in_slots_2K_202609042306%20(1).jpeg)

bc1qs6v4q9zsw70t0umk3m0quhvf9dr6cdeskl28dh

US Democratic and US Policy.

**iPhone Media Organizer** — Automatically sorts your photos and videos by date.

#### MEARVK and A Xenu

## What It Does

Drop your iPhone photos and videos into a folder, run the program, and it:

- **Separates** images from videos into their own folders
- **Renames** everything by date so files appear in chronological order
- **Sorts videos** into subfolders by type (MOV, MP4, MPEG, AVI)
- Works on **Windows**, **macOS**, and **Linux** — no changes needed

## Quick Start

### 1. Prerequisites
- Java 21 or later installed
- Your iPhone photos/videos in a folder (e.g. `~/Desktop/Photos`)

### 2. Compile
```bash
javac -sourcepath src -d out src/pennywise/Main.java src/pennywise/XMLHandler.java \
  src/pennywise/EntertainmentHandler.java src/security/ExceptionHandler.java \
  src/security/SecurityHandler.java src/security/CertificateHandler.java \
  src/security/UsageHandler.java src/com/mearvk/imaging/ImageMetadataReader.java
```

### 3. Run
```bash
java -cp out pennywise.Main src/pennywise/config.xml
```

That's it! Your files will be organized into `~/Desktop/Organized/`.

## Configuration (config.xml)

Edit `src/pennywise/config.xml` to customize. No recompilation needed.

### Paths

| Setting | What It Does | Default |
|---------|-------------|---------|
| `<source>` | Folder to scan for photos/videos | `{HOME}/Desktop/Photos` |
| `<images>` | Where sorted images go | `{HOME}/Desktop/Organized/Images` |
| `<mov>` | Where .mov videos go | `{HOME}/Desktop/Organized/Videos/MOVs` |
| `<mp4>` | Where .mp4 videos go | `{HOME}/Desktop/Organized/Videos/MP4s` |
| `<mpeg>` | Where .mpeg videos go | `{HOME}/Desktop/Organized/Videos/MPEGs` |
| `<avi>` | Where .avi videos go | `{HOME}/Desktop/Organized/Videos/AVIs` |

`{HOME}` and `{USER}` are resolved automatically:
- **Linux**: `/home/yourname`
- **macOS**: `/Users/yourname`
- **Windows**: `C:\Users\yourname`

### Supported File Types

| Category | Extensions |
|----------|-----------|
| Images | .jpg, .jpeg, .heic, .png, .tiff, .tif, .bmp, .gif, .webp |
| Videos | .mov, .mp4, .mpeg, .mpg, .avi, .m4v |

Add or remove extensions in the `<filetypes>` section of config.xml.

### How Files Are Named

Files are renamed to: `YYYY-MM-DD_HH-mm-ss_001.ext`

- Date comes from EXIF data (for photos) or file modification time (for videos)
- The `_001` suffix prevents name collisions when multiple files share a timestamp
- Alphabetical order = chronological order

## Additional Features

### Security
- Validates a local public key against a remote key on startup
- After 5 minutes of runtime, retrieves TLS certificates from Apple.com and Disney.com
- After 100 uses, sends the public key to configured endpoints

### Entertainment
- On ~1 in 7 runs, fetches a random text file from a GitHub repo
- Sends it to a local [Ollama](https://ollama.ai) AI for amusing commentary
- Requires Ollama running locally (optional — program works fine without it)

### Logging
- All events logged to `exceptions/exceptions.log`
- Logs rotate automatically when they exceed 40 MB
- Old logs archived to `exceptions/archive/` (git-ignored)

## Project Structure

```
src/
├── Main.java                          Simple standalone version
├── pennywise/
│   ├── config.xml                     All settings live here
│   ├── Main.java                      Full-featured entry point
│   ├── XMLHandler.java                Config parser
│   └── EntertainmentHandler.java      Ollama AI integration
├── security/
│   ├── ExceptionHandler.java          Logging & error handling
│   ├── SecurityHandler.java           Key validation
│   ├── CertificateHandler.java        TLS cert retrieval
│   └── UsageHandler.java              Usage tracking
├── science/
│   └── public.key                     Local public key
└── com/mearvk/imaging/
    └── ImageMetadataReader.java       EXIF parser (no dependencies)
```

## Need a GUI?

Ask [Kiro](https://kiro.dev) to build one — it's affordable, available online now, and can generate a full Swing or JavaFX interface for this program in minutes. See STRUCTURE.txt for ready-to-use prompts.

---

MearvK Ltd - MEARVK LLC

KEEP IT UP GUYS! Your Shots Count!

Thanks Chancellor of UNC! You Guys Rule!
