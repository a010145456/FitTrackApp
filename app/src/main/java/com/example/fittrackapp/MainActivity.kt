package com.example.fittrackapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fittrackapp.ui.theme.FitTrackAppTheme
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

class MainActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitTrackAppTheme {
                AppContent(db)
            }
        }
    }
}

@Composable
fun AppContent(db: FirebaseFirestore) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Menu) }

    when (currentScreen) {
        is Screen.Menu -> MenuScreen(onNavigate = { currentScreen = it })
        is Screen.Add -> AddExerciseScreen(db = db, onNavigateBack = { currentScreen = Screen.Menu })
        is Screen.List -> ListExercisesScreen(db = db, onNavigateBack = { currentScreen = Screen.Menu })
        is Screen.Modify -> ModifyExerciseScreen(db = db, onNavigateBack = { currentScreen = Screen.Menu })
        is Screen.Delete -> DeleteExerciseScreen(db = db, onNavigateBack = { currentScreen = Screen.Menu })
    }
}

@Composable
fun MenuScreen(onNavigate: (Screen) -> Unit) {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(text = "Menú Principal", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { onNavigate(Screen.Add) }, modifier = Modifier.fillMaxWidth()) {
            Text("Agregar Ejercicio")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onNavigate(Screen.List) }, modifier = Modifier.fillMaxWidth()) {
            Text("Listar Ejercicios")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onNavigate(Screen.Modify) }, modifier = Modifier.fillMaxWidth()) {
            Text("Modificar Ejercicio")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onNavigate(Screen.Delete) }, modifier = Modifier.fillMaxWidth()) {
            Text("Eliminar Ejercicio")
        }
    }
}

@Composable
fun AddExerciseScreen(db: FirebaseFirestore, onNavigateBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(text = "Agregar Ejercicio", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del ejercicio") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duración (minutos)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            addExercise(name, duration.toInt(), db)
            onNavigateBack()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Agregar")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

@Composable
fun ListExercisesScreen(db: FirebaseFirestore, onNavigateBack: () -> Unit) {
    var exercises by remember { mutableStateOf(emptyList<String>()) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(text = "Listar Ejercicios", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { getExercises(db) { ex -> exercises = ex } }, modifier = Modifier.fillMaxWidth()) {
            Text("Cargar Ejercicios")
        }
        Spacer(modifier = Modifier.height(16.dp))

        exercises.forEach { exercise ->
            Text(text = exercise, modifier = Modifier.padding(bottom = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

@Composable
fun ModifyExerciseScreen(db: FirebaseFirestore, onNavigateBack: () -> Unit) {
    var selectedExerciseId by remember { mutableStateOf<String?>(null) }
    var updatedName by remember { mutableStateOf("") }
    var updatedDuration by remember { mutableStateOf("") }
    var exercises by remember { mutableStateOf(emptyList<String>()) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(text = "Modificar Ejercicio", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { getExercises(db) { ex -> exercises = ex } }, modifier = Modifier.fillMaxWidth()) {
            Text("Seleccionar Ejercicio")
        }
        Spacer(modifier = Modifier.height(16.dp))

        exercises.forEach { exercise ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = exercise, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    val exerciseId = exercise.split("=>")[0].trim()
                    selectedExerciseId = exerciseId
                    val details = exercise.split("=>")[1].trim()
                    val (name, duration) = details.split(", ").map { it.split(":")[1].trim() }
                    updatedName = name
                    updatedDuration = duration
                }) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Editar")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (selectedExerciseId != null) {
            TextField(
                value = updatedName,
                onValueChange = { updatedName = it },
                label = { Text("Actualizar nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = updatedDuration,
                onValueChange = { updatedDuration = it },
                label = { Text("Actualizar duración (minutos)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (selectedExerciseId != null) {
                    updateExercise(selectedExerciseId!!, updatedName, updatedDuration.toInt(), db)
                    onNavigateBack()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Actualizar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

@Composable
fun DeleteExerciseScreen(db: FirebaseFirestore, onNavigateBack: () -> Unit) {
    var exercises by remember { mutableStateOf(emptyList<String>()) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(text = "Eliminar Ejercicio", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { getExercises(db) { ex -> exercises = ex } }, modifier = Modifier.fillMaxWidth()) {
            Text("Seleccionar Ejercicio para Eliminar")
        }
        Spacer(modifier = Modifier.height(16.dp))

        exercises.forEach { exercise ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = exercise, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    val exerciseId = exercise.split("=>")[0].trim()
                    deleteExercise(exerciseId, db)
                    onNavigateBack()
                }) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

fun addExercise(name: String, duration: Int, db: FirebaseFirestore) {
    val exercise = hashMapOf(
        "name" to name,
        "duration" to duration
    )

    db.collection("exercises")
        .add(exercise)
        .addOnSuccessListener { documentReference ->
            Log.d("CRUD", "DocumentSnapshot added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w("CRUD", "Error adding document", e)
        }
}

fun getExercises(db: FirebaseFirestore, onResult: (List<String>) -> Unit) {
    db.collection("exercises")
        .get()
        .addOnSuccessListener { result ->
            val exercises = result.map { "${it.id} => name: ${it.getString("name")}, duration: ${it.getLong("duration")}" }
            onResult(exercises)
        }
        .addOnFailureListener { exception ->
            Log.w("CRUD", "Error getting documents.", exception)
            onResult(emptyList())
        }
}

fun updateExercise(documentId: String, name: String, duration: Int, db: FirebaseFirestore) {
    val exerciseUpdates = hashMapOf(
        "name" to name,
        "duration" to duration
    )

    db.collection("exercises").document(documentId)
        .update(exerciseUpdates as Map<String, Any>)
        .addOnSuccessListener {
            Log.d("CRUD", "DocumentSnapshot successfully updated!")
        }
        .addOnFailureListener { e ->
            Log.w("CRUD", "Error updating document", e)
        }
}

fun deleteExercise(documentId: String, db: FirebaseFirestore) {
    db.collection("exercises").document(documentId)
        .delete()
        .addOnSuccessListener {
            Log.d("CRUD", "DocumentSnapshot successfully deleted!")
        }
        .addOnFailureListener { e ->
            Log.w("CRUD", "Error deleting document", e)
        }
}

sealed class Screen {
    object Menu : Screen()
    object Add : Screen()
    object List : Screen()
    object Modify : Screen()
    object Delete : Screen()
}
