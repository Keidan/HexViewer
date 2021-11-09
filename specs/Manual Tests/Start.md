---
testspace:
title: Start
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Starting the application after opening a standard file (Open or Partial file opening).

## Buttons behavour
The `OPEN`, `PARTIAL FILE OPENING` and `RECENTLY OPENED` buttons must be enabled.

## Menus behavour
- Click on the contextual menu (the three dots at the top right of the application).
- The menus below must be enabled:
   - `Open...`
   - `Partial file opening...`
   - `Recently opened...`
   - `Settings`
   - All other menus must be disabled.
