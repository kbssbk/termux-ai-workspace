package com.beomsoo.sentencelibrary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class InstallIdentityTest {
    @Test fun v3_identity_is_stable_and_does_not_reuse_v2_package() {
        assertEquals("com.beomsoo.sentencelibrary", BuildConfig.APPLICATION_ID)
        assertNotEquals("com.beomsoo.sentenceapp", BuildConfig.APPLICATION_ID)
    }
}
