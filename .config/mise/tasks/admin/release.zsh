#!/usr/bin/env zsh
#MISE description="Bump version across gradle/libs.versions.toml, gradle.properties, build-logic/gradle.properties, mise.toml, create git tag (version/X.Y.Z), and push by default"
#MISE alias="release"
#USAGE arg "[version]" help="Semantic version to release (e.g. 3.0.0)"
#USAGE flag "-n --no-push" help="Skip push, creating commit and tag locally only"
#USAGE flag "--suppress-auto-push" help="Alias to skip push, creating commit and tag locally only"
#USAGE flag "--commit-only" help="Alias to skip push, creating commit and tag locally only"

set -euo pipefail

BRANCH="$(git rev-parse --abbrev-ref HEAD)"
if [[ "$BRANCH" != "main" ]]; then
  echo "❌ Error: releases must be cut from 'main' (current: $BRANCH)" >&2
  exit 1
fi

TOML_FILE="gradle/libs.versions.toml"
PROPERTIES_FILE="gradle.properties"
BUILD_LOGIC_PROPERTIES="build-logic/gradle.properties"
MISE_FILE="mise.toml"

VERSION=""
NO_PUSH=false

for arg in "$@"; do
  case "$arg" in
    -n|--no-push|--suppress-auto-push|--commit-only)
      NO_PUSH=true
      ;;
    -p|--push)
      # Retained for backwards compatibility; push is now the default
      ;;
    -*)
      ;;
    *)
      if [[ -z "$VERSION" ]]; then
        VERSION="$arg"
      fi
      ;;
  esac
done

CURRENT_TOML_VERSION=$(sed -n -E 's/^[[:space:]]*project[[:space:]]*=[[:space:]]*"([^"]+)"/\1/p' "$TOML_FILE" | head -n1)
LATEST_GIT_TAG=$(git tag -l "version/*" | tr -s '[:space:]' '\n' | sort -V | tail -n1 | sed 's|^version/||')

if [[ -z "$VERSION" ]]; then
  if [[ -n "$LATEST_GIT_TAG" ]] && git rev-parse "version/$LATEST_GIT_TAG" >/dev/null 2>&1; then
    # Default to latest git tag if it exists locally, or prompt
    echo "Current libs.versions.toml version: ${CURRENT_TOML_VERSION:-None}"
    echo "Current highest git tag: ${LATEST_GIT_TAG:-None}"
    read -r "VERSION?Enter version to release or push (default: ${LATEST_GIT_TAG}): "
    VERSION="${VERSION:-$LATEST_GIT_TAG}"
  else
    echo "Current libs.versions.toml version: ${CURRENT_TOML_VERSION:-None}"
    echo "Current highest git tag: ${LATEST_GIT_TAG:-None}"
    read -r "VERSION?Enter new version to release (e.g. 3.0.0): "
  fi
fi

VERSION="${VERSION#version/}"
VERSION="${VERSION#v}"

if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "❌ Error: Version '$VERSION' does not follow semantic versioning (X.Y.Z, e.g. 3.0.0)." >&2
  exit 1
fi

TAG_NAME="version/$VERSION"

# If tag already exists locally:
if git rev-parse "$TAG_NAME" >/dev/null 2>&1; then
  # Check if remote already has this tag
  REMOTE_TAG=$(git ls-remote --tags origin "refs/tags/$TAG_NAME" 2>/dev/null || true)
  if [[ -n "$REMOTE_TAG" ]]; then
    echo "❌ Error: Git tag '$TAG_NAME' already exists on remote origin." >&2
    exit 1
  fi

  if [[ "$NO_PUSH" == true ]]; then
    echo "ℹ️  Git tag '$TAG_NAME' already exists locally."
    echo "Tag created locally (--no-push specified). Run without --no-push to push to remote:"
    echo "   mise run release $VERSION"
    exit 0
  else
    echo "ℹ️  Git tag '$TAG_NAME' exists locally but not on remote origin."
    echo "Treating this invocation as the push path for '$TAG_NAME'..."
    echo "Pushing '$BRANCH' and '$TAG_NAME' to origin..."
    git push origin "$BRANCH"
    git push origin "$TAG_NAME"
    echo "✓ Pushed commit and tag to remote origin."
    exit 0
  fi
fi

# Ensure working tree is clean outside of version descriptor files
UNCOMMITTED=$(git status --porcelain | grep -v "$TOML_FILE" | grep -v "$PROPERTIES_FILE" | grep -v "$BUILD_LOGIC_PROPERTIES" | grep -v "$MISE_FILE" || true)
if [[ -n "$UNCOMMITTED" ]]; then
  echo "⚠️ Warning: Working tree has uncommitted changes outside of version metadata files:" >&2
  git status -s >&2
  echo "Please commit or stash your changes before tagging a release." >&2
  exit 1
fi

echo "📝 Updating version to $VERSION across project metadata files..."

# Update gradle/libs.versions.toml
if [[ -f "$TOML_FILE" ]]; then
  sed -i '' -E "s/^[[:space:]]*project[[:space:]]*=[[:space:]]*\"[^\"]+\"/  project = \"$VERSION\"/" "$TOML_FILE"
fi

# Update gradle.properties
if [[ -f "$PROPERTIES_FILE" ]]; then
  sed -i '' -E "s/^[[:space:]]*version[[:space:]]*=[[:space:]]*.*/version = $VERSION/" "$PROPERTIES_FILE"
fi

# Update build-logic/gradle.properties
if [[ -f "$BUILD_LOGIC_PROPERTIES" ]]; then
  sed -i '' -E "s/^[[:space:]]*version[[:space:]]*=[[:space:]]*.*/version = $VERSION/" "$BUILD_LOGIC_PROPERTIES"
fi

# Update mise.toml
if [[ -f "$MISE_FILE" ]]; then
  sed -i '' -E "s/^[[:space:]]*PROJECT_VERSION[[:space:]]*=[[:space:]]*\"[^\"]+\"/  PROJECT_VERSION = \"$VERSION\"/" "$MISE_FILE"
fi

git add "$TOML_FILE" "$PROPERTIES_FILE" "$BUILD_LOGIC_PROPERTIES" "$MISE_FILE"
git commit -m "chore(release): bump version to $VERSION"
echo "✓ Committed version $VERSION bump across project files."

echo "🚀 Creating git tag '$TAG_NAME' on branch '$BRANCH'..."
git tag -a "$TAG_NAME" -m "Release version $VERSION"
echo "✓ Created annotated tag: $TAG_NAME"

if [[ "$NO_PUSH" == true ]]; then
  echo ""
  echo "Tag created locally (--no-push/--commit-only specified). When ready to publish:"
  echo "   mise run release $VERSION"
  echo "   (or: git push origin $BRANCH && git push origin $TAG_NAME)"
else
  echo "Pushing '$BRANCH' and '$TAG_NAME' to origin..."
  git push origin "$BRANCH"
  git push origin "$TAG_NAME"
  echo "✓ Pushed commit and tag to remote origin."
fi
