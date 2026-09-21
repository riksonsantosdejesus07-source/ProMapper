package com.promapper.app

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class ProMapperAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Ponto de integração para o motor de mapeamento.
        // A aplicação não tenta contornar mecanismos anti-cheat.
        return false
    }
}
