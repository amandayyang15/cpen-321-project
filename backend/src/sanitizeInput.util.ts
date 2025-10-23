export const sanitizeArgs = (args: unknown[]): unknown[] => {
  return args.map(arg => {
    if (typeof arg === 'object' && arg !== null) {
      // For objects, use JSON.stringify without sanitization for logging
      return JSON.stringify(arg, null, 2);
    }
    return String(arg);
  });
};

export const sanitizeInput = (input: string): string => {
  // Only check for actual CRLF injection patterns, not legitimate newlines in JSON
  if (input.includes('\r\n') && (input.includes('HTTP/') || input.includes('Set-Cookie') || input.includes('Location'))) {
    throw new Error('CRLF injection attempt detected');
  }
  return input;
};
