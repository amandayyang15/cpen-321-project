// Check current calendar connection status
const mongoose = require('mongoose');
require('dotenv').config();

const userSchema = new mongoose.Schema({
  name: String,
  email: String,
  calendarEnabled: Boolean,
  calendarRefreshToken: String,
});

const User = mongoose.model('User', userSchema);

async function checkCalendarStatus() {
  try {
    const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/cpen321';
    await mongoose.connect(mongoUri);
    console.log('✅ Connected to MongoDB\n');

    const users = await User.find({});
    
    console.log('='.repeat(80));
    console.log('CALENDAR CONNECTION STATUS');
    console.log('='.repeat(80));
    
    for (const user of users) {
      console.log(`\n👤 User: ${user.name} (${user.email})`);
      console.log(`   Calendar Enabled: ${user.calendarEnabled}`);
      
      if (user.calendarRefreshToken) {
        const isTestToken = user.calendarRefreshToken.startsWith('test_token_');
        console.log(`   Refresh Token: ${isTestToken ? '🧪 TEST TOKEN' : '✅ REAL TOKEN'}`);
        
        if (isTestToken) {
          console.log(`   ⚠️  WARNING: Using test token - events will NOT appear in Google Calendar!`);
          console.log(`   ℹ️  To connect real calendar, run:`);
          console.log(`      1. Disconnect: POST /api/calendar/disconnect`);
          console.log(`      2. Authorize: GET /api/calendar/oauth/authorize`);
        } else {
          console.log(`   ✅ Real Google Calendar connected!`);
          console.log(`   Token (first 20 chars): ${user.calendarRefreshToken.substring(0, 20)}...`);
        }
      } else {
        console.log(`   Refresh Token: ❌ NOT SET`);
        console.log(`   ℹ️  To connect, visit: GET /api/calendar/oauth/authorize`);
      }
    }
    
    console.log('\n' + '='.repeat(80));
    console.log('\n📋 Summary:');
    const testTokenUsers = users.filter(u => u.calendarRefreshToken?.startsWith('test_token_'));
    const realTokenUsers = users.filter(u => u.calendarRefreshToken && !u.calendarRefreshToken.startsWith('test_token_'));
    const noTokenUsers = users.filter(u => !u.calendarRefreshToken);
    
    console.log(`   Users with test tokens: ${testTokenUsers.length}`);
    console.log(`   Users with real tokens: ${realTokenUsers.length}`);
    console.log(`   Users without tokens: ${noTokenUsers.length}`);
    
    if (testTokenUsers.length > 0) {
      console.log('\n⚠️  TEST MODE ACTIVE - Tasks will not appear in Google Calendar!');
    }
    
  } catch (error) {
    console.error('❌ Error:', error);
  } finally {
    await mongoose.disconnect();
  }
}

checkCalendarStatus();
