/**
 * Script to enable calendar for a specific user
 * Usage: node enable-calendar-for-user.js "user@email.com"
 */

const mongoose = require('mongoose');

// Get email from command line argument
const userEmail = process.argv[2];

if (!userEmail) {
  console.error('❌ Please provide a user email as an argument');
  console.log('Usage: node enable-calendar-for-user.js "user@email.com"');
  process.exit(1);
}

const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/cpen321';

async function enableCalendar() {
  try {
    console.log('🔌 Connecting to MongoDB...');
    await mongoose.connect(MONGODB_URI);
    console.log('✅ Connected to MongoDB\n');

    // Define User schema
    const userSchema = new mongoose.Schema({
      name: String,
      email: String,
      calendarEnabled: Boolean,
      calendarRefreshToken: String,
    });
    
    const User = mongoose.models.User || mongoose.model('User', userSchema);
    
    // Find user by email
    const user = await User.findOne({ email: userEmail });
    
    if (!user) {
      console.error(`❌ User not found with email: ${userEmail}`);
      await mongoose.connection.close();
      process.exit(1);
    }

    console.log(`📋 Found user: ${user.name} (${user.email})`);
    console.log(`   Current status:`);
    console.log(`   - Calendar Enabled: ${user.calendarEnabled ? '✅ YES' : '❌ NO'}`);
    console.log(`   - Calendar Token: ${user.calendarRefreshToken ? '✅ Connected' : '❌ Not Connected'}\n`);

    if (!user.calendarRefreshToken) {
      console.log('⚠️  WARNING: This user does not have a calendar token yet!');
      console.log('   They need to authorize the app first by visiting:');
      console.log(`   http://localhost:3000/api/calendar/oauth/authorize`);
      console.log('\n   After authorizing, run this script again.\n');
    }

    // Enable calendar
    user.calendarEnabled = true;
    await user.save();

    console.log('✅ Calendar enabled successfully!');
    console.log(`   ${user.name}'s tasks with deadlines will now sync to Google Calendar.\n`);

    if (!user.calendarRefreshToken) {
      console.log('⚠️  Remember: User still needs to authorize the app first!');
    } else {
      console.log('🎉 All set! Create a task with a deadline to test it.');
    }

    await mongoose.connection.close();
  } catch (error) {
    console.error('❌ Error:', error.message);
    if (error.stack) {
      console.error(error.stack);
    }
    process.exit(1);
  }
}

enableCalendar();
