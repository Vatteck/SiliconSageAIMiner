#!/bin/bash
#
# Silicon Sage Release Script
# Usage: ./release.sh 2.9.17-dev "Brief changelog summary"
#
# This script:
#   1. Updates version in build.gradle.kts, version.json, README.md
#   2. Builds the debug APK
#   3. Copies APK to Desktop with proper naming
#   4. Commits all changes
#   5. Creates and pushes a git tag (triggers GitHub Actions)
#

set -e  # Exit on error

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Project paths
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD_GRADLE="$PROJECT_DIR/app/build.gradle.kts"
VERSION_JSON="$PROJECT_DIR/version.json"
README="$PROJECT_DIR/README.md"
CHANGELOG="$PROJECT_DIR/CHANGELOG.md"
APK_OUTPUT="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
DESKTOP="$HOME/Desktop"

# Parse arguments
VERSION="$1"
SUMMARY="$2"

if [ -z "$VERSION" ]; then
    echo -e "${RED}Error: Version required${NC}"
    echo "Usage: ./release.sh <version> [summary]"
    echo "Example: ./release.sh 2.9.17-dev \"Added new raid mechanics\""
    exit 1
fi

# Strip 'v' prefix if provided
VERSION="${VERSION#v}"

echo -e "${CYAN}═══════════════════════════════════════════${NC}"
echo -e "${CYAN}  Silicon Sage Release: v${VERSION}${NC}"
echo -e "${CYAN}═══════════════════════════════════════════${NC}"
echo ""

# Get current version code and increment
CURRENT_CODE=$(grep -oP 'versionCode = \K\d+' "$BUILD_GRADLE")
NEW_CODE=$((CURRENT_CODE + 1))

echo -e "${YELLOW}[1/6]${NC} Updating version files..."
echo "       versionCode: $CURRENT_CODE → $NEW_CODE"
echo "       versionName: $VERSION"

# Update build.gradle.kts
sed -i "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/" "$BUILD_GRADLE"
sed -i "s/versionName = \".*\"/versionName = \"$VERSION\"/" "$BUILD_GRADLE"

# Update version.json (changes as string for backward compatibility with GSON)
TODAY=$(date +%Y-%m-%d)
cat > "$VERSION_JSON" << EOF
{
  "version": "$VERSION",
  "build": $NEW_CODE,
  "date": "$TODAY",
  "changes": "${SUMMARY:-See CHANGELOG.md for details}"
}
EOF

# Update README.md footer
sed -i "s/\*\*Current Version\*\*: v.*/\*\*Current Version\*\*: v$VERSION/" "$README"
sed -i "s/\*\*Last Updated\*\*: .*/\*\*Last Updated\*\*: $TODAY/" "$README"

echo -e "${GREEN}       ✓ Version files updated${NC}"

# Build APK
echo ""
echo -e "${YELLOW}[2/6]${NC} Building debug APK..."
cd "$PROJECT_DIR"
./gradlew assembleDebug --quiet

if [ ! -f "$APK_OUTPUT" ]; then
    echo -e "${RED}Error: APK not found at $APK_OUTPUT${NC}"
    exit 1
fi

echo -e "${GREEN}       ✓ Build successful${NC}"

# Copy APK to Desktop
APK_NAME="Sage_${VERSION}.apk"
echo ""
echo -e "${YELLOW}[3/6]${NC} Copying APK to Desktop..."
cp "$APK_OUTPUT" "$DESKTOP/$APK_NAME"
echo -e "${GREEN}       ✓ $APK_NAME → Desktop${NC}"

# Copy APK to releases folder for GitHub Actions
RELEASES_DIR="$PROJECT_DIR/releases"
mkdir -p "$RELEASES_DIR"
cp "$APK_OUTPUT" "$RELEASES_DIR/$APK_NAME"
echo -e "${GREEN}       ✓ $APK_NAME → releases/${NC}"

# Git operations
echo ""
echo -e "${YELLOW}[4/6]${NC} Committing changes..."
git add -A
git commit -m "Release v$VERSION" -m "${SUMMARY:-Release v$VERSION}"
echo -e "${GREEN}       ✓ Changes committed${NC}"

echo ""
echo -e "${YELLOW}[5/6]${NC} Creating tag v$VERSION..."
if git rev-parse "v$VERSION" >/dev/null 2>&1; then
    echo -e "       Tag exists, replacing..."
    git tag -d "v$VERSION"
    git push origin ":refs/tags/v$VERSION" 2>/dev/null || true
fi
git tag -a "v$VERSION" -m "Release v$VERSION"
echo -e "${GREEN}       ✓ Tag created${NC}"

echo ""
echo -e "${YELLOW}[6/6]${NC} Pushing to origin..."
BRANCH=$(git rev-parse --abbrev-ref HEAD)
git push origin "$BRANCH" --tags
echo -e "${GREEN}       ✓ Pushed to GitHub${NC}"

echo ""
echo -e "${CYAN}═══════════════════════════════════════════${NC}"
echo -e "${GREEN}  Release v$VERSION complete!${NC}"
echo -e "${CYAN}═══════════════════════════════════════════${NC}"
echo ""
echo -e "  ${CYAN}Local APK:${NC}  $DESKTOP/$APK_NAME"
echo -e "  ${CYAN}GitHub:${NC}     https://github.com/Vatteck/SiliconSageAIMiner/releases/tag/v$VERSION"
echo ""
echo -e "  GitHub Actions will now create the release automatically."
echo ""
