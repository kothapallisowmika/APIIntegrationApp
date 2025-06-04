package com.example.apiintegrationapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.apiintegrationapp.ui.theme.APIIntegrationAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            APIIntegrationAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "postList") {
                    composable("postList") {
                        PostListScreen(navController)
                    }
                    composable(
                        route = "postDetail/{title}/{body}",
                        arguments = listOf(
                            navArgument("title") { type = NavType.StringType },
                            navArgument("body") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val title = backStackEntry.arguments?.getString("title") ?: ""
                        val body = backStackEntry.arguments?.getString("body") ?: ""
                        PostDetailScreen(title, body, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun PostListScreen(navController: NavHostController) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    fun fetchPosts() {
        isLoading = true
        error = ""
        RetrofitInstance.api.getPosts().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                isLoading = false
                posts = response.body() ?: emptyList()
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                isLoading = false
                error = t.message ?: "Unknown error"
            }
        })
    }

    LaunchedEffect(Unit) {
        fetchPosts()
    }

    val filteredPosts = posts.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (error.isNotEmpty()) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn {
                items(filteredPosts) { post ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = {
                            navController.navigate("postDetail/${post.title}/${post.body}")
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📌 ${post.title}", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(post.body, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
