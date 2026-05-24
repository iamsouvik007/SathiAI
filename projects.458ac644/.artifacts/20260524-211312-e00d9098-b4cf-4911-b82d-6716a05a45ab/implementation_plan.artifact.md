# SathiAI UI Simplification and Code Block Fixes

This plan outlines the changes to simplify the chat UI by removing unnecessary camera features and implementing a professional dark-themed code block rendering system.

## Proposed Changes

### 1. Simplify Chat Input
Remove all camera and gallery related features from the chat input areas in both the overlay and the main chat screen.

#### [ChatScreen.kt](file:///C:/Users/souvi/AppData/Local/Google/AndroidStudio2025.3.4/projects/app/src/main/java/com/example/sathiai/chat/ChatScreen.kt)
- Remove `onPickImage`, `selectedImage`, and `onClearImage` from `PremiumInputBar`.
- Remove the `IconButton` with `Icons.Default.CameraAlt`.
- Remove image preview logic from `PremiumInputBar`.
- Remove `imagePicker` launcher.

#### [SathiInputBar.kt](file:///C:/Users/souvi/AppData/Local/Google/AndroidStudio2025.3.4/projects/app/src/main/java/com/example/sathiai/ui/components/SathiInputBar.kt)
- (Verify) Ensure no camera/gallery icons are present.

---

### 2. Premium Code Block UI
Implement a custom code block renderer that matches premium AI apps.

#### [NEW] [SathiCodeBlock.kt](file:///C:/Users/souvi/AppData/Local/Google/AndroidStudio2025.3.4/projects/app/src/main/java/com/example/sathiai/ui/components/SathiCodeBlock.kt)
- Create a `SathiCodeBlock` composable:
    - Background: `Color(0xFF1E1E1E)` (Dark Charcoal).
    - Rounded corners: `12.dp`.
    - Horizontal scrolling for long lines.
    - Syntax highlighting (using basic keyword coloring or a library like `syntax-highlighter`).
    - Monospace font (`FontFamily.Monospace`).
    - "Copy" button in the top-right corner.
    - Language label in the top-left corner.

#### [SathiMarkdownText.kt](file:///C:/Users/souvi/AppData/Local/Google/AndroidStudio2025.3.4/projects/app/src/main/java/com/example/sathiai/ui/components/SathiMarkdownText.kt)
- Update `SathiMarkdownText` to parse the markdown string and detect code blocks.
- Render text segments using `MarkdownText` and code blocks using `SathiCodeBlock`.

---

### 3. Layout and Spacing Fixes
Ensure content doesn't overlap with the input bar and improve overall readability.

#### [PremiumOverlayUI.kt](file:///C:/Users/souvi/AppData/Local/Google/AndroidStudio2025.3.4/projects/app/src/main/java/com/example/sathiai/ui/components/PremiumOverlayUI.kt)
- Adjust `LazyColumn` `contentPadding` to ensure the last message is fully visible above the input bar.
- Add a bottom spacer in the `LazyColumn`.

#### [SathiMessageBubble.kt](file:///C:/Users/souvi/AppData/Local/Google/AndroidStudio2025.3.4/projects/app/src/main/java/com/example/sathiai/ui/components/SathiMessageBubble.kt)
- Adjust width constraints to ensure code blocks have enough horizontal space without overflowing the bubble.

## Verification Plan

### Automated Tests
- `analyze_file` on all modified files to ensure no syntax errors.

### Manual Verification
- Open the overlay and main chat: Verify the camera button is gone.
- Send a message containing a code block (e.g., "Write a Python hello world").
- Verify the code block:
    - Dark background.
    - Readable contrast.
    - Horizontal scrolling works.
    - Copy button copies the code correctly.
- Verify that messages scroll correctly and aren't hidden by the input bar.
