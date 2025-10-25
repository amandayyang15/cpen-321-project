// Quick test script to check calendar authorization endpoint
// Run this to get the OAuth URL

const JWT_TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY4Zjk4OTYzZTcxNGIyMzk5M2M4NzBlZiIsImlhdCI6MTc2MTM1MzgxOCwiZXhwIjoxNzYxNDIyMjE4fQ.1lmrE2X7nh7E5tfOZUtI9Pj1EpifpInyJMtTdd3uL6Y'; // Your JWT token

console.log('\n🔍 Testing Calendar Authorization Endpoint\n');
console.log('Run this command to get the Google OAuth URL:\n');
console.log(`curl -X GET http://localhost:3000/api/calendar/oauth/authorize -H "Authorization: Bearer ${JWT_TOKEN}"`);
console.log('\n');
console.log('Or use this PowerShell command:\n');
console.log(`Invoke-RestMethod -Uri "http://localhost:3000/api/calendar/oauth/authorize" -Method GET -Headers @{"Authorization"="Bearer ${JWT_TOKEN}"}`);
console.log('\n');
console.log('Then open the authUrl in your browser to authorize!\n');
