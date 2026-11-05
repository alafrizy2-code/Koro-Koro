package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.VocalPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Koro Koro TV", appName)
  }

  @Test
  fun `verify numeric remote 0 to 9 vocal presets mapping`() {
    assertEquals(VocalPreset.NORMAL, VocalPreset.fromKeyNumber(0))
    assertEquals(VocalPreset.STUDIO_REVERB, VocalPreset.fromKeyNumber(1))
    assertEquals(VocalPreset.ECHO, VocalPreset.fromKeyNumber(2))
    assertEquals(VocalPreset.CONCERT_HALL, VocalPreset.fromKeyNumber(3))
    assertEquals(VocalPreset.KARAOKE_POP, VocalPreset.fromKeyNumber(4))
    assertEquals(VocalPreset.DEEP_VOCAL, VocalPreset.fromKeyNumber(5))
    assertEquals(VocalPreset.BRIGHT_VOCAL, VocalPreset.fromKeyNumber(6))
    assertEquals(VocalPreset.WARM_VOCAL, VocalPreset.fromKeyNumber(7))
    assertEquals(VocalPreset.POWERFUL_VOCAL, VocalPreset.fromKeyNumber(8))
    assertEquals(VocalPreset.CUSTOM_EFFECT, VocalPreset.fromKeyNumber(9))
    for (i in 0..9) {
      assertNotNull(VocalPreset.fromKeyNumber(i))
    }
  }
}
