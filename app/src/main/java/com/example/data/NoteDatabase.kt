package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "notely_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.noteDao())
                }
            }
        }

        private suspend fun populateInitialData(noteDao: NoteDao) {
            val checklistItems = listOf(
                ChecklistItem(text = "Try creating a note with the + button", isDone = true),
                ChecklistItem(text = "Pick custom accent colors & categories", isDone = false),
                ChecklistItem(text = "Pin important notes to keep them at top", isDone = false),
                ChecklistItem(text = "Toggle checklist mode for your to-dos", isDone = false)
            )

            noteDao.insertNote(
                NoteEntity(
                    title = "Welcome to Notely! ✍️",
                    content = "Notely is your minimal, powerful notes companion. Capture quick thoughts, organize daily tasks, and keep everything safe offline.\n\nTap on any note to edit, or use the color palette to organize your thoughts visually.",
                    colorKey = "AMBER",
                    category = "Ideas",
                    isPinned = true,
                    isFavorite = true,
                    checklistJson = ChecklistParser.toJson(checklistItems)
                )
            )

            noteDao.insertNote(
                NoteEntity(
                    title = "Grocery & Weekend Prep 🛒",
                    content = "Remember to check farmers market on Saturday morning for fresh sourdough and berries!",
                    colorKey = "MINT",
                    category = "Tasks",
                    isPinned = false,
                    isFavorite = false,
                    checklistJson = ChecklistParser.toJson(
                        listOf(
                            ChecklistItem(text = "Oat milk & Espresso beans", isDone = true),
                            ChecklistItem(text = "Fresh basil & ripe avocados", isDone = false),
                            ChecklistItem(text = "Dark chocolate 85%", isDone = false)
                        )
                    )
                )
            )

            noteDao.insertNote(
                NoteEntity(
                    title = "Project Brainstorm 💡",
                    content = "1. Clean Material 3 design with responsive grid.\n2. Instant real-time search across titles and checklists.\n3. Offline-first local database with Room.\n4. Quick share with other apps.",
                    colorKey = "SKY",
                    category = "Work",
                    isPinned = false,
                    isFavorite = true,
                    checklistJson = ""
                )
            )
        }
    }
}
