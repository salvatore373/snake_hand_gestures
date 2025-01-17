package com.snakehandgestures

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task

class GoogleSignInManager(private val activity: ComponentActivity) {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    init {
        setupGoogleSignIn()
        setupActivityResultLauncher()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    private fun setupActivityResultLauncher() {
        signInLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == ComponentActivity.RESULT_OK) {
                handleSignInResult(result.data)
            } else {
                Log.e("GoogleSignInManager", "Sign-in canceled or failed")
            }
        }
    }

    fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(data: Intent?) {
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account: GoogleSignInAccount = task.getResult(Exception::class.java)
            val username = account.displayName
            Log.i("GoogleSignInManager", "Sign-in successful, username: $username")
        } catch (e: Exception) {
            Log.e("GoogleSignInManager", "Sign-in failed", e)
        }
    }

    fun signOut() {
        googleSignInClient.signOut()
    }

    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(activity)
        return account != null
    }

    fun getSignedInUserName(): String {
        val account = GoogleSignIn.getLastSignedInAccount(activity)
        return account!!.displayName!!
    }
}
