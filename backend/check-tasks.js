// Check all tasks in database
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

async function checkTasks() {
  try {
    // Connect to MongoDB
    const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/cpen321';
    await mongoose.connect(mongoUri);
    console.log('✅ Connected to MongoDB\n');

    // Get all tasks
    const allTasks = await Task.find({}).sort({ createdAt: -1 }).limit(15);
    
    console.log(`📋 Total tasks: ${allTasks.length}\n`);
    console.log('='.repeat(80));

    for (const task of allTasks) {
      console.log(`\nTitle: "${task.title}"`);
      console.log(`  ID: ${task._id}`);
      console.log(`  Status: ${task.status}`);
      console.log(`  Deadline: ${task.deadline}`);
      console.log(`  Calendar Event ID: ${task.calendarEventId || '❌ NOT SET'}`);
      console.log(`  Created: ${task.createdAt}`);
      console.log(`  Assignees: ${task.assignees.length}`);
      
      // Check assignee calendar status
      for (const assigneeId of task.assignees) {
        const user = await User.findById(assigneeId);
        if (user) {
          console.log(`    👤 ${user.name}: Calendar Enabled = ${user.calendarEnabled}, Has Token = ${!!user.calendarRefreshToken}`);
        }
      }
    }

    console.log('\n' + '='.repeat(80));
    console.log('\n✅ Check complete!');
    
  } catch (error) {
    console.error('❌ Error:', error);
  } finally {
    await mongoose.disconnect();
  }
}

checkTasks();
