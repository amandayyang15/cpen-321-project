// Test if calendar event creation works with the real token
const mongoose = require('mongoose');
const { google } = require('googleapis');
require('dotenv').config();

const userSchema = new mongoose.Schema({
  name: String,
  email: String,
  calendarEnabled: Boolean,
  calendarRefreshToken: String,
});

const User = mongoose.model('User', userSchema);

async function testCalendarEvent() {
  try {
    const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/cpen321';
    await mongoose.connect(mongoUri);
    console.log('✅ Connected to MongoDB\n');

    // Find user with real token
    const user = await User.findOne({ 
      name: 'Ricky Lin',
      calendarRefreshToken: { $exists: true, $ne: null }
    });

    if (!user) {
      console.log('❌ User not found');
      return;
    }

    console.log(`👤 Testing calendar for: ${user.name}`);
    console.log(`   Calendar Enabled: ${user.calendarEnabled}`);
    console.log(`   Has Token: ${!!user.calendarRefreshToken}`);
    console.log(`   Token Type: ${user.calendarRefreshToken.startsWith('test_token_') ? 'TEST' : 'REAL'}\n`);

    if (user.calendarRefreshToken.startsWith('test_token_')) {
      console.log('⚠️  Still using test token - skipping API test');
      return;
    }

    // Try to create a test event
    console.log('🔄 Attempting to create test calendar event...\n');

    const oauth2Client = new google.auth.OAuth2(
      process.env.GOOGLE_CLIENT_ID,
      process.env.GOOGLE_CLIENT_SECRET,
      process.env.GOOGLE_CALENDAR_REDIRECT_URI
    );

    oauth2Client.setCredentials({
      refresh_token: user.calendarRefreshToken,
    });

    const calendar = google.calendar({ version: 'v3', auth: oauth2Client });

    // Create test event for tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(14, 0, 0, 0); // 2 PM tomorrow

    const event = {
      summary: '🧪 Test Task from Backend Script',
      description: 'This is a test event created to verify calendar integration works',
      start: {
        dateTime: tomorrow.toISOString(),
        timeZone: 'America/Vancouver',
      },
      end: {
        dateTime: tomorrow.toISOString(),
        timeZone: 'America/Vancouver',
      },
      reminders: {
        useDefault: false,
        overrides: [
          { method: 'popup', minutes: 60 },
        ],
      },
    };

    const response = await calendar.events.insert({
      calendarId: 'primary',
      requestBody: event,
    });

    console.log('✅ SUCCESS! Event created in Google Calendar!');
    console.log(`   Event ID: ${response.data.id}`);
    console.log(`   Event Link: ${response.data.htmlLink}`);
    console.log(`   Summary: ${response.data.summary}`);
    console.log(`   Start: ${response.data.start.dateTime}`);
    console.log('\n🎉 Your real Google Calendar integration is working!');
    console.log('   Check your Google Calendar to see the test event.');
    console.log('\n💡 Now when you create tasks with deadlines in your app, they will appear in your calendar!');

  } catch (error) {
    console.error('❌ Error testing calendar:', error.message);
    if (error.response?.data) {
      console.error('   API Error:', JSON.stringify(error.response.data, null, 2));
    }
    console.log('\n⚠️  The token might need to be refreshed. Try creating a task in your app to trigger a refresh.');
  } finally {
    await mongoose.disconnect();
  }
}

testCalendarEvent();
