package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NoteDatabase
import com.example.data.NoteEntity
import com.example.data.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NotesTab {
    ALL, FAVORITES, ARCHIVE
}

enum class NotesViewMode {
    GRID, LIST
}

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository

    init {
        val database = NoteDatabase.getDatabase(application, viewModelScope)
        repository = NoteRepository(database.noteDao())
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedTab = MutableStateFlow(NotesTab.ALL)
    val selectedTab: StateFlow<NotesTab> = _selectedTab.asStateFlow()

    private val _viewMode = MutableStateFlow(NotesViewMode.GRID)
    val viewMode: StateFlow<NotesViewMode> = _viewMode.asStateFlow()

    private val _recentlyDeletedNote = MutableStateFlow<NoteEntity?>(null)
    val recentlyDeletedNote: StateFlow<NoteEntity?> = _recentlyDeletedNote.asStateFlow()

    val availableCategories = listOf(
        "All", "General", "Ideas", "Tasks", "Work", "Personal", "Study"
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val baseNotesFlow = combine(_selectedTab, _searchQuery) { tab, query ->
        Pair(tab, query)
    }.flatMapLatest { (tab, query) ->
        if (query.isNotBlank()) {
            repository.searchNotes(query.trim())
        } else {
            when (tab) {
                NotesTab.ALL -> repository.activeNotes
                NotesTab.FAVORITES -> repository.favoriteNotes
                NotesTab.ARCHIVE -> repository.archivedNotes
            }
        }
    }

    val displayedNotes: StateFlow<List<NoteEntity>> = combine(
        baseNotesFlow,
        _selectedCategory,
        _selectedTab,
        _searchQuery
    ) { notes, category, tab, query ->
        var list = notes
        if (tab == NotesTab.ARCHIVE && query.isNotBlank()) {
            list = list.filter { it.isArchived }
        } else if (tab != NotesTab.ARCHIVE && query.isNotBlank()) {
            list = list.filter { !it.isArchived }
            if (tab == NotesTab.FAVORITES) {
                list = list.filter { it.isFavorite }
            }
        }

        if (category != "All") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun onTabSelected(tab: NotesTab) {
        _selectedTab.value = tab
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == NotesViewMode.GRID) NotesViewMode.LIST else NotesViewMode.GRID
    }

    fun saveNote(note: NoteEntity, onSaved: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.insertOrUpdate(
                note.copy(updatedAt = System.currentTimeMillis())
            )
            onSaved?.invoke(id)
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.setPinned(note.id, !note.isPinned)
        }
    }

    fun toggleFavorite(note: NoteEntity) {
        viewModelScope.launch {
            repository.setFavorite(note.id, !note.isFavorite)
        }
    }

    fun toggleArchive(note: NoteEntity) {
        viewModelScope.launch {
            repository.setArchived(note.id, !note.isArchived)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            _recentlyDeletedNote.value = note
            repository.delete(note)
        }
    }

    fun undoDelete() {
        val noteToRestore = _recentlyDeletedNote.value ?: return
        viewModelScope.launch {
            repository.insertOrUpdate(noteToRestore)
            _recentlyDeletedNote.value = null
        }
    }

    fun clearRecentlyDeleted() {
        _recentlyDeletedNote.value = null
    }

    fun getNoteFlow(id: Long) = repository.getNoteById(id)
}
