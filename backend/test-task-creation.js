const mongoose = require('mongoose');
const axios = require('axios');

// Test script to verify task creation
async function testTaskCreation() {
  try {
    console.log('🧪 Testing Task Creation Flow...\n');

    // Connect to MongoDB
    await mongoose.connect('mongodb://localhost:27017/cpen321', {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    });
    console.log('✅ Connected to MongoDB');

    // Check existing tasks in database
    const Task = mongoose.model('Task', new mongoose.Schema({}, { strict: false }));
    const existingTasks = await Task.find({});
    console.log(`📊 Found ${existingTasks.length} existing tasks in database`);
    
    if (existingTasks.length > 0) {
      console.log('📋 Existing tasks:');
      existingTasks.forEach((task, index) => {
        console.log(`  ${index + 1}. ${task.title} (${task.status}) - Project: ${task.projectId}`);
      });
    }

    console.log('\n🔍 Database connection test completed');
    console.log('📝 To test task creation:');
    console.log('1. Start your backend server');
    console.log('2. Try creating a task from the frontend');
    console.log('3. Check the server logs for detailed information');
    console.log('4. Use the debug endpoint: GET /api/tasks/debug/all');

  } catch (error) {
    console.error('❌ Test failed:', error.message);
  } finally {
    await mongoose.disconnect();
    console.log('🔌 Disconnected from MongoDB');
  }
}

testTaskCreation();
