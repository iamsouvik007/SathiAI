# SathiAI Voice & Chat Management Upgrade

- [/] Voice Recognition Integration
    - [ ] Create `VoiceRecognizerManager` with `SpeechRecognizer`
    - [ ] Add `RECORD_AUDIO` permission to `AndroidManifest.xml`
    - [ ] Implement `VoiceState` management in `ChatViewModel`
- [ ] Chat History Enhancements
    - [ ] Update `ConversationEntity` with `isPinned` field
    - [ ] Update `ChatDao` with pin/delete/rename methods
    - [ ] Implement UI for Pin/Delete actions in History Drawer
- [ ] Premium UI/UX Implementation
    - [ ] Add animated Microphone button with listening glow/waveform
    - [ ] Implement Permission handling for Microphone
    - [ ] Add Swipe-to-Action or Context Menu for chat history items
- [ ] Final Verification
    - [ ] Test STT in different network conditions
    - [ ] Verify persistence of pinned chats
    - [ ] Ensure overlay service remains untouched
