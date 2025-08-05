# Analyze with Sonar

This composite action runs a SonarQube/SonarCloud analysis for a Gradle project.

It sets up Sonar cache, runs the analysis with the correct environment variables, and displays cached files.

---

## Usage

```yaml
- uses: ./.github/actions/analyze-with-sonar
  with:
    sonar-token: ${{ secrets.SONAR_TOKEN }}
