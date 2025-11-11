package com.example.parkwise.ui.screens



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parkwise.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Note: This screen assumes the UserProfile model and AuthViewModel functions
// from your application's data layer are correctly implemented.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailsScreen(
    authViewModel: AuthViewModel = viewModel(),
    onBack: () -> Unit
) {
    // 1. Fetch the current user profile data
    val profile by authViewModel.userProfile.collectAsState()

    // 2. State for form fields (Initialize with current profile data from RTDB)
    //    These will be null/empty if the user is new and hasn't saved vehicle details yet.
    var carName by remember(profile.carName) { mutableStateOf(profile.carName ?: "") }
    var carType by remember(profile.carType) { mutableStateOf(profile.carType ?: "") }
    var fuelType by remember(profile.fuelType) { mutableStateOf(profile.fuelType ?: "") }
    var carNumberPlate by remember(profile.carNumberPlate) { mutableStateOf(profile.carNumberPlate ?: "") }
    var prefersEv by remember(profile.prefersEv) { mutableStateOf(profile.prefersEv ?: false) }

    // 3. Determine if the Save button should be enabled
    val isFormValid = remember(carName, carNumberPlate) {
        carName.isNotBlank() && carNumberPlate.isNotBlank()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Vehicle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    // 4. Create an updated profile object to save
                    val updatedProfile = profile.copy(
                        carName = carName.trim().takeIf { it.isNotBlank() },
                        carType = carType.trim().takeIf { it.isNotBlank() },
                        fuelType = fuelType.trim().takeIf { it.isNotBlank() },
                        carNumberPlate = carNumberPlate.trim().takeIf { it.isNotBlank() },
                        prefersEv = prefersEv
                    )

                    // 5. Call the ViewModel function to save data.
                    //    This update will create the initial vehicle data in RTDB if none existed.
                    authViewModel.updateUserProfile(updatedProfile)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = isFormValid
            ) {
                Text("Save Vehicle Details")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Car Name Field
            OutlinedTextField(
                value = carName,
                onValueChange = { carName = it },
                label = { Text("Car Make & Model (e.g., Tesla Model 3)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Car Type Field
            OutlinedTextField(
                value = carType,
                onValueChange = { carType = it },
                label = { Text("Vehicle Type (e.g., Sedan, SUV)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Fuel Type Field
            OutlinedTextField(
                value = fuelType,
                onValueChange = { fuelType = it },
                label = { Text("Fuel Type (e.g., Petrol, Diesel, Electric)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Number Plate Field
            OutlinedTextField(
                value = carNumberPlate,
                onValueChange = { carNumberPlate = it.uppercase() },
                label = { Text("License Plate Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Prefers EV Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Prefer EV Charging Spots",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Show electric vehicle charging stations preferentially.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = prefersEv,
                    onCheckedChange = { prefersEv = it }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Vehicle information is required for certain parking zones and services.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}