import { useState } from 'react';

function formatAmount(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(value);
}

function formatTimestamp(value) {
  return new Date(value).toLocaleString('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

export default function AlertTable({ alerts }) {
  const [exportError, setExportError] = useState('');

  async function exportAlerts() {
    setExportError('');
    try {
      const response = await fetch('/api/alerts/export');
      if (!response.ok) throw new Error('Alert export could not be downloaded.');
      const file = await response.blob();
      const url = URL.createObjectURL(file);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'alerts.xlsx';
      link.click();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (requestError) {
      setExportError(requestError.message);
    }
  }

  return (
    <section className="panel alert-panel">
      <div className="panel-heading compact">
        <div>
          <p className="eyebrow">RULE MATCHES</p>
          <h2>Recent alerts</h2>
        </div>
        <div className="alert-actions">
          <span className="table-count">{alerts.length} records</span>
          <button className="export-button" type="button" onClick={exportAlerts} title="Download all alerts as an Excel file">
            <span aria-hidden="true">↓</span>
            Export Excel
          </button>
        </div>
      </div>

      {exportError && <p className="form-error export-error" role="alert">{exportError}</p>}

      {alerts.length === 0 ? (
        <div className="empty-state">No alerts have been generated yet.</div>
      ) : (
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Transaction</th>
                <th>User</th>
                <th>Amount</th>
                <th>Type</th>
                <th>Reason</th>
                <th>Detected</th>
              </tr>
            </thead>
            <tbody>
              {alerts.map((alert) => (
                <tr key={alert.id}>
                  <td className="mono-cell">{alert.transactionId}</td>
                  <td>{alert.userId}</td>
                  <td className="amount-cell">{formatAmount(alert.amount)}</td>
                  <td>
                    <span className="anomaly-tag">{alert.anomalyType}</span>
                  </td>
                  <td className="reason-cell">{alert.reason}</td>
                  <td className="time-cell">{formatTimestamp(alert.detectedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}