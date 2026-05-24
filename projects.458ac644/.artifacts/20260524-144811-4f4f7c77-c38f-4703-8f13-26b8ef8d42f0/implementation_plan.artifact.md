# Voice Recognition & Chat Management Plan

Implement professional Voice-to-Text input and advanced chat history features (pin/delete) while maintaining the existing overlay quickball UI.

## Proposed Changes

### 1. Permissions & Manifest

#### [AndroidManifest.xml](file:///C:/Users/souvi/AppData/Local/Google/AndroidStudio2025.3.4/projects/app/src/main/AndroidManifest.xml)
- Add `android.permission.RECORD_AUDIO`.

---

### 2. Voice Recognition Engine

#### [NEW] [VoiceRecognizerManager.kt](file:///C:/Users/souvi/AppData/Local/Google/AndroidStudio2025.3.4/projects/app/src/main/java/com/example/sathiai/voice/VoiceRecognizerManager.kt)
- Wrapper around `SpeechRecognizer`.
- Handles lifecycle, partial results, errors, and silence detection.
- Provides a `Flow<VoiceState>` for the ViewModel.

---

### 3. Data Layer Enhancements

#### [ChatEntity.kt](file:///C:/Users/souvi/AppData/Local/Google/AndroidStudio2025.3.4/projects/app/src/main/java/com/example/sathiai/data/local/ChatEntity.kt)
- Add `isPinned: Boolean = false` to `ConversationEntity`.

#### [ChatDao.kt](file:///C:/Users/souvi/AppData/Local/Google/AndroidStudio2025.3.4/projects/app/src/main/java/com/example/sathiai/data/local/ChatDao.kt)
- Add `updatePinnedStatus(id, pinned)`.
- Update `getAllConversations()` query to order by `isPinned DESC, lastTimestamp DESC`.

---

### 4. ViewModel Integration

#### [ChatViewModel.kt](file:///C:/Users/souvi/AppData/Local/Google/AndroidStudio2025.3.4/projects/app/src/main/java/com/example/sathiai/chat/ChatViewModel.kt)
- Integrate `VoiceRecognizerManager`.
- Add `togglePinConversation(id)`.
- Expose `sttText` and `isListening` states.

---

### 5. UI Components

#### [ChatScreen.kt](file:///C:/Users/souvi/AppData/Local/Google/AndroidStudio2025.3.4/projects/app/src/main/java/com/example/sathiai/chat/ChatScreen.kt)
- **Voice Button**: Add animated Mic icon with Pulse animation.
- **Permission Handler**: Use `rememberPermissionState`.
- **History Drawer**: Add long-press or trailing icons for Delete/Pin on `NavigationDrawerItem`.

---

## Verification Plan

### Automated Tests
- Build verification: `gradlew :app:assembleDebug`.
- Static analysis: `analyze_file` on new components.

### Manual Verification
1. **STT Flow**: Tap Mic, speak, verify text populates input field in real-time. Verify timeout/silence handling.
2. **Permission Flow**: Deny mic permission, verify graceful error message. Grant permission, verify it starts listening.
3. **History Management**: Pin a chat, verify it moves to top. Delete a chat, verify it disappears from DB.
4. **Consistency**: Ensure Overlay remains visually unchanged.
