---
testspace:
title: First start
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Start the application for the first time (or after a cache cleanup).

## Buttons behavour
The `OPEN` and `PARTIAL FILE OPENING` buttons must be enabled.
The `RECENTLY OPENED` button must be diabled.

## Menus behavour
- Click on the contextual menu (the three dots at the top right of the application).
- The menus below must be enabled:
   - `Open...`
   - `Partial file opening...`
   - `Settings`
- All other menus must be disabled.
