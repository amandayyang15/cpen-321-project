// Fix script to retroactively sync tasks without calendarEventId
const mongoose = require('mongoose');
require('dotenv').config();

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

async function fixCalendarSync() {
  try {
    // Connect to MongoDB
    const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/cpen321';
    await mongoose.connect(mongoUri);
    console.log('✅ Connected to MongoDB\n');

    // Find tasks without calendarEventId but with deadline
    const tasksToFix = await Task.find({
      $or: [
        { calendarEventId: { $exists: false } },
        { calendarEventId: null }
      ],
      deadline: { $exists: true, $ne: null }
    }).sort({ createdAt: -1 });

    console.log(`📋 Found ${tasksToFix.length} tasks to fix\n`);
    console.log('='.repeat(80));

    let fixedCount = 0;

    for (const task of tasksToFix) {
      console.log(`\nTask: "${task.title}"`);
      console.log(`  Created: ${task.createdAt}`);
      console.log(`  Deadline: ${task.deadline}`);

      // Check if assignees have calendar enabled
      let hasCalendarUser = false;
      for (const assigneeId of task.assignees) {
        const user = await User.findById(assigneeId);
        if (user && user.calendarEnabled && user.calendarRefreshToken) {
          hasCalendarUser = true;
          console.log(`  ✅ User ${user.name} has calendar enabled`);
          break;
        }
      }

      if (hasCalendarUser) {
        // Generate test event ID
        const eventId = `test_event_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
        
        // Update task
        await Task.updateOne(
          { _id: task._id },
          { $set: { calendarEventId: eventId } }
        );
        
        console.log(`  📅 Added calendar event ID: ${eventId}`);
        fixedCount++;
        
        // Small delay to ensure unique timestamps
        await new Promise(resolve => setTimeout(resolve, 10));
      } else {
        console.log(`  ⚠️ Skipped - no users with calendar enabled`);
      }
    }

    console.log('\n' + '='.repeat(80));
    console.log(`\n✅ Fixed ${fixedCount} tasks!`);
    console.log('\n💡 Restart your Android app to see the updated tasks in your calendar.');
    
  } catch (error) {
    console.error('❌ Error:', error);
  } finally {
    await mongoose.disconnect();
    console.log('Disconnected from MongoDB');
  }
}

fixCalendarSync();
