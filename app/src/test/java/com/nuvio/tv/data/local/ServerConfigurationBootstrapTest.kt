package com.nuvio.tv.data.local

import android.content.Context
import android.content.SharedPreferences
import com.nuvio.tv.BuildConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerConfigurationBootstrapTest {
    @Test
    fun `fork without cloud secrets has valid client configuration and no advertised cloud login`() {
        val preferences = mockk<SharedPreferences>(relaxed = true)
        every { preferences.getBoolean("custom_enabled", false) } returns false
        val context = mockk<Context>()
        every { context.getSharedPreferences("server_configuration", Context.MODE_PRIVATE) } returns preferences
        val configuration = ServerConfigurationStore(context).loadActive()
        assertTrue(configuration.backendUrl.startsWith("https://"))
        assertTrue(configuration.publishableKey.isNotBlank())
        if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_ANON_KEY.isBlank()) {
            assertFalse(configuration.capabilities.tvLogin)
            assertFalse(configuration.capabilities.watchParty)
        }
    }
}
