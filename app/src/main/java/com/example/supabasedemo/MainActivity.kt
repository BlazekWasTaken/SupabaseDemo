package com.example.supabasedemo

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.Manifest
import android.app.Activity
import com.example.supabasedemo.data.network.SupabaseClient.client


import androidx.camera.view.PreviewView
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.supabasedemo.compose.screens.ChoiceScreen
import com.example.supabasedemo.compose.screens.LoginScreen
import com.example.supabasedemo.compose.screens.MainMenuScreen
import com.example.supabasedemo.compose.screens.SignupScreen
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseClient
import com.example.supabasedemo.ui.theme.SupabaseDemoTheme
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.math.log

import com.example.supabasedemo.ui.theme.AppTheme
import com.example.supabasedemo.ui.theme.Surface
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 101

    private val _userState = mutableStateOf<UserState>(UserState.InLoginChoice)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface() {
                    Navigation()
                }
            }
        }
    }

    @Composable
    private fun Navigation() {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = LoginProcess
        ) {
            navigation<LoginProcess>(startDestination = LoginChoice) {
                composable<LoginChoice> { ChoiceScreen(
                    onNavigateToLogIn = {
                        navController.navigate(route = Login)
                    },
                    onNavigateToSignUp = {
                        navController.navigate(route = Signup)
                    },
                    getState = {
                        return@ChoiceScreen _userState
                    },
                    setState = {
                        setState(it)
                    }
                ) }
                composable<Login> { LoginScreen(
                    onNavigateToMainMenu = {
                        navController.navigate(route = MainMenu)
                        {
                            popUpToTop(navController)
                        }
                    },
                    getState = {
                        return@LoginScreen _userState
                    },
                    setState = {
                        setState(it)
                    }
                ) }
                composable<Signup> { SignupScreen(
                    onNavigateToChoice = {
                        navController.navigate(route = LoginChoice) {
                            popUpToTop(navController)
                        }
                    },
                    getState = {
                        return@SignupScreen _userState
                    },
                    setState = {
                        setState(it)
                    }
                ) }
            }
            navigation<MainMenu>(startDestination = Menu) {
                composable<Menu> { MainMenuScreen(
                    getState = {
                        return@MainMenuScreen _userState
                    },
                    setState = {
                        setState(it)
                    }
                ) }
                composable<Stats> {

                }
                composable<Tutorial> {

                }
            }
            navigation<Settings>(startDestination = SettingsMenu) {
                composable<SettingsMenu> {

                }
                composable<AccountInfo> {

                }
                composable<Sounds> {

                }
                composable<Theme> {

                }
                composable<Demo> {

                }
            }
            navigation<Game>(startDestination = GameChoice) {
                composable<GameChoice> {

                }
            }
        }
    }

    // region objects
    @Serializable
    object LoginProcess
    @Serializable
    object LoginChoice
    @Serializable
    object Login
    @Serializable
    object Signup

    @Serializable
    object MainMenu
    @Serializable
    object Menu
    @Serializable
    object Stats
    @Serializable
    object Tutorial
    @Serializable
    object Settings
    @Serializable
    object SettingsMenu
    @Serializable
    object AccountInfo
    @Serializable
    object Sounds
    @Serializable
    object Theme
    @Serializable
    object Demo

    @Serializable
    object Game
    @Serializable
    object GameChoice
    // endregion

    private fun NavOptionsBuilder.popUpToTop(navController: NavController) {
        popUpTo(navController.currentBackStackEntry?.destination?.route ?: return) {
            inclusive =  true
        }
    }

    private fun setState(state: UserState) {
        _userState.value = state
        Toast.makeText(this, _userState.value.toString(), Toast.LENGTH_SHORT).show()
    }

    // region game
    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    @Composable
    fun LoggedInScreen(onCreateGameClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to the Game!")
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = onCreateGameClick) {
                Text("Create a Game")
            }
        }
    }

    private fun generateQRCode(text: String): Bitmap? {
        val writer = QRCodeWriter()
        return try {
            Log.d("QRCode", "Attempting to encode QR code for text: $text")

            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height

            Log.d("QRCode", "QR code BitMatrix created with dimensions: $width x $height")

            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            Log.d("QRCode", "QR code Bitmap successfully generated")

            bmp
        } catch (e: WriterException) {
            Log.e("QRCode", "Failed to generate QR code: ${e.message}")
            e.printStackTrace()
            null
        } catch (e: Exception) {
            Log.e("QRCode", "Unexpected error: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    @Composable
    fun QRCodeScanner(
        onScanSuccess: (String) -> Unit,
        onScanError: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
        val scanner = BarcodeScanning.getClient()

        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            LaunchedEffect(context) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            AndroidView(
                modifier = modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = androidx.camera.core.Preview.Builder()
                            .build()
                            .apply {
                                surfaceProvider = previewView.surfaceProvider
                            }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor, { imageProxy ->
                            processImageProxy(scanner, imageProxy, onScanSuccess, onScanError)
                        })

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("QRCodeScanner", "Camera binding failed: ${e.message}")
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )
        }
    }

    @OptIn(ExperimentalGetImage::class)
    public fun processImageProxy(
        scanner: BarcodeScanner,
        imageProxy: ImageProxy,
        onScanSuccess: (String) -> Unit,
        onScanError: () -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(inputImage as InputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { qrCode ->
                            onScanSuccess(qrCode)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("QRCodeScanner", "QR code scanning failed: ${it.message}")
                    onScanError()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun getCurrentUser(): JsonObject? {
        val user = client.auth.currentUserOrNull()
        val metadata = user?.userMetadata
        return metadata
    }

    @Serializable
    data class Game(
        val uuid: String,
        val user1: String,
        val user2: String? = null
    )

    private fun createGameInSupabase(gameUuid: String, onGameCreated: () -> Unit, onError: (String) -> Unit){
        val loggedInUser = getCurrentUser()

        val gameData = Game(
            uuid = gameUuid,
            user1 = loggedInUser?.get("sub").toString().trim().replace("\"", ""),
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = client.from("games")
                    .insert(gameData)

                Log.d("Supabase", "Game created: $result")

                withContext(Dispatchers.Main) {
                    onError("Error creating game.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Supabase", "catch (e: Exception: $e")
                    onError(e.message ?: "Unexpected error occurred.")
                }
            }
        }
    }

    @Composable
    fun CreateGameScreen(onGameCreated: (String) -> Unit) {
        var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var showScanner by remember { mutableStateOf(false) }
        var scannedQRCode by remember { mutableStateOf<String?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                val gameUuid = UUID.randomUUID().toString()
                coroutineScope.launch{
                    createGameInSupabase(gameUuid, onGameCreated = {
                        qrCodeBitmap = generateQRCode(gameUuid) ?: run {
                            errorMessage = "Error generating QR code"
                            return@createGameInSupabase
                        }
                    }, onError = {
                        errorMessage = it
                    })
                }
//                try {
//                    qrCodeBitmap = generateQRCode(gameData) ?: throw Exception("Bitmap generation returned null")
//                } catch (e: Exception) {
//                    Log.e("QRCode", "Error generating QR code: ${e.message}")
//                }
            }) {
                Text("Generate QR Code aka Create Game")
            }
            Spacer(modifier = Modifier.padding(16.dp))

            qrCodeBitmap?.let { bitmap ->
                Image(
                    painter = BitmapPainter(bitmap.asImageBitmap()),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp)
                )
            } ?: Text("Click the button to generate a QR code.")

            Spacer(modifier = Modifier.padding(16.dp))

            Button(onClick = {
                showScanner = true
            }) {
                Text("Join Game")
            }

            if (showScanner) {
                QRCodeScanner(onScanSuccess = { qrCode ->
                    showScanner = false
                    scannedQRCode = qrCode
                }, onScanError = {
                    showScanner = false
                    Log.e("QRCode", "Error scanning QR code")
                }, modifier = Modifier.fillMaxSize()
                )
            }

            scannedQRCode?.let {
                Spacer(modifier = Modifier.padding(16.dp))
                Text("Scanned QR Code: $it")
            }
        }
    }

    @Composable
    fun MainScreen(viewModel: SupabaseAuthViewModel = viewModel()) {
        val context = LocalContext.current
        val userState by viewModel.userState

        var userEmail by remember { mutableStateOf("") }
        var userPassword by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var macAddress by remember { mutableStateOf("") }

        var currentUserState by remember { mutableStateOf("") }

        var navigateToCreateGame by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.isUserLoggedIn(
                context,
            )
        }

        if (navigateToCreateGame) {
            CreateGameScreen(onGameCreated = {
                navigateToCreateGame = false
            })
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = userEmail,
                    placeholder = {
                        Text(text = "Enter email")
                    },
                    onValueChange = {
                        userEmail = it
                    })
                Spacer(modifier = Modifier.padding(8.dp))
                TextField(
                    value = username,
                    placeholder = {
                        Text(text = "Enter username")
                    },
                    onValueChange = {
                        username = it
                    })
                Spacer(modifier = Modifier.padding(8.dp))
                TextField(
                    value = macAddress,
                    placeholder = {
                        Text(text = "mac address (will remove tomorrow)")
                    },
                    onValueChange = {
                        macAddress = it
                    }
                )
                Spacer(modifier = Modifier.padding(8.dp))
                TextField(
                    value = userPassword,
                    placeholder = {
                        Text(text = "Enter password")
                    },
                    onValueChange = {
                        userPassword = it
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Button(onClick = {
                    viewModel.signUp(
                        context,
                        userEmail,
                        userPassword,
                        username,
                        macAddress
                    )
                }) {
                    Text(text = "Sign Up")
                }

                Button(onClick = {
                    viewModel.login(
                        context,
                        userEmail,
                        userPassword,
                    )
                }) {
                    Text(text = "Login")
                }

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        viewModel.logout(context)
                    }) {
                    Text(text = "Logout")
                }

                when (userState) {
                    is UserState.Loading -> {
                        LoadingComponent()
                    }

                    is UserState.Success -> {
                        val message = (userState as UserState.Success).message
                        currentUserState = message
                    }

                    is UserState.LoggedIn -> {
                        LoggedInScreen(onCreateGameClick = {
                            navigateToCreateGame = true
                        })
                    }

                    is UserState.Error -> {
                        val message = (userState as UserState.Error).message
                        currentUserState = message
                    }
                }

                if (currentUserState.isNotEmpty()) {
                    Text(text = currentUserState)
                }
            }
        }
    }
    //endregion
}