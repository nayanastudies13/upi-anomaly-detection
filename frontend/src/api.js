export async function requestJson(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  const result = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(result.error || `Request failed with status ${response.status}`);
  }
  return result;
}