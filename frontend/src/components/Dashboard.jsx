import { useState } from 'react';
import SummaryCards from './SummaryCards.jsx';
import TransactionForm from './TransactionForm.jsx';
import TransactionTable from './TransactionTable.jsx';
import AlertTable from './AlertTable.jsx';

function formatLastUpdated(value) {
  if (!value) return 'Waiting for data';
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(value);
}

export default function Dashboard({ data, loading, error, lastUpdated, onRefresh, onTransactionSubmitted }) {
  const [activeSection, setActiveSection] = useState('overview');

  const totalTransactions = data?.totalTransactions ?? 0;
  const totalAlerts = data?.totalAlerts ?? 0;
  const velocityAlerts = data?.velocityAlerts ?? 0;
  const aboveMeanAmountAlerts = data?.aboveMeanAmountAlerts ?? 0;

  const alertMix = [
    { label: 'Velocity', value: velocityAlerts, tone: 'tone-blue' },
    { label: 'Above mean', value: aboveMeanAmountAlerts, tone: 'tone-amber' },
    { label: 'Other alert types', value: Math.max(totalAlerts - velocityAlerts - aboveMeanAmountAlerts, 0), tone: 'tone-rose' },
  ];

  const navItems = [
    { id: 'overview', label: 'Overview', icon: '◉' },
    { id: 'transactions', label: 'Transactions', icon: '⇄' },
    { id: 'alerts', label: 'Anomaly Alerts', icon: '⚠' },
    { id: 'analytics', label: 'Analytics', icon: '◫' },
    { id: 'health', label: 'System Health', icon: '▣' },
  ];

  const scrollToSection = (sectionId) => {
    setActiveSection(sectionId);
    const section = document.getElementById(sectionId);
    if (section) {
      section.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  return (
    <div className="dashboard-shell">
      <aside className="sidebar">
        <div className="brand-box">
          <div className="brand-mark">U</div>
          <div>
            <div className="brand-name">UPI Shield</div>
            <div className="brand-subtitle">Security Monitor</div>
          </div>
        </div>

        <nav className="sidebar-nav" aria-label="Sidebar navigation">
          {navItems.map((item) => (
            <button
              key={item.id}
              type="button"
              className={`nav-item ${activeSection === item.id ? 'active' : ''}`}
              onClick={() => scrollToSection(item.id)}
            >
              <span aria-hidden="true">{item.icon}</span>
              {item.label}
            </button>
          ))}
        </nav>

        <div className="sidebar-card">
          <p className="eyebrow white">Capture</p>
          <h3>Rule-driven monitoring</h3>
          <p>{totalTransactions} transactions tracked with {totalAlerts} alerts recorded.</p>
        </div>
      </aside>

      <main className="main-panel">
        <header id="overview" className="topbar dashboard-section">
          <div>
            <p className="eyebrow">PAYMENT SECURITY</p>
            <h1>UPI anomaly intelligence</h1>
          </div>

          <div className="topbar-actions">
            <div className={`connection-chip ${error ? 'offline' : 'online'}`}>
              <span className="connection-dot" />
              {error ? 'API unavailable' : 'Live monitoring'}
            </div>
            <button className="refresh-button" type="button" onClick={onRefresh} title="Refresh dashboard">
              <span aria-hidden="true">↻</span>
              Refresh
            </button>
          </div>
        </header>

        <div className="meta-row dashboard-section">
          <div>
            <span className="meta-label">Page status</span>
            <strong>{error ? 'Connection issue' : 'System healthy'}</strong>
          </div>
          <div>
            <span className="meta-label">Last updated</span>
            <strong>{formatLastUpdated(lastUpdated)}</strong>
          </div>
        </div>

        {error && (
          <div className="notice notice-error" role="alert">
            <strong>Dashboard load failed.</strong> {error}
            <button type="button" onClick={onRefresh}>Retry</button>
          </div>
        )}

        {loading && !data ? (
          <div className="loading-state" role="status">Connecting to the anomaly engine…</div>
        ) : (
          <>
            <section id="overview-metrics" className="dashboard-section">
              <SummaryCards data={data} />
            </section>

            <section id="analytics" className="analytics-row dashboard-section">
              <div className="panel inform-panel">
                <div className="panel-heading compact">
                  <div>
                    <p className="eyebrow">ANALYTICS</p>
                    <h2>Alert mix</h2>
                  </div>
                </div>
                <div className="bar-list">
                  {alertMix.map((item) => {
                    const total = totalAlerts || 1;
                    const width = Math.max((item.value / total) * 100, item.value > 0 ? 8 : 0);
                    return (
                      <div key={item.label} className="bar-row">
                        <div className="bar-meta">
                          <span>{item.label}</span>
                          <strong>{item.value}</strong>
                        </div>
                        <div className="bar-track">
                          <span className={`bar-fill ${item.tone}`} style={{ width: `${width}%` }} />
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>

              <div className="panel inform-panel" id="health">
                <div className="panel-heading compact">
                  <div>
                    <p className="eyebrow">THREAT LEVEL</p>
                    <h2>System posture</h2>
                  </div>
                </div>
                <div className="health-grid">
                  <div className="metric-box">
                    <span className="metric-caption">Normal</span>
                    <strong>{Math.max(totalTransactions - totalAlerts, 0)}</strong>
                  </div>
                  <div className="metric-box warning-box">
                    <span className="metric-caption">Alerts</span>
                    <strong>{totalAlerts}</strong>
                  </div>
                  <div className="metric-box danger-box">
                    <span className="metric-caption">Velocity</span>
                    <strong>{velocityAlerts}</strong>
                  </div>
                </div>
              </div>
            </section>

            <section id="transactions" className="workspace-grid dashboard-section">
              <TransactionForm onSubmitted={onTransactionSubmitted} />
              <TransactionTable transactions={data?.recentTransactions || []} />
            </section>

            <section id="alerts" className="dashboard-section">
              <AlertTable alerts={data?.recentAlerts || []} />
            </section>
          </>
        )}
      </main>
    </div>
  );
}