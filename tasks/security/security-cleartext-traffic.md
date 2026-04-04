# HIGH — Cleartext Traffic Permitted

**Category:** Security
**Priority:** HIGH
**Status:** TODO

## Files
- `res/xml/network_security_config.xml:3`

## Issue
Allows HTTP traffic to `webshooter.se` and `localhost`, enabling downgrade attacks.

## Fix
Remove cleartext permission for `webshooter.se`; keep localhost only for debug builds.
