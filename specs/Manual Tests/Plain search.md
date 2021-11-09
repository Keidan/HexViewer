---
testspace:
title: Plain text search
---

{% if page %} {% assign spec = page %} {% endif %}

# {{ spec.title }}
Searching for text in the plain text view.

## Test
- Follow the instructions specified in the `Plain view` test.
- Click on the magnifying glass icon at the top of the application.
- Enter a text that appears on a single line.
- The list is updated with only those lines that contain the word you are looking for.
- Enter text that has a word that overlaps multiple lines.
- The list is updated with only those lines that contain the word you are looking for.
