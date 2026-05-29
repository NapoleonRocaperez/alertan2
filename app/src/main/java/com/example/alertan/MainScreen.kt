package com.example.alertan

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertan.ui.theme.OnPrimary
import com.example.alertan.ui.theme.Primary
import com.example.alertan.ui.theme.Secondary
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Info



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var temperature by remember { mutableStateOf("--°C") }
    var currentTime by remember { mutableStateOf("") }
    var showAbout by remember { mutableStateOf(false) }
    var alarmaActiva by remember { mutableStateOf(false) }

    //Relog en tiempo real
    LaunchedEffect( Unit) {
        while (true) {
            currentTime = SimpleDateFormat("EEEE dd 'de ' MMMM yyyy  \nHH:mm:ss", Locale("es","CO"))
                .format(Date())
                .replaceFirstChar { it.uppercaseChar() }
            delay(1000L)
        }
    }

    LaunchedEffect(Unit) {
        fetchTemperaturaCucuta{temperature =it}
    }

    DisposableEffect(Unit) {
        val db = Firebase.database.reference.child("alarma")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val valor = snapshot.getValue(Int::class.java) ?:0
                alarmaActiva = valor == 1
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al sicronizar:, ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        db.addValueEventListener(listener)

        onDispose {
            db.removeEventListener(listener)
        }

    }

    if (showAbout){
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Acerca de", color = Primary) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    Text("Aplicacion desarrollada por")
                    Text(
                        "Napoleón Roca Pérez",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary

                    )
                    Text("Ingeniero Electrónico")
                    Text("Cúcuta-2026", color = Secondary)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) {
                    Text("Cerrar", color= Secondary)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚨 ALERTA IE CARLOS RAMIREZ PARIS", color = OnPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        }
    ){
        innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,

                ) {
                    Text(text = currentTime, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Temperatura en Cucuta",color = Primary)
                    Text(
                        text = temperature,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            //Boton de panico

            val buttonColor by animateColorAsState(
                targetValue = if (alarmaActiva)  Color.Red else Color.Green,
                animationSpec = tween (600)
            )

            Button(
                onClick = {
                    alarmaActiva = !alarmaActiva
                    val nuevoValor = if (alarmaActiva) 1 else 0

                    Firebase.database.reference.child("alarma")
                        .setValue(nuevoValor)
                        .addOnSuccessListener {
                            vibrar(context, 600)
                            Toast.makeText(
                                context,
                                if (alarmaActiva) "🚨 ALARMA ACTIVADA" else "✅ Alarma apagada",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            alarmaActiva = !alarmaActiva
                        }
                },
                modifier = Modifier
                    .size(220.dp)
                    .shadow(16.dp, CircleShape),
                colors= ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White
                ),
                shape=CircleShape
            ){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center

                ) {
                    Icon(
                        imageVector = if (alarmaActiva)Icons.Default.Warning else Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(70.dp)

                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (alarmaActiva) "🚨 ALARMA\nACTIVADA" else "✅ Alarma\napagada",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center

                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = {
                    val intentUbicacion= Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)


                    try{
                        context.startActivity(intentUbicacion)
                        //mensaje claro para los pasos quew haga falta
                        Toast.makeText(
                            context, "Toca en 1:Servicios de ubicación  2.Alteras de sismos",
                            Toast.LENGTH_LONG
                        ).show()
                    }catch (e:Exception){
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                        Toast.makeText(
                            context, "Usa la lupa para buscar 'la alaerta de sismos'",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(60.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                    border = BorderStroke(2.dp, Primary),
                    shape= RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)

                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Activar alertas automátcas de sismos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,


                    )
                }
              }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showAbout = true },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                border = BorderStroke(1.dp, Primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info,null,tint=Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Acerca de", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}

private fun vibrar(context: Context, tiempo: Long) {
    val vibrartor=context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
    if(vibrartor?.hasVibrator()==true){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            vibrartor.vibrate(VibrationEffect.createOneShot(tiempo, VibrationEffect.DEFAULT_AMPLITUDE))
        }else{
            @Suppress("DEPRECATION")
            vibrartor.vibrate(tiempo)
        }
    }
}


private fun fetchTemperaturaCucuta(onResult: (String) -> Unit) {
    onResult("27°C")
}