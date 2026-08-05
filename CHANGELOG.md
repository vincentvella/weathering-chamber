# Changelog

All notable changes to the Weathering Chamber are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
Each release ships two jars — one for Minecraft 1.21.1 and one for 26.2.

## [Unreleased]

## [1.0.0] - 2026-08-05

### Added
- **Weathering Chamber** — a water-powered machine that erodes
  cobblestone → gravel → sand, making sand a renewable resource.
- Powered by an adjacent water block on any side (no fuel); ~10 seconds per grind.
- Hopper automation: insert into the input, extract from the output, with input
  filtering so only cobblestone and gravel are accepted.
- Drops its contents when broken.
- Crafted around a grindstone core, with copper sides and pointed-dripstone
  top/bottom.
- Custom block and GUI textures, plus a mod icon.
- Builds for **Minecraft 1.21.1 and 26.2** from a single Fabric codebase.

[Unreleased]: https://github.com/vincentvella/weathering-chamber/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/vincentvella/weathering-chamber/releases/tag/v1.0.0
