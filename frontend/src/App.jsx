import { useEffect, useState } from 'react';
import Dashboard from './components/Dashboard.jsx';
import { requestJson } from './api.js';

export default function App() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [lastUpdated, setLastUpdated] = useState(null);

  async function loadDashboard(showLoading = false) {
    if (showLoading) setLoading(true);
    try {
      const result = await requestJson('/api/dashboard');
      setDashboard(result);
      setError('');
      setLastUpdated(new Date());
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDashboard(true);
    const refreshTimer = window.setInterval(() => loadDashboard(), 4000);
    return () => window.clearInterval(refreshTimer);
  }, []);

  return (
    <Dashboard
      data={dashboard}
      loading={loading}
      error={error}
      lastUpdated={lastUpdated}
      onRefresh={() => loadDashboard(true)}
      onTransactionSubmitted={() => loadDashboard()}
    />
  );
}