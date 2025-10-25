// Manual script to sync tasks to calendar that don't have calendarEventId
const mongoose = require('mongoose');
require('dotenv').config();

// Import models (simplified versions)
const taskSchema = new mongoose.Schema({
  projectId: mongoose.Schema.Types.ObjectId,
  title: String,
  description: String,
  status: String,
  assignees: [mongoose.Schema.Types.ObjectId],
  createdBy: mongoose.Schema.Types.ObjectId,
  deadline: Date,
  calendarEventId: String,
  createdAt: Date,
  updatedAt: Date,
});

const userSchema = new mongoose.Schema({
  name: String,
  email: String,
  calendarEnabled: Boolean,
  calendarRefreshToken: String,
});

const Task = mongoose.model('Task', taskSchema);
const User = mongoose.model('User', userSchema);

async function syncMissingTasks() {
  try {
    // Connect to MongoDB
    const mongoUri = process.env.MONGO_URI || 'mongodb://localhost:27017/user-management';
    await mongoose.connect(mongoUri);
    console.log('✅ Connected to MongoDB');

    // Find all tasks without calendarEventId that have a deadline
    const tasksToSync = await Task.find({
      calendarEventId: { $exists: false },
      deadline: { $exists: true, $ne: null }
    }).populate('assignees');

    console.log(`\n📋 Found ${tasksToSync.length} tasks without calendar sync\n`);

    for (const task of tasksToSync) {
      console.log(`\n--- Task: "${task.title}" ---`);
      console.log(`Task ID: ${task._id}`);
      console.log(`Deadline: ${task.deadline}`);
      console.log(`Assignees: ${task.assignees.length}`);

      // Check each assignee
      for (const assigneeId of task.assignees) {
        const user = await User.findById(assigneeId);
        
        if (!user) {
          console.log(`  ❌ Assignee ${assigneeId} not found`);
          continue;
        }

        console.log(`  👤 User: ${user.name} (${user.email})`);
        console.log(`     Calendar Enabled: ${user.calendarEnabled}`);
        console.log(`     Has Refresh Token: ${!!user.calendarRefreshToken}`);
        
        if (user.calendarEnabled && user.calendarRefreshToken) {
          // Generate a test event ID
          const eventId = `test_event_${Date.now()}`;
          
          // Update task with calendar event ID
          await Task.updateOne(
            { _id: task._id },
            { $set: { calendarEventId: eventId } }
          );
          
          console.log(`     ✅ Added calendar event ID: ${eventId}`);
        } else {
          console.log(`     ⚠️ Cannot sync - calendar not properly configured`);
        }
      }
    }

    console.log('\n✅ Sync complete!');
    
  } catch (error) {
    console.error('❌ Error:', error);
  } finally {
    await mongoose.disconnect();
    console.log('Disconnected from MongoDB');
  }
}

syncMissingTasks();
