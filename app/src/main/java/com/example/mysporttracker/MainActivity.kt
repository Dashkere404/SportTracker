package com.example.mysporttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.mysporttracker.ui.theme.MySportTrackerTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@Entity
data class Exercise (
    @PrimaryKey (autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val weight: Double,
    val number: Int,
    val repeatNumber: Int,
    val isCompleted: Boolean = false
)

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM EXERCISE")
    fun getAll(): Flow<List<Exercise>>

    @Insert
    suspend fun insertExercise (exercise: Exercise)

    @Update
    suspend fun updateExercise (exercise: Exercise)

    @Update
    suspend fun toogleExercise (exercise: Exercise)

    @Delete
    suspend fun deleteExercise (exercise: Exercise)
}

@Database(entities = [Exercise::class], version = 1)
abstract class ExerciseDatabase : RoomDatabase(){
    abstract fun exerciseDao(): ExerciseDao
}

class ExerciseViewModel (
    private val dao: ExerciseDao
) : ViewModel() {
    val exercises: StateFlow<List<Exercise>> = dao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun add(exercise: Exercise) = viewModelScope.launch {
        dao.insertExercise(exercise)
    }
    fun toogle(exercise: Exercise) = viewModelScope.launch {
        dao.toogleExercise(exercise.copy(isCompleted = !exercise.isCompleted))
    }

    fun update(exercise: Exercise) = viewModelScope.launch {
        dao.updateExercise(exercise)
    }
    fun delete(exercise: Exercise) = viewModelScope.launch {
        dao.deleteExercise (exercise)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(
            applicationContext,
            ExerciseDatabase::class.java,
            "sport_tracker_db"
        ).build()

        val dao = db.exerciseDao()

        val factory = ExerciseViewModelFactory (dao)

        val viewModel: ExerciseViewModel by viewModels { factory }

        setContent {
            MySportTrackerTheme {
                Scaffold(modifier = androidx.compose.ui.Modifier.fillMaxSize()) { innerPadding ->
                    WorkoutScreen(
                        viewModel = viewModel,
                        modifier = androidx.compose.ui.Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

class ExerciseViewModelFactory(private val dao: ExerciseDao) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun WorkoutScreen(
    viewModel: ExerciseViewModel,
    modifier: Modifier = Modifier
) {
    val exercises by viewModel.exercises.collectAsState()
    var showDialog by remember {mutableStateOf(false)}
    var showUpdateDialog by remember {mutableStateOf<Exercise?>(null)}
    Box (modifier = modifier.fillMaxSize()){
        if (exercises.isEmpty()){
            Box(
                modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Добро пожаловать в \"Трекер упражнений\"!\nЗдесь вы можете добавлять, изменять, удалять упражнения, которые хотите сделать в будущем.\nПри выполнении какого-то упражнения вы можете пометить его как выполненное, нажав на него.\nНажмите +, чтобы добавить своё первое упражнение",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(exercises, key = {it.id}) {exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onToogle = {viewModel.toogle(it)},
                        onDelete = {viewModel.delete(it)},
                        onUpdate = { showUpdateDialog = it}
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = {showDialog = true},
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Text(text = "+", style = MaterialTheme.typography.titleLarge)
        }
        if (showDialog){
            AddExerciseDialog(
                onDismiss = {showDialog = false},
                onConfirm = {exercise ->
                    viewModel.add(exercise)
                    showDialog = false
                }
            )
        }
        showUpdateDialog?.let {exercise ->
            UpdateExerciseDialog(
                exercise = exercise,
                onDismiss = { showUpdateDialog = null},
                onConfirm = { updated ->
                    viewModel.update(updated)
                    showUpdateDialog = null
                }
            )
        }
    }
}

