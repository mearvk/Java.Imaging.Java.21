# PIXEL.md — Java.Imaging.Java.21

## 1. Purpose

PIXEL.md is the project-orientation document for Java.Imaging.Java.21. It records what the repository is intended to make, what is actually represented in the source tree, and how contributors should describe and extend this kind of software without confusing planned work with implemented behavior.

Java.Imaging.Java.21 is a Java 21 iPhone media-organization project. Its primary represented workflow is to scan a source directory, classify photos and videos, derive dates from image metadata or file timestamps, and place renamed media into organized destination folders.

The repository also contains security-related, entertainment, logging, configuration, and optional MySQL integration material. Those areas should be described according to the code and documentation actually present in the repository.

---

## 2. What’s Made

### 2.1 iPhone Media Organizer

The central application organizes media into predictable directory structures.

The documented workflow includes:

- scanning a configured source directory;
- identifying supported image and video extensions;
- separating images from videos;
- grouping common video formats into type-specific directories;
- deriving dates from EXIF metadata for photos where available;
- using file modification time for videos;
- generating chronological filenames;
- preventing timestamp collisions with a numeric suffix.

The documented filename pattern is:

YYYY-MM-DD_HH-mm-ss_001.ext

### 2.2 Java 21 Application Structure

The project is implemented as a Java 21 application without Maven or Gradle being identified as the primary build system. The repository includes direct javac compilation instructions and an IntelliJ IDEA module.

The main source areas include:

- src/pennywise/ — XML-driven application entry point and configuration handling;
- src/security/ — logging, key-validation, certificate, and usage-related components;
- src/com/mearvk/imaging/ — image metadata parsing;
- src/science/ — local public-key material;
- src/Main.java — simpler standalone entry point.

### 2.3 Image Metadata Handling

ImageMetadataReader.java provides a self-contained JPEG/TIFF EXIF parsing implementation as represented by the project documentation.

This makes image date extraction part of the organizer's core documented behavior without requiring a separate image-processing framework for that parser.

### 2.4 XML Configuration

src/pennywise/config.xml provides the documented configuration boundary.

Configuration covers:

- source and destination paths;
- recognized file types;
- security settings;
- entertainment settings;
- runtime behavior that can be changed without recompilation.

The project resolves {HOME} and {USER} path variables for the documented Linux, macOS, and Windows environments.

### 2.5 Security-Oriented Components

The repository contains security-oriented components for:

- local-versus-remote public-key validation;
- TLS certificate retrieval;
- usage tracking;
- exception logging.

These should be treated as application features and integration code, not as independent security certification. Security properties should be verified from implementation and deployment behavior before being presented as guarantees.

### 2.6 Entertainment Integration

The documented full application contains an optional entertainment path involving:

- polling configured GitHub content;
- selecting content according to configured behavior;
- sending content to a locally running Ollama service;
- recording related activity through the project's logging path.

This is an optional application integration and is separate from the core media-organization workflow.

### 2.7 Optional MySQL Integration

MySQL.md documents an optional database layer for recording:

- processed files;
- original and resulting names;
- media type;
- date information;
- source and destination paths;
- application usage history.

The documented database is named imaging, with tables including file_log and usage_log.

Database integration is optional; the repository documents a non-database operating mode as well.

---

## 3. What’s Included

### Core Source

- src/Main.java
- src/pennywise/Main.java
- src/pennywise/XMLHandler.java
- src/pennywise/EntertainmentHandler.java
- src/com/mearvk/imaging/ImageMetadataReader.java
- security-related Java classes under src/security/

### Configuration

- src/pennywise/config.xml
- IntelliJ module metadata in Java Imaging.iml

### Documentation

- README.md
- STRUCTURE.txt
- MySQL.md
- INTELLIGIBLE.md
- LICENSE.md

### Operational Material

The repository also contains configuration/, contacts/, modules/, and scripts/ directories. Their individual contents should be treated as the authoritative source for any detailed operational capability rather than inferred from their directory names alone.

### Build Model

The documented baseline uses Java 21+ and direct javac commands. The project does not currently present Maven or Gradle as its primary build contract.

---

## 4. How to Write This Kind of Stuff

### Start With the User Workflow

Explain the thing a person actually does first:

1. provide media;
2. configure paths if necessary;
3. run the organizer;
4. inspect organized output.

Then describe the internal architecture.

### Separate Media Logic From Integrations

The media organizer is the core. Security, entertainment, logging, and MySQL are integration layers.

Keeping those boundaries visible makes the project easier to maintain and reason about.

### Name Real Files

Use paths such as:

- src/pennywise/Main.java
- src/pennywise/XMLHandler.java
- src/com/mearvk/imaging/ImageMetadataReader.java
- src/pennywise/config.xml

Concrete paths are more useful than broad statements such as “the imaging module.”

### Describe Supported File Types Precisely

The README documents image extensions including:

.jpg, .jpeg, .heic, .png, .tiff, .tif, .bmp, .gif, .webp

and video extensions including:

.mov, .mp4, .mpeg, .mpg, .avi, .m4v

When these lists change, update the configuration documentation and this PIXEL document together.

### Distinguish Configuration From Code

If behavior can be changed in config.xml without recompilation, document it as configuration rather than presenting every setting as a source-code change.

### Treat External Services as Boundaries

GitHub, Ollama, Apple-related endpoints, Disney-related endpoints, iCloud, and MySQL are external boundaries in the documented design.

External dependencies should have:

- clear purpose;
- explicit configuration;
- failure behavior;
- privacy/security considerations;
- documented network assumptions.

### Never Turn Documentation Into a Security Guarantee

A component called SecurityHandler or CertificateHandler is evidence that security-related code exists. It is not, by itself, proof of a security property.

Use implementation evidence, tests, deployment controls, and independent review when making stronger claims.

### Keep Credentials Out of Source

The current MySQL.md contains example/default database credentials. Production deployments should replace default credentials and keep secrets outside version-controlled source.

### Preserve Cross-Platform Intent

The project documents Linux, macOS, and Windows operation. Changes should preserve platform-neutral Java behavior where practical and clearly identify platform-specific scripts or assumptions.

---

## 5. Recommended PIXEL Pattern

For this repository, future PIXEL updates should follow this order:

1. Purpose — what the project is;
2. What’s Made — capabilities represented by source and documentation;
3. What’s Included — files, directories, configuration, and supporting material;
4. How to Write This Kind of Stuff — engineering/documentation principles;
5. Recommended PIXEL Pattern — how future maintainers should extend the document;
6. Current Project Snapshot — concise state of the repository;
7. Maintenance Rule — what must be updated when the implementation changes.

The document should remain descriptive rather than promotional.

---

## 6. Current Project Snapshot

Project: Java.Imaging.Java.21  
Primary language: Java  
Target/runtime: Java 21+  
Primary function: iPhone photo and video organization  
Configuration: XML  
Primary build style: direct javac compilation / IntelliJ module  
Optional database: MySQL  
Optional AI integration: local Ollama  
Platforms documented: Linux, macOS, Windows  
Primary branch: master

The repository currently represents a compact application with a clear media-organization workflow plus several optional integration layers.

---

## 7. Maintenance Rule

When the implementation changes, update PIXEL.md when the change materially affects:

- the application's primary workflow;
- source-tree architecture;
- supported media formats;
- configuration semantics;
- build or runtime requirements;
- external services;
- database behavior;
- security-related behavior;
- platform support;
- documented operational boundaries.

The governing rule is simple:

> **Describe what the repository actually makes, identify what it includes, and keep future capability visibly separate from present implementation.**

---

*Max Rupplin - MEARVK LLC - 2026*