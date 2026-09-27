package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ChecklistItem
import com.example.data.ChecklistParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Notely", appName)
  }

  @Test
  fun `test checklist serialization and parsing`() {
    val items = listOf(
        ChecklistItem(id = "1", text = "Buy milk", isDone = true),
        ChecklistItem(id = "2", text = "Write code", isDone = false)
    )
    val json = ChecklistParser.toJson(items)
    val parsed = ChecklistParser.parse(json)

    assertEquals(2, parsed.size)
    assertEquals("Buy milk", parsed[0].text)
    assertTrue(parsed[0].isDone)
    assertEquals("Write code", parsed[1].text)
    assertEquals(false, parsed[1].isDone)
  }
}
