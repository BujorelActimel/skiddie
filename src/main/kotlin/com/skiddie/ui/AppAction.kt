package com.skiddie.ui

sealed class AppAction {
    object RunStop : AppAction()
    object ClearTerminal : AppAction()
    object FocusEditor : AppAction()
    object FocusTerminal : AppAction()
    object NewFile : AppAction()
    object SaveFile : AppAction()
    object OpenFile : AppAction()
}
