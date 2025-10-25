// Test the timezone fix by creating an event with correct date
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

async function testTimezoneFix() {
  try {
    const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/cpen321';
    await mongoose.connect(mongoUri);
    console.log('✅ Connected to MongoDB\n');

    const user = await User.findOne({ 
      name: 'Ricky Lin',
      calendarRefreshToken: { $exists: true, $ne: null }
    });

    if (!user || user.calendarRefreshToken.startsWith('test_token_')) {
      console.log('❌ Need real token');
      return;
    }

    console.log('🧪 Testing timezone fix...\n');

    const oauth2Client = new google.auth.OAuth2(
      process.env.GOOGLE_CLIENT_ID,
      process.env.GOOGLE_CLIENT_SECRET,
      process.env.GOOGLE_CALENDAR_REDIRECT_URI
    );

    oauth2Client.setCredentials({
      refresh_token: user.calendarRefreshToken,
    });

    const calendar = google.calendar({ version: 'v3', auth: oauth2Client });

    // Test with Oct 26 deadline (stored as midnight UTC)
    const deadline = new Date('2025-10-26T00:00:00.000Z');
    
    console.log('📅 Deadline from database: 2025-10-26T00:00:00.000Z');
    console.log('   Expected in calendar: October 26, 2025 (all-day)\n');

    // Format as date-only (the fix)
    const formatDateOnly = (date) => {
      const year = date.getUTCFullYear();
      const month = String(date.getUTCMonth() + 1).padStart(2, '0');
      const day = String(date.getUTCDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    };

    const startDate = formatDateOnly(deadline);
    const endDate = formatDateOnly(new Date(deadline.getTime() + 24 * 60 * 60 * 1000));

    console.log(`🔧 Using date format (fixed):`);
    console.log(`   Start date: ${startDate}`);
    console.log(`   End date: ${endDate}\n`);

    const event = {
      summary: '✅ FIXED - Oct 26 Deadline Test',
      description: 'Testing timezone fix - should show Oct 26',
      start: {
        date: startDate, // Use 'date' instead of 'dateTime'
      },
      end: {
        date: endDate,
      },
    };

    const response = await calendar.events.insert({
      calendarId: 'primary',
      requestBody: event,
    });

    console.log('✅ Test event created!');
    console.log(`   Event ID: ${response.data.id}`);
    console.log(`   Summary: ${response.data.summary}`);
    console.log(`   Start: ${response.data.start.date}`);
    console.log(`   Link: ${response.data.htmlLink}`);
    console.log('\n🎉 Check your Google Calendar - the event should be on October 26!');

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    await mongoose.disconnect();
  }
}

testTimezoneFix();
