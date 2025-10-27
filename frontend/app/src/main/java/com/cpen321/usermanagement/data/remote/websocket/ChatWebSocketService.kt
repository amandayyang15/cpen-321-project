package com.cpen321.usermanagement.data.remote.websocket

import android.util.Log
import com.cpen321.usermanagement.BuildConfig
import com.cpen321.usermanagement.data.remote.dto.ChatMessage
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatWebSocketService @Inject constructor() {
    
    companion object {
        private const val TAG = "ChatWebSocketService"
    }
    
    private var socket: Socket? = null
    private var authToken: String? = null
    private var currentProjectId: String? = null
    private var messageCallback: ((ChatMessage) -> Unit)? = null
    
    fun setAuthToken(token: String) {
        this.authToken = token
        Log.d(TAG, "Auth token set")
    }
    
    fun connect(callback: (ChatMessage) -> Unit) {
        Log.d(TAG, "connect() called, authToken: ${authToken?.take(10)}...")
        
        // Store the callback
        messageCallback = callback
        
        // If already connected, just return (callback is now set)
        if (socket?.connected() == true) {
            Log.d(TAG, "Socket already connected, callback registered")
            return
        }
        
        // If there's an existing socket but it's not connected, disconnect it first
        if (socket != null && !socket!!.connected()) {
            Log.d(TAG, "Disconnecting existing socket before reconnecting")
            socket?.disconnect()
            socket = null
        }
        
        if (authToken.isNullOrEmpty()) {
            Log.e(TAG, "Cannot connect: auth token is not set!")
            return
        }
        
        try {
            val baseUrl = BuildConfig.API_BASE_URL
                .removeSuffix("/")
                .removeSuffix("/api/")
                .removeSuffix("/api")
            
            Log.d(TAG, "Connecting to WebSocket server: $baseUrl")
            
            val options = IO.Options.builder()
                .setAuth(
                    mapOf(
                        "token" to (authToken ?: "")
                    )
                )
                .setTransports(arrayOf("websocket", "polling"))
                .setTimeout(20000)
                .build()
            
            socket = IO.socket(baseUrl, options)
            
            socket?.apply {
                on(Socket.EVENT_CONNECT) {
                    Log.d(TAG, "WebSocket connected")
                    // If we have a project to join, join it now that we're connected
                    currentProjectId?.let { projectId ->
                        Log.d(TAG, "Auto-joining project room: $projectId")
                        socket?.emit("join_project", projectId)
                    }
                }
                
                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.e(TAG, "WebSocket connection error: ${args.contentToString()}")
                }
                
                on(Socket.EVENT_DISCONNECT) { args ->
                    Log.d(TAG, "WebSocket disconnected: ${args.contentToString()}")
                }
                
                on("joined_project") { args ->
                    val data = args[0] as? JSONObject
                    Log.d(TAG, "Joined project: ${data?.toString()}")
                }
                
                on("left_project") { args ->
                    val data = args[0] as? JSONObject
                    Log.d(TAG, "Left project: ${data?.toString()}")
                }
                
                on("new_message") { args ->
                    try {
                        val messageJson = args[0] as? JSONObject
                        if (messageJson != null) {
                            val message = parseMessageFromJson(messageJson)
                            Log.d(TAG, "Received new message: ${message.id}")
                            messageCallback?.invoke(message)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing new message", e)
                    }
                }
                
                on("message_deleted") { args ->
                    val data = args[0] as? JSONObject
                    val messageId = data?.getString("messageId")
                    Log.d(TAG, "Message deleted: $messageId")
                    // Handle message deletion if needed
                }
                
                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.e(TAG, "WebSocket error: ${args.contentToString()}")
                }
            }
            
            socket?.connect()
            Log.d(TAG, "Socket connection initiated")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating socket connection", e)
        }
    }
    
    fun joinProject(projectId: String) {
        Log.d(TAG, "joinProject called for: $projectId")
        currentProjectId = projectId  // Store project ID for auto-join on connect
        
        if (socket?.connected() == true) {
            Log.d(TAG, "Joining project room: $projectId")
            socket?.emit("join_project", projectId)
        } else {
            Log.d(TAG, "Socket not connected yet, will join when connected")
            // Project ID is stored in currentProjectId, will be used in EVENT_CONNECT
        }
    }
    
    fun leaveProject(projectId: String) {
        if (socket?.connected() == true && currentProjectId == projectId) {
            Log.d(TAG, "Leaving project: $projectId")
            socket?.emit("leave_project", projectId)
            currentProjectId = null
        }
    }
    
    fun disconnect() {
        currentProjectId?.let { leaveProject(it) }
        socket?.disconnect()
        socket = null
        Log.d(TAG, "Socket disconnected")
    }
    
    fun isConnected(): Boolean {
        return socket?.connected() == true
    }
    
    fun isInProject(): Boolean {
        return currentProjectId != null
    }
    
    private fun parseMessageFromJson(json: JSONObject): ChatMessage {
        return ChatMessage(
            id = json.getString("id"),
            content = json.getString("content"),
            senderName = json.getString("senderName"),
            senderId = json.getString("senderId"),
            timestamp = json.getLong("timestamp"),
            projectId = json.getString("projectId"),
            isFromCurrentUser = false
        )
    }
}

